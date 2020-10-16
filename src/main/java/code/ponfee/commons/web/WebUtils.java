package code.ponfee.commons.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
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
import code.ponfee.commons.io.ByteOrderMarks;
import code.ponfee.commons.io.Files;
import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.util.Networks;
import code.ponfee.commons.util.UrlCoder;

/**
 * web工具类
 * 
 * @author Ponfee
 */
public final class WebUtils {

    private final static Pattern PATTERN_SUFFIX = Pattern.compile("\\S*[?]\\S*");
    public static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";

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

    public static final String COOKIE_ROOT_PATH = "/";

    /**
     * Gets the http servlet request parameters
     * the parameter value is {@link java.lang.String} type
     * if is array parameter, then the value is based-join on "," as String
     * 
     * @param request
     * @return Map<String, String>, the array param value use "," to join
     */
    public static Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String> params = new TreeMap<>();
        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
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

    private static final List<String> LOCAL_IPS = Arrays.asList(
        "127.0.0.1", "0:0:0:0:0:0:0:1", "::1"
    );
    /**
     * 获取客户端ip
     * @param req
     * @return
     */
    public static String getClientIp(HttpServletRequest req) {
        boolean invalid;
        String ip = req.getHeader("x-forwarded-for");
        if (invalid = isInvalidIp(ip)) {
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

        if (LOCAL_IPS.contains(ip)) {
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

    /**
     * 响应数据到请求客户端
     * @param resp
     * @param contentType
     * @param text
     * @param charset
     */
    public static void response(HttpServletResponse resp, 
                                ContentType contentType, 
                                String text, String charset) {
        resp.setContentType(contentType.value() + ";charset=" + charset);
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
        response(resp, ContentType.APPLICATION_JSON, toJson(data), charset);
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

    public static void download(HttpServletResponse resp,
                                byte[] data, String filename) {
        download(resp, data, filename, Files.UTF_8, false, false);
    }

    /**
    * Response as a stream attachment
    * 
    * @param resp     the HttpServletResponse
    * @param data     the resp byte array data
    * @param filename the resp attachment filename
    * @param charset  the attachment filename encoding
    * @param isGzip   {@code true} to use gzip compress
    * @param withBom  {@code true} with bom header
    */
    public static void download(HttpServletResponse resp, byte[] data,
                                String filename, String charset,
                                boolean isGzip, boolean withBom) {
        download(resp, new ByteArrayInputStream(data), filename, charset, isGzip, withBom);
    }

    /**
     * response to input stream
     * @param resp     the HttpServletResponse
     * @param input    the input stream
     * @param filename the resp attachment filename
     */
    public static void download(HttpServletResponse resp, 
                                InputStream input, String filename) {
        download(resp, input, filename, Files.UTF_8, false, false);
    }

    /**
     * Response as a stream attachment
     * 
     * @param resp     the HttpServletResponse
     * @param input    the input stream
     * @param filename the resp attachment filename
     * @param charset  the attachment filename encoding
     * @param isGzip   {@code true} to use gzip compress
     * @param withBom  {@code true} with bom header
     */
    public static void download(HttpServletResponse resp, InputStream input, 
                                String filename, String charset, 
                                boolean isGzip, boolean withBom) {
        try (InputStream in = input; 
             OutputStream out = resp.getOutputStream()
        ) {
            addStreamHeader(resp, filename, charset);

            byte[] bom = withBom ? ByteOrderMarks.get(Charset.forName(charset)) : null;
            if (isGzip) {
                resp.setHeader("Content-Encoding", "gzip");
                if (bom != null) {
                    GzipProcessor.compress(
                        new SequenceInputStream(new ByteArrayInputStream(bom), in), out
                    );
                } else {
                    GzipProcessor.compress(in, out);
                }
            } else {
                if (bom != null) {
                    out.write(bom);
                }
                IOUtils.copyLarge(in, out);
            }
        } catch (IOException e) {
            // cannot happened
            throw new RuntimeException("response input stream occur error", e);
        }
    }

    /**
     * 响应数据流，如图片数据
     * 
     * response(resp, image_byte_array, ContentType.IMAGE_JPEG.value(), false);
     * 
     * @param resp the HttpServletResponse
     * @param data the byte array data
     * @param contentType the content-type
     * @param isGzip whether to encode gzip
     */
    public static void response(HttpServletResponse resp, byte[] data,
                                ContentType contentType, boolean isGzip) {
        response(resp, new ByteArrayInputStream(data), contentType, isGzip);
    }

    /**
     * 响应数据流，如图片数据
     * 
     * response(resp, image_file_input_stream, ContentType.IMAGE_JPEG.value(), false);
     * 
     * @param resp  the HttpServletResponse
     * @param input the input stream
     * @param contentType the content-type
     * @param isGzip whether to encode gzip
     */
    public static void response(HttpServletResponse resp, InputStream input,
                                ContentType contentType, boolean isGzip) {
        try (OutputStream output = resp.getOutputStream()) {
            resp.setContentType(contentType.value());
            if (isGzip) {
                resp.setHeader("Content-Encoding", "gzip");
                GzipProcessor.compress(input, output);
            } else {
                IOUtils.copy(input, output);
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

    public static void delCookie(HttpServletRequest req, HttpServletResponse resp, String name) {
        delCookie(req, resp, COOKIE_ROOT_PATH, name);
    }

    /**
     * Delete the cookie by spec name
     * 
     * @param req  the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param path the cookie path
     * @param name the cookie name
     */
    public static void delCookie(HttpServletRequest req, 
                                 HttpServletResponse resp, 
                                 String path, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setPath(path); 
                cookie.setMaxAge(0);
                cookie.setValue("");
                resp.addCookie(cookie);
                return;
            }
        }
    }

    /**
     * 设置cookie
     * @param response
     * @param name
     * @param value
     */
    public static void addCookie(HttpServletResponse response, 
                                 String name, String value) {
        addCookie(response, name, value, COOKIE_ROOT_PATH, 24 * 60 * 60);
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
     * Creates a cookie
     * 
     * @param name
     * @param value
     * @param path
     * @param maxAge
     * @return an object of cookie
     */
    public static Cookie createCookie(String name, String value, 
                                      String path, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        //cookie.setHttpOnly(true);
        return cookie;
    }

    /**
     * 会话跟踪
     */
    public static void setSessionTrace(HttpServletResponse response, String token) {
        int maxAge = (token == null) ? 0 : 86400;
        //result.setAuthToken(token); // to response body
        WebUtils.addCookie(response, AUTH_COOKIE, token, COOKIE_ROOT_PATH, maxAge); // to cookie
        response.addHeader(AUTH_HEADER, token); // to header
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

        return request.getHeader(AUTH_HEADER); // from header;
    }

    public static String getContextPath(ServletContext context) {
        String contextPath = normalize(UrlCoder.decodeURI(context.getContextPath()));
        if ("/".equals(contextPath)) {
            contextPath = "";
        }
        return contextPath;
    }

    /**
     * Return the context path for the given request, detecting an include request
     * URL if called within a RequestDispatcher include.
     * <p>As the value returned by <code>request.getContextPath()</code> is <i>not</i>
     * decoded by the servlet container, this method will decode it.
     *
     * @param request current HTTP request
     * @return the context path
     */
    public static String getContextPath(HttpServletRequest request) {
        //String contextPath = request.getContextPath();
        //return StringUtils.isEmpty(contextPath) ? "/" : contextPath;

        String contextPath = (String) request.getAttribute(INCLUDE_CONTEXT_PATH_ATTRIBUTE);
        if (contextPath == null) {
            contextPath = request.getContextPath();
        }

        String encoding = request.getCharacterEncoding();
        contextPath = (encoding == null) 
                    ? UrlCoder.decodeURI(contextPath) 
                    : UrlCoder.decodeURI(contextPath, encoding);

        contextPath = normalize(contextPath);
        if ("/".equals(contextPath)) {
            contextPath = ""; // the normalize method will return a "/" and includes on Jetty, will also be a "/".
        }
        return contextPath;
    }

    /**
     * Normalize a relative URI path that may have relative values ("/./",
     * "/../", and so on ) it it.  <strong>WARNING</strong> - This method is
     * useful only for normalizing application-generated paths.  It does not
     * try to perform security checks for malicious input.
     * Normalize operations were was happily taken from org.apache.catalina.util.RequestUtil in
     * Tomcat trunk, r939305
     *
     * @param path Relative path to be normalized
     * @return normalized path
     */
    public static String normalize(String path) {
        return normalize(path, true);
    }

    /**
     * Normalize a relative URI path that may have relative values ("/./",
     * "/../", and so on ) it it.  <strong>WARNING</strong> - This method is
     * useful only for normalizing application-generated paths.  It does not
     * try to perform security checks for malicious input.
     * Normalize operations were was happily taken from org.apache.catalina.util.RequestUtil in
     * Tomcat trunk, r939305
     *
     * @param path             Relative path to be normalized
     * @param replaceBackSlash Should '\\' be replaced with '/'
     * @return normalized path
     */
    public static String normalize(String path, boolean replaceBackSlash) {
        if (path == null) {
            return null;
        }

        // Create a place for the normalized path
        String normalized = path;

        if (replaceBackSlash && normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');

        if (normalized.equals("/."))
            return "/";

        // Add a leading "/" if necessary
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                    normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                    normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                    normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);

    }

    // --------------------------------------------------------------------private methods
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

    private static void addStreamHeader(HttpServletResponse resp, String filename, String charset) {
        filename = UrlCoder.encodeURIComponent(filename, charset); // others web browse
        //filename = new String(filename.getBytes(charset), ISO_8859_1); // firefox web browse

        resp.setContentType(ContentType.APPLICATION_OCTET_STREAM.value());

        //resp.setHeader("Content-Length", Long.toString(length));

        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        //resp.setHeader("Content-Disposition", "form-data; name=\"attachment\"; filename=\"" + filename + "\"");

        //resp.setHeader("Set-Cookie", "fileDownload=true; path=/"); // JQuery $.fileDownload plugin

        resp.setCharacterEncoding(charset);
    }

    private static boolean isInvalidIp(String ip) {
        return StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip);
    }
}
