package code.ponfee.commons.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;

import code.ponfee.commons.io.Files;

/**
 * 本地线程级的web上下文持有类
 * 
 * https://github.com/alibaba/transmittable-thread-local
 * 
 * @author Ponfee
 */
public final class WebContext {

    /** HTTP请求与响应 */
    private static final ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletResponse> RESPONSE = new ThreadLocal<>();

    /** 用于非用户访问请求：程序内部反射调用controller方法 */
    private static final ThreadLocal<Map<String, String[]>> INJECTED_PARAMS =
        ThreadLocal.withInitial(HashMap::new);

    // -----------------------getter
    public static HttpServletRequest getRequest() {
        //<listener><listener-class>org.springframework.web.context.request.RequestContextListener</listener-class></listener>
        //return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return REQUEST.get();
    }

    public static HttpServletResponse getResponse() {
        //return ((ServletWebRequest)RequestContextHolder.getRequestAttributes()).getResponse();
        return RESPONSE.get();
    }

    /**
     * 设置参数
     * @param name
     * @param value
     */
    public static void setParameter(String name, String value) {
        String[] values = INJECTED_PARAMS.get().get(name);
        if (values == null || values.length == 0) {
            values = new String[] { value };
        } else {
            values = ArrayUtils.add(values, value);
        }
        INJECTED_PARAMS.get().put(name, values);
    }

    public static void clearParameter() {
        INJECTED_PARAMS.remove();
    }

    /**
     * 获取参数
     * @param name
     * @return
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        if (null != request) {
            return request.getParameter(name);
        } else {
            String[] values = INJECTED_PARAMS.get().get(name); // CUST_PARAMS.get().remove(name)
            return (values == null || values.length == 0) ? null : values[0];
        }
    }

    /**
     * 获取参数
     * @param name
     * @return
     */
    public static String[] getParameterValues(String name) {
        HttpServletRequest request = getRequest();
        if (null != request) {
            return request.getParameterValues(name);
        } else {
            return INJECTED_PARAMS.get().get(name);
        }
    }

    public String getText() {
        return getText(Files.DEFAULT_CHARSET_NAME);
    }

    /**
     * Gets the text string from request input stream
     * 
     * @param request the HttpServletRequest
     * @param charset the string encoding
     * @return string
     */
    public String getText(String charset) {
        return WebUtils.getText(getRequest(), charset);
    }

    /**
     * 获取客户端IP
     * @return
     */
    public static String getClientIp() {
        return WebUtils.getClientIp(getRequest());
    }

    /**
     * 获取客户端设备类型
     * @return
     */
    public static LiteDevice getClientDevice() {
        return WebUtils.getClientDevice(getRequest());
    }

    // --------------------------setter/remover
    private static void setRequest(HttpServletRequest req) {
        REQUEST.set(req);
    }

    private static void setResponse(HttpServletResponse resp) {
        RESPONSE.set(resp);
    }

    private static void removeRequest() {
        REQUEST.remove();
    }

    private static void removeResponse() {
        RESPONSE.remove();
    }

    /**
     * 过滤器：根据filterName的属性值的首字母排序的顺序执行
     */
    /*@WebFilter(
       filterName = "0000.code.ponfee.commons.web.WebContext$WebContextFilter",
       dispatcherTypes = {
           DispatcherType.REQUEST,
           DispatcherType.FORWARD,
           DispatcherType.INCLUDE,
           DispatcherType.ASYNC,
           DispatcherType.ERROR
        }, 
        urlPatterns = { "/*" }, 
        initParams = { @WebInitParam(name = "cors", value = "true") },
        asyncSupported = true // 支持异步Servlet
    )*/
    public static class WebContextFilter implements Filter {
        private boolean cors;

        @Override
        public void init(FilterConfig cfg) {
            cors = Boolean.parseBoolean(cfg.getInitParameter("cors"));
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) 
            throws IOException, ServletException { 
            try {
                WebContext.setRequest((HttpServletRequest) req);
                WebContext.setResponse((HttpServletResponse) resp);
                if (cors) {
                    WebUtils.cors((HttpServletRequest) req, (HttpServletResponse) resp);
                }
                chain.doFilter(req, resp);
            } finally {
                WebContext.removeRequest();
                WebContext.removeResponse();
            }
        }

        @Override
        public void destroy() {}
    }

}
