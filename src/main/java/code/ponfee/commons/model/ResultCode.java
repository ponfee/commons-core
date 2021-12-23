package code.ponfee.commons.model;

import code.ponfee.commons.http.HttpStatus;

import static java.net.HttpURLConnection.*;

/**
 * <pre>
 * Http response code: {@link java.net.HttpURLConnection#HTTP_OK}
 *  100 => "HTTP/1.1 100 Continue",
 *  101 => "HTTP/1.1 101 Switching Protocols",
 *  200 => "HTTP/1.1 200 OK",
 *  201 => "HTTP/1.1 201 Created",
 *  202 => "HTTP/1.1 202 Accepted",
 *  203 => "HTTP/1.1 203 Non-Authoritative Information",
 *  204 => "HTTP/1.1 204 No Content",
 *  205 => "HTTP/1.1 205 Reset Content",
 *  206 => "HTTP/1.1 206 Partial Content",
 *  300 => "HTTP/1.1 300 Multiple Choices",
 *  301 => "HTTP/1.1 301 Moved Permanently",
 *  302 => "HTTP/1.1 302 Found",
 *  303 => "HTTP/1.1 303 See Other",
 *  304 => "HTTP/1.1 304 Not Modified",
 *  305 => "HTTP/1.1 305 Use Proxy",
 *  307 => "HTTP/1.1 307 Temporary Redirect",
 *  400 => "HTTP/1.1 400 Bad Request",
 *  401 => "HTTP/1.1 401 Unauthorized",
 *  402 => "HTTP/1.1 402 Payment Required",
 *  403 => "HTTP/1.1 403 Forbidden",
 *  404 => "HTTP/1.1 404 Not Found",
 *  405 => "HTTP/1.1 405 Method Not Allowed",
 *  406 => "HTTP/1.1 406 Not Acceptable",
 *  407 => "HTTP/1.1 407 Proxy Authentication Required",
 *  408 => "HTTP/1.1 408 Request Time-out",
 *  409 => "HTTP/1.1 409 Conflict",
 *  410 => "HTTP/1.1 410 Gone",
 *  411 => "HTTP/1.1 411 Length Required",
 *  412 => "HTTP/1.1 412 Precondition Failed",
 *  413 => "HTTP/1.1 413 Request Entity Too Large",
 *  414 => "HTTP/1.1 414 Request-URI Too Large",
 *  415 => "HTTP/1.1 415 Unsupported Media Type",
 *  416 => "HTTP/1.1 416 Requested range not satisfiable",
 *  417 => "HTTP/1.1 417 Expectation Failed",
 *  500 => "HTTP/1.1 500 Internal Server Error",
 *  501 => "HTTP/1.1 501 Not Implemented",
 *  502 => "HTTP/1.1 502 Bad Gateway",
 *  503 => "HTTP/1.1 503 Service Unavailable",
 *  504 => "HTTP/1.1 504 Gateway Time-out"
 * 
 * 2开头-请求成功-表示成功处理了请求的状态代码。
 *  200 (成功)       服务器已成功处理了请求。通常，这表示服务器提供了请求的网页。
 *  201 (已创建)     请求成功并且服务器创建了新的资源。
 *  202 (已接受)     服务器已接受请求，但尚未处理。
 *  203 (非授权信息)  服务器已成功处理了请求，但返回的信息可能来自另一来源。
 *  204 (无内容)     服务器成功处理了请求，但没有返回任何内容。
 *  205 (重置内容)   没有新的内容，但浏览器应该重置它所显示的内容(用来强制浏览器清除表单输入内容)。
 *  206 (部分内容)   服务器成功处理了部分GET请求。
 * 
 * 3开头 (请求被重定向)表示要完成请求，需要进一步操作。通常，这些状态代码用来重定向。
 *  300   (多种选择)  针对请求，服务器可执行多种操作。服务器可根据请求者 (user agent) 选择一项操作，或提供操作列表供请求者选择。
 *  301   (永久移动)  请求的网页已永久移动到新位置。服务器返回此响应(对 GET 或 HEAD 请求的响应)时，会自动将请求者转到新位置。
 *  302   (临时移动)  服务器目前从不同位置的网页响应请求，但请求者应继续使用原有位置来进行以后的请求。
 *  303   (查看其他位置) 请求者应当对不同的位置使用单独的 GET 请求来检索响应时，服务器返回此代码。
 *  304   (未修改) 自从上次请求后，请求的网页未修改过。服务器返回此响应时，不会返回网页内容。
 *  305   (使用代理) 请求者只能使用代理访问请求的网页。如果服务器返回此响应，还表示请求者应使用代理。
 *  307   (临时重定向)  服务器目前从不同位置的网页响应请求，但请求者应继续使用原有位置来进行以后的请求。
 * 
 * 4开头 (请求错误)这些状态代码表示请求可能出错，妨碍了服务器的处理。
 *  400   (错误请求) 服务器不理解请求的语法。
 *  401   (未授权) 请求要求身份验证。对于需要登录的网页，服务器可能返回此响应。
 *  403   (禁止) 服务器拒绝请求。
 *  404   (未找到) 服务器找不到请求的网页。
 *  405   (方法禁用) 禁用请求中指定的方法。
 *  406   (不接受) 无法使用请求的内容特性响应请求的网页。
 *  407   (需要代理授权) 此状态代码与 401(未授权)类似，但指定请求者应当授权使用代理。
 *  408   (请求超时)  服务器等候请求时发生超时。
 *  409   (冲突)  服务器在完成请求时发生冲突。服务器必须在响应中包含有关冲突的信息。
 *  410   (已删除)  如果请求的资源已永久删除，服务器就会返回此响应。
 *  411   (需要有效长度) 服务器不接受不含有效内容长度标头字段的请求。
 *  412   (未满足前提条件) 服务器未满足请求者在请求中设置的其中一个前提条件。
 *  413   (请求实体过大) 服务器无法处理请求，因为请求实体过大，超出服务器的处理能力。
 *  414   (请求的 URI 过长) 请求的 URI(通常为网址)过长，服务器无法处理。
 *  415   (不支持的媒体类型) 请求的格式不受请求页面的支持。
 *  416   (请求范围不符合要求) 如果页面无法提供请求的范围，则服务器会返回此状态代码。
 *  417   (未满足期望值) 服务器未满足"期望"请求标头字段的要求。
 * 
 * 5开头(服务器错误)这些状态代码表示服务器在尝试处理请求时发生内部错误。这些错误可能是服务器本身的错误，而不是请求出错。
 *  500   (服务器内部错误)  服务器遇到错误，无法完成请求。
 *  501   (尚未实施) 服务器不具备完成请求的功能。例如，服务器无法识别请求方法时可能会返回此代码。
 *  502   (错误网关) 服务器作为网关或代理，从上游服务器收到无效响应。
 *  503   (服务不可用) 服务器目前无法使用(由于超载或停机维护)。通常，这只是暂时状态。
 *  504   (网关超时)  服务器作为网关或代理，但是没有及时从上游服务器收到请求。
 *  505   (HTTP 版本不受支持) 服务器不支持请求中所用的 HTTP 协议版本。
 *
 * 公用错误码区间[000 ~ 999]
 * </pre>
 * 
 * @see org.springframework.http.HttpStatus
 * @see code.ponfee.commons.http.HttpStatus
 * @see java.net.HttpURLConnection#HTTP_OK
 *  
 * @author Ponfee
 */
