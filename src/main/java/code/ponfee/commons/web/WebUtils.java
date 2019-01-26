package code.ponfee.commons.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.http.ContentType;
import code.ponfee.commons.io.Files;
import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.util.Networks;
import code.ponfee.commons.util.UrlCoder;

/**
 * web工具类
 * @author fupf
 */
public final class WebUtils {

    private final static Pattern PATTERN_SUFFIX = Pattern.compile("\\S*[?]\\S*");

    /*private static final Pattern PATTERN_MOBILE = Pattern.compile(
        "\\b(ip(hone|od)|android|opera m(ob|in)i|windows (phone|ce)|blackberry|s(ymbian|eries60|amsung)"
      + "|p(laybook|alm|rofile/midp|laystation portable)|nokia|fennec|htc[-_]|mobile|up.browser"
      + "|[1-4][0-9]{2}x[1-4][0-9]{2})\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_IPAD = Pattern.compile(
        "\\b(ipad|tablet|(Nexus 7)|up.browser|[1-4][0-9]{2}x[1-4][0-9]{2})\\b", 
        Pattern.CASE_INSENSITIVE
    );*/

    /** authorization */
    public static final String AUTH_HEADER = "X-Auth-Token";
    public static final String AUTH_COOKIE = "auth_token";
    public static final String AUTH_PARAME = "authToken";

