//package code.ponfee.commons.web;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
//import javax.servlet.ServletConfig;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.google.common.collect.ImmutableMap;
//
//import code.ponfee.commons.exception.BasicException;
//import code.ponfee.commons.http.HttpParams;
//import code.ponfee.commons.io.Files;
//import code.ponfee.commons.json.Jsons;
//import code.ponfee.commons.model.Result;
//import code.ponfee.commons.model.ResultCode;
//import code.ponfee.commons.web.WebUtils;
//
///**
// * 全局异常处理<p>
// * https://www.runoob.com/servlet/servlet-exception-handling.html
// * 
// * <pre>{@code 
// * 
// *   <servlet>
// *     <servlet-name>globalExceptionHandler</servlet-name>
// *     <servlet-class>cn.ponfee.web.framework.web.GlobalExceptionHandler</servlet-class>
// *     <init-param>
// *       <param-name>handlerType</param-name>
// *       <param-value>application/json</param-value>
// *     </init-param>
// *   </servlet>
// *   <servlet-mapping>
// *     <servlet-name>globalExceptionHandler</servlet-name>
// *     <url-pattern>/globalExceptionHandler</url-pattern>
// *   </servlet-mapping>
// *   <error-page>
// *     <error-code>404</error-code>
// *     <location>/globalExceptionHandler</location>
// *   </error-page>
// *   <error-page>
// *     <exception-type>java.lang.Throwable</exception-type>
// *     <location>/globalExceptionHandler</location>
// *   </error-page>
// *   <error-page>
// *     <error-code>403</error-code>
// *     <location>/globalExceptionHandler</location>
// *   </error-page>
// * 
// * }</pre>
// * 
// * @author Ponfee
// */
///*@WebServlet(
//    name = "cn.ponfee.web.framework.web.GlobalExceptionHandler",
//    urlPatterns = {"/globalExceptionHandler"},
//    initParams = {
//        @WebInitParam(name="handlerType", value="application/json")
//    },
//    asyncSupported=true
//)*/
//public class GlobalExceptionHandler extends HttpServlet {
//
//    private static final long serialVersionUID = 6067653829035388068L;
//    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
//    private static final String DEFAULT_HANDLER_TYPE = "application/json";
//    public static final String ERROR_MSG = "系统异常，请与管理员联系";
//
//    private String handlerType;
//    private String errorPage;
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
//        this.doPost(req, resp);
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
//        Throwable throwable = (Throwable) req.getAttribute("javax.servlet.error.exception");
//        Integer statusCode = (Integer) req.getAttribute("javax.servlet.error.status_code");
//        String servletName = (String) req.getAttribute("javax.servlet.error.servlet_name");
//        String requestUri = (String) req.getAttribute("javax.servlet.error.request_uri");
//        //Class<?> message = (Class<?>) req.getAttribute("javax.servlet.error.message");
//        //Class<?> type = (Class<?>) req.getAttribute("javax.servlet.error.exception_type");
//        logger.info("{}-{}-{}-{}", throwable, statusCode, servletName, requestUri);
//
//        try {
//            if (throwable != null) {
//                if (throwable instanceof BasicException 
//                    || throwable instanceof IllegalArgumentException) {
//                    logger.info("", throwable);
//                } else {
//                    logger.error("", throwable);
//                }
//                throw throwable;
//            } else {
//                throw new WebException(ResultCode.NOT_FOUND.getCode(), "file not found");
//            }
//        } catch (Throwable e) {
//            String errorMsg = (e instanceof BasicException) || logger.isInfoEnabled()
//                              ? e.getMessage() : ERROR_MSG;
//            errorMsg = StringUtils.isBlank(errorMsg) ?  ERROR_MSG : errorMsg;
//            switch (handlerType) {
//                case "application/json":
//                case "text/html":
//                case "text/plain":
//                    int code = (e instanceof BasicException) 
//                               ? ((BasicException) e).getCode() 
//                               : ResultCode.SERVER_ERROR.getCode();
//                    resp.setContentType(handlerType + ";charset=" + Files.UTF_8);
//                    try (PrintWriter writer = resp.getWriter()) {
//                        writer.print(Jsons.toJson(Result.failure(code, errorMsg)));
//                    } catch (IOException ex) {
//                        logger.error("", ex);
//                    }
//                    break;
//                default:
//                    if (errorPage != null) {
//                        String context = WebUtils.getContextPath(req);
//                        String url = HttpParams.buildUrlPath(context + errorPage, Files.UTF_8, 
//                                                             ImmutableMap.of("msg", errorMsg));
//                        try {
//                            resp.sendRedirect(resp.encodeRedirectURL(url));
//                        } catch (IOException ex) {
//                            logger.error("response send redirect occur error", ex);
//                        }
//                    } else {
//                        resp.setStatus(ResultCode.SERVER_ERROR.getCode());
//                        try (PrintWriter writer = resp.getWriter()) {
//                            writer.append(errorMsg);
//                        } catch (IOException ex) {
//                            logger.error("", ex);
//                        }
//                    }
//                    break;
//            }
//        }
//    }
//
//    @Override
//    public void init(ServletConfig config) throws ServletException {
//        super.init(config);
//        handlerType = config.getInitParameter("handlerType");
//        if (StringUtils.isBlank(handlerType)) {
//            handlerType = DEFAULT_HANDLER_TYPE;
//        }
//        handlerType = handlerType.toLowerCase();
//
//        errorPage = config.getInitParameter("errorPage");
//        if (StringUtils.isBlank(errorPage)) {
//            errorPage = null;
//        }
//    }
//
//}