public final class ResultCode implements CodeMsg {

    private static final int SYS_CODE_MIN = 100;
    private static final int SYS_CODE_MAX = 599;
    private static final String SYS_ERROR = "Must in predefine reserved code [" + SYS_CODE_MIN + ", " + SYS_CODE_MAX + "]";
    private static final String BIZ_ERROR = "Cannot defined in reserved code [" + SYS_CODE_MIN + ", " + SYS_CODE_MAX + "]";

    /** 公用结果码 */
    public static final ResultCode OK                 = of0(HTTP_OK,                "OK");
    public static final ResultCode CREATED            = of0(HTTP_CREATED,           "已创建"); // POST
    public static final ResultCode ACCEPTED           = of0(HTTP_ACCEPTED,          "已接受，等待处理");
    public static final ResultCode NOT_AUTHORITATIVE  = of0(HTTP_NOT_AUTHORITATIVE, "非授权信息");
    public static final ResultCode NO_CONTENT         = of0(HTTP_NO_CONTENT,        "已处理，无返回内容"); // PUT, PATCH, DELETE
    public static final ResultCode REST_CONTENT       = of0(HTTP_RESET,             "重置内容");
    public static final ResultCode PARTIAL_CONTENT    = of0(HTTP_PARTIAL,           "部分内容");

    public static final ResultCode REDIRECT           = of0(302, "重定向");

    public static final ResultCode BAD_REQUEST        = of0(400, "参数错误");
    public static final ResultCode UNAUTHORIZED       = of0(401, "未授权");     // 
    public static final ResultCode FORBIDDEN          = of0(403, "拒绝访问");   // BLACKLIST
    public static final ResultCode NOT_FOUND          = of0(404, "资源未找到"); // GET return null
    public static final ResultCode NOT_ALLOWED        = of0(405, "方法不允许");
    public static final ResultCode NOT_ACCEPTABLE     = of0(406, "请求格式错误");
    public static final ResultCode REQUEST_TIMEOUT    = of0(408, "请求超时");
    public static final ResultCode OPS_CONFLICT       = of0(409, "数据不存在或版本冲突"); // DATABASE UPDATE DELETE FAIL
    public static final ResultCode UNSUPPORT_MEDIA    = of0(415, "格式不支持");

    public static final ResultCode SERVER_ERROR       = of0(500, "服务器错误");
    public static final ResultCode BAD_GATEWAY        = of0(502, "网关错误");
    public static final ResultCode SERVER_UNAVAILABLE = of0(503, "服务不可用");
    public static final ResultCode GATEWAY_TIMEOUT    = of0(504, "网关超时");
    public static final ResultCode SERVER_UNSUPPORTED = of0(505, "服务不支持");

    private final int code;
    private final boolean success;
    private final String msg;

    private ResultCode(int code, String msg) {
        this.code = code;
        this.success = HttpStatus.Series.valueOf(code) == HttpStatus.Series.SUCCESSFUL;
        this.msg = msg;
    }

    /**
     * inner create, only call in this class
     * use in assign the commons code 
     * @param code
     * @param msg
     * @return
     */
    private static ResultCode of0(int code, String msg) {
        if (code < SYS_CODE_MIN || code > SYS_CODE_MAX) {
            throw new IllegalArgumentException(SYS_ERROR);
        }
        return new ResultCode(code, msg);
    }

    /**
     * others place cannot set the code in commons code range[000 ~ 999]
     * 
     * @param code
     * @param msg
     * @return
     */
    public static ResultCode of(int code, String msg) {
        if (code >= SYS_CODE_MIN && code <= SYS_CODE_MAX) {
            throw new IllegalArgumentException(BIZ_ERROR);
        }
        return new ResultCode(code, msg);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String getMsg() {
        return msg;
    }

}