    /**
     * get the http servlet request parameters
     * the parameter value is {@link java.lang.String} type
     * if is array parameter, then the value is based-join on "," as String
     * @param request
     * @return Map<String, String>, the array param value use "," to join
     */
    public static Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = new TreeMap<>();
        for (Entry<String, String[]> entry : requestParams.entrySet()) {
            params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
        }
        return params;
    }

    public static String getText(HttpServletRequest request) {
        return getText(request, Files.UTF_8);
    }

    /**
     * get the text string from request input stream
     * @param request the HttpServletRequest
     * @param charset the string encoding
     * @return string
     */
    public static String getText(HttpServletRequest request, String charset) {
        try (InputStream input = request.getInputStream()) {
            return IOUtils.toString(input, charset);
        } catch (Exception e) {
            throw new RuntimeException("read request input stream error", e);
        }
    }

    /**
     * 获取客户端ip
     * @param req
     * @return
     */
    public static String getClientIp(HttpServletRequest req) {
        boolean invalid = true;
        String ip = req.getHeader("x-forwarded-for");
        if (invalid && (invalid = isInvalidIp(ip))) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (invalid && (invalid = isInvalidIp(ip))) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (invalid && (invalid = isInvalidIp(ip))) {
            ip = req.getHeader("HTTP_CLIENT_IP");
        }
        if (invalid && (invalid = isInvalidIp(ip))) {
            ip = req.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (invalid && (invalid = isInvalidIp(ip))) {
            ip = req.getHeader("X-Real-IP");
        }
        if (invalid && (invalid = isInvalidIp(ip))) {
            ip = req.getRemoteAddr();
        }

        if (ip != null && ip.indexOf(",") > 0) {
            // 对于通过多个代理的情况，第一个ip为客户端真实ip，多个ip按照','分割
            ip = ip.substring(0, ip.indexOf(","));
        }

        if (   "127.0.0.1".equals(ip) 
            || "0:0:0:0:0:0:0:1".equals(ip) 
            || "::1".equals(ip)
        ) {
            ip = Networks.HOST_IP; // 如果是本机ip
        }
        return ip;
    }

    /**
     * 获取客户端设备类型
     * @return
     */
    public static LiteDevice getClientDevice(HttpServletRequest req) {
        return new LiteDeviceResolver().resolveDevice(req);
        /*String userAgent = Objects.toString(userAgent(req), "");
        if (PATTERN_MOBILE.matcher(userAgent).find()) {
            return DeviceType.MOBILE;
        } else if (PATTERN_IPAD.matcher(userAgent).find()) {
            return DeviceType.TABLET;
        } else {
            return DeviceType.NORMAL;
        }*/
    }

    /**
     * 判断是否ajax请求
     * @param req
     * @return
     */
    public static boolean isAjax(HttpServletRequest req) {
        return "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
    }

    /**
     * Returns the web browser user-agent
     * 
     * @param req the HttpServletRequest
     * @return web browser user-agent
     */
    public static String userAgent(HttpServletRequest req) {
        return req.getHeader("User-Agent");
    }

    public static void response(HttpServletResponse resp, ContentType contentType,
                                String text, Charset charset) {
        response(resp, contentType.value(), text, charset.name());
    }

    /**
     * 响应数据到请求客户端
     * @param resp
     * @param contentType
     * @param text
     * @param charset
     */
    public static void response(HttpServletResponse resp, String contentType, 
                                String text, String charset) {
        resp.setContentType(contentType + ";charset=" + charset);
        resp.setCharacterEncoding(charset);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(text);
        } catch (IOException e) {
            // cannot happened
            throw new RuntimeException("response " + contentType + " occur error", e);
        }
    }

    /**
     * 响应json数据
     * @param resp
     * @param data
     */
    public static void respJson(HttpServletResponse resp, Object data) {
        respJson(resp, data, Files.UTF_8);
    }

    public static void respJson(HttpServletResponse resp, Object data, String charset) {
        response(resp, "application/json", toJson(data), charset);
    }

    public static void respJsonp(HttpServletResponse response, 
                                 String callback, Object data) {
        respJsonp(response, callback, data, Files.UTF_8);
    }

    /**
     * 响应jsonp数据
     * @param resp
     * @param callback
     * @param data
     * @param charset
     */
    public static void respJsonp(HttpServletResponse resp, String callback, 
                                 Object data, String charset) {
        respJson(resp, callback + "(" + toJson(data) + ");", charset);
    }

    /**
     * response to input stream
     * @param resp     the HttpServletResponse
     * @param input    the input stream
     * @param filename the resp attachment filename
     */
    public static void response(HttpServletResponse resp, 
                                InputStream input, String filename) {
        response(resp, input, filename, Files.UTF_8, false);
    }

    /**
     * response to input stream
     * @param resp      the HttpServletResponse
     * @param input     the input stream
     * @param filename  the resp attachment filename
     * @param charset   the attachment filename encoding
     * @param isGzip    {@code true} to use gzip compress
     */
    public static void response(HttpServletResponse resp, InputStream input, 
                                String filename, String charset, boolean isGzip) {
        try (InputStream in = input;
             OutputStream out = resp.getOutputStream()
        ) {
            respStream(resp, in.available(), filename, charset);
            if (isGzip) {
                resp.setHeader("Content-Encoding", "gzip");
                GzipProcessor.compress(in, out);
            } else {
                IOUtils.copyLarge(in, out);
            }
        } catch (IOException e) {
            // cannot happened
            throw new RuntimeException("response input stream occur error", e);
        }
    }

    /**
     * response to byte array
     * @param resp     the HttpServletResponse
     * @param data     the byte array data
     * @param filename the resp attachment filename
     */
    public static void response(HttpServletResponse resp, 
                                byte[] data, String filename) {
        response(resp, data, filename, Files.UTF_8, false);
    }

    /**
     * 响应流数据
     * @param resp      the HttpServletResponse
     * @param data      the resp byte array data
     * @param filename  the resp attachment filename
     * @param charset   the attachment filename encoding
     * @param isGzip    {@code true} to use gzip compress
     */
    public static void response(HttpServletResponse resp, byte[] data,
                                String filename, String charset, boolean isGzip) {
        try (OutputStream out = resp.getOutputStream()) {
            respStream(resp, data.length, filename, charset);
            if (isGzip) {
                resp.setHeader("Content-Encoding", "gzip");
                GzipProcessor.compress(data, out);
            } else {
                out.write(data);
            }
        } catch (IOException e) {
            // cannot happened
            throw new RuntimeException("response byte array data occur error", e);
        }
    }

    /**
     * ross-Origin Resource Sharing
     * 
     * @param req  the HttpServletRequest
     * @param resp the HttpServletResponse
     */
    public static void cors(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        origin = StringUtils.isEmpty(origin) ? "*" : origin;
        resp.setHeader("Access-Control-Allow-Origin", origin);

        String headers = req.getHeader("Access-Control-Allow-Headers");
        headers = StringUtils.isEmpty(headers) 
                  ? "Origin,No-Cache,X-Requested-With,If-Modified-Since,Pragma,"
                  + "Expires,Last-Modified,Cache-Control,Content-Type,X-E4M-With" 
                  : headers;
        resp.setHeader("Access-Control-Allow-Headers", headers);

        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,HEAD,OPTIONS");
        resp.setHeader("Access-Control-Max-Age", "0");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("XDomainRequestAllowed", "1");
    }

    /**
     * 获取请求地址后缀名
     * @param req
     * @return
     */
    public static String getUrlSuffix(HttpServletRequest req) {
        String url = req.getRequestURI();
        if (!url.contains(".")) {
            return null;
        }

        String[] pathInfos = url.split("/");
        String endUrl = pathInfos[pathInfos.length - 1];
        if (PATTERN_SUFFIX.matcher(url).find()) {
            String[] spEndUrl = endUrl.split("\\?");
            return spEndUrl[0].split("\\.")[1];
        } else {
            return endUrl.split("\\.")[1];
        }
    }

    public static String xssReplace(String text) {
        return StringUtils.replaceEach(
            text, 
            new String[] { "<", ">", "%3c", "%3e" }, 
            new String[] { "&lt;", "&gt;", "&lt;", "&gt;" }
        );
    }

    /**
     * get cookie value
     * @param req
     * @param name
     * @return
     */
    public static String getCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Delete the cookie by spec name
     * @param req
     * @param resp
     * @param name
     */
    public static void delCookie(HttpServletRequest req, 
                                 HttpServletResponse resp, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setMaxAge(0);
                cookie.setValue(null);
                resp.addCookie(cookie);
                return;
            }
        }
    }

    /**
     * 获取请求头参数
     * @param request
     * @param name
     * @return
     */
    public static String getHeader(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    /**
     * 设置cookie
     * @param response
     * @param name
     * @param value
     */
    public static void addCookie(HttpServletResponse response, 
                                 String name, String value) {
        addCookie(response, name, value, "/", 24 * 60 * 60);
    }

    /**
     * 设置cookie
     * @param resp
     * @param name
     * @param value
     * @param path
     * @param maxAge
     */
    public static void addCookie(HttpServletResponse resp, String name, 
                                 String value, String path, int maxAge) {
        resp.addCookie(createCookie(name, value, path, maxAge));
    }

    /**
     * 创建cookie
     * @param name
     * @param value
     * @param path
     * @param maxAge
     * @return
     */
    public static Cookie createCookie(String name, String value, 
                                      String path, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    /**
     * 设置响应头
     * @param response
     * @param name
     * @param value
     */
    public static void addHeader(HttpServletResponse response, String name, String value) {
        response.addHeader(name, value);
    }

    /**
     * Returns the string of web application context path
     *
     * @param request the HttpServletRequest
     * @return web application context path
     */
    public static String getContextPath(HttpServletRequest request) {
        return getContextPath(request.getServletContext());
    }

    /**
     * Returns the string of web application context path
     *
     * @param context the ServletContext
     * @return web application context path
     */
    public static String getContextPath(ServletContext context) {
        /*if (context.getMajorVersion() == 2 && context.getMinorVersion() < 5) {
            return null;
        }*/
        String contextPath = context.getContextPath();
        return StringUtils.isEmpty(contextPath) ? "/" : contextPath;
    }

    /**
     * 会话跟踪
     */
    public static void setSessionTrace(HttpServletResponse response, String token) {
        int maxAge = (token == null) ? 0 : 86400;
        //result.setAuthToken(token); // to response body
        WebUtils.addCookie(response, AUTH_COOKIE, token, "/", maxAge); // to cookie
        WebUtils.addHeader(response, AUTH_HEADER, token); // to header
    }

    /**
     * 会话跟踪
     */
    public static String getSessionTrace(HttpServletRequest request) {
        String authToken = request.getParameter(AUTH_PARAME); // from param
        if (authToken != null) {
            return authToken;
        }

        authToken = WebUtils.getCookie(request, AUTH_COOKIE); // from cooike
        if (authToken != null) {
            return authToken;
        }

        return WebUtils.getHeader(request, AUTH_HEADER); // from header;
    }

    // ----------------------------------private methods----------------------------------
    /**
     * to json string
     * @param data
     * @return
     */
    private static String toJson(Object data) {
        return (data instanceof CharSequence)
               ? data.toString()
               : Jsons.toJson(data);
    }

    private static void respStream(HttpServletResponse resp, long size,
                                   String filename, String charset) {
        filename = UrlCoder.encodeURIComponent(filename, charset); // others web browse
        //filename = new String(filename.getBytes(StandardCharsets.UTF_8), 
        //                      StandardCharsets.ISO_8859_1); // firefox web browse
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Length", Long.toString(size));
        resp.setHeader("Content-Disposition", "form-data; name=\"attachment\";filename=\"" + filename + "\"");
        resp.setCharacterEncoding(charset);
    }

    private static boolean isInvalidIp(String ip) {
        return StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip);
    }
}
