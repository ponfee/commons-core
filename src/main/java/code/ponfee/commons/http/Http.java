package code.ponfee.commons.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Preconditions;

import code.ponfee.commons.io.Closeables;
import code.ponfee.commons.io.Files;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.util.Enums;

/**
 * <pre>
 *  Accept属于请求头， Content-Type属于实体头
 *     请求方的http报头结构：通用报头|请求报头|实体报头 
 *     响应方的http报头结构：通用报头|响应报头|实体报头
 *
 *  <p>
 *  请求报头有：Accept、Accept-Charset、Accept-Encoding、Accept-Language、Referer、
 *          Authorization、From、Host、If-Match、User-Agent、If-Modified-Since等
 *  Accept：告诉WEB服务器自己接受什么介质类型，*∕*表示任何类型，type∕*表示该类型下的所有子类型，type∕sub-type，如Accept(text/html)
 *
 *  <p>
 *  响应报头有：Age、Server、Accept-Ranges、Vary等
 *
 *  <p>
 *  实体报头有：Allow、Location、Content-Base、Content-Encoding、Content-Length、
 *          Content-Range、Content-MD5、Content-Type、Expires、Last-Modified等
 *  Content-Type：
 *     请求实体报头：浏览器告诉Web服务器自己发送的数据格式，如Content-Type(application/json，multipart/form-data，
 *                                                        application/x-www-form-urlencoded)
 *     响应实体报头：Web服务器告诉浏览器自己响应的消息格式，例如Content-Type(application/xml，application/json)
 * </pre>
 * http://www.atool.org/httptest.php<p>
 * 
 * Restful：https://www.cnblogs.com/pixy/p/4838268.html<p>
 * 
 * http工具类<p>
 * 
 * Spring RestTemplate<p>
 * Fluent Api<p>
 * HttpClient Api<p>
 * OK Http Api<p>
 * 
 * @author Ponfee
 */
public final class Http {

    private final String url; // url
    private final HttpMethod method; // 请求方法

    private final Map<String, String> headers = new HashMap<>(1);   // http请求头
    private final Map<String, Object> params  = new HashMap<>(1);   // http请求参数
    private final List<MimePart> parts        = new ArrayList<>(1); // http文件上传

    private String data; // 请求data
    private int connectTimeout = 1000 * 3; // 连接超时时间
    private int readTimeout = 1000 * 7; // 读取返回数据超时时间（socket timeout）
    private Boolean encode = Boolean.TRUE; // 是否编码
    private String contentType; // 请求内容类型
    private String contentCharset; // 请求内容编码
    private String accept; // 接收类型
    private SSLSocketFactory sslSocketFactory; // 走SSL/TSL通道

    // ----------------------------------------------------------response
    private Map<String, List<String>> respHeaders;
    private HttpStatus status;

    private Http(String url, HttpMethod method) {
        this.url = url;
        this.method = method;
    }

    // ----------------------------method--------------------------
    public static Http get(String url) {
        return new Http(url, HttpMethod.GET);
    }

    public static Http post(String url) {
        return new Http(url, HttpMethod.POST);
    }

    public static Http put(String url) {
        return new Http(url, HttpMethod.PUT);
    }

    public static Http head(String url) {
        return new Http(url, HttpMethod.HEAD);
    }

    public static Http delete(String url) {
        return new Http(url, HttpMethod.DELETE);
    }

    public static Http trace(String url) {
        return new Http(url, HttpMethod.TRACE);
    }

    public static Http options(String url) {
        return new Http(url, HttpMethod.OPTIONS);
    }

    public static Http of(String url, String method) {
        return of(url, Enums.ofIgnoreCase(HttpMethod.class, method, HttpMethod.GET));
    }

    public static Http of(String url, HttpMethod method) {
        return new Http(url, method);
    }

    // ----------------------------header--------------------------
    /**
     * 设置请求头
     * @param name
     * @param value
     * @return
     */
    public Http addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * 设置请求头
     * @param headers
     * @return
     */
    public Http addHeader(Map<String, String> headers) {
        if (MapUtils.isNotEmpty(headers)) {
            this.headers.putAll(headers);
        }
        return this;
    }

    // ----------------------------param--------------------------
    /**
     * 最终是拼接成queryString的形式追加到url（即作为get的http请求参数）
     * get方式会有编码等问题，推荐使用data方式传参数：{@link #data(Map)}
     * @param params
     * @return
     */
    public Http addParam(Map<String, ?> params) {
        this.params.putAll(params);
        return this;
    }

    public <T> Http addParam(String name, T value) {
        this.params.put(name, value);
        return this;
    }

    // ----------------------------data--------------------------
    /**
     * 发送到服务器的查询字符串或json串：name1=value1&name2=value2
     * 
     * @param params the http params
     * @return a reference to this object
     */
    public Http data(Map<String, ?> params) {
        return data(params, Files.UTF_8);
    }

    /**
     * 发送到服务器的查询字符串或json串：name1=value1&name2=value2
     * 
     * @param params the http params
     * @param charset the charset
     * @return a reference to this object
     */
    public Http data(Map<String, ?> params, String charset) {
        return data(HttpParams.buildParams(params, charset));
    }

    /**
     * HttpURLConnection.getOutputStream().write(data)
     * 
     * @param data the http body data
     * @return a reference to this object
     */
    public Http data(String data) {
        Preconditions.checkState(this.data == null, "http data are already set.");
        Preconditions.checkArgument(data != null && !data.isEmpty(), "data cannot be empty.");
        this.data = data;
        return this;
    }

    // ----------------------------part--------------------------
    public Http addPart(String formName, String fileName, Object mimePart) {
        return this.addPart(formName, fileName, null, mimePart);
    }

    /**
     * 文件上传
     * @param formName 表单名称
     * @param fileName 附件名称
     * @param partType 附件类型，value of the Content-Type part header
     * @param mimePart 上传数据
     * @return
     */
    public Http addPart(String formName, String fileName, 
                        String partType, Object mimePart) {
        this.parts.add(new MimePart(formName, fileName, partType, mimePart));
        return this;
    }

    // ----------------------------encode--------------------------
    /**
     * 编码url
     * @param encode
     * @return
     */
    public Http encode(Boolean encode) {
        this.encode = encode;
        return this;
    }

    // ----------------------------request contentType--------------------------
    /**
     * <pre>
     *  请求实体报头，发送信息至服务器时内容编码类型：
     *    multipart/form-data，application/x-www-form-urlencoded，
     *    application/json
     *  默认：application/x-www-form-urlencoded
     *  调用方式：contentType("application/json", "UTF-8")
     * </pre>
     * @param contentType
     * @param contentCharset
     * @return
     */
    public Http contentType(ContentType contentType, String contentCharset) {
        this.contentType = contentType.value();
        this.contentCharset = contentCharset;
        return this;
    }

    public Http contentType(ContentType contentType) {
        return this.contentType(contentType, Files.UTF_8);
    }

    // ----------------------------response accept--------------------------
    /**
     * 内容类型发送请求头，告诉服务器什么样的响应会接受返回
     * header("Accept", contentType)
     * @param contentType  application/json
     * @return
     */
    public Http accept(ContentType contentType) {
        this.accept = contentType.value();
        return this;
    }

    // --------------------------------timeout------------------------------
    /**
     * set connect timeout
     * @param seconds (s)
     * @return this
     */
    public Http connTimeoutSeconds(int seconds) {
        this.connectTimeout = seconds * 1000;
        return this;
    }

    /**
     * set read timeout
     * @param seconds (s)
     * @return this
     */
    public Http readTimeoutSeconds(int seconds) {
        this.readTimeout = seconds * 1000;
        return this;
    }

    // ----------------------------trust spec cert--------------------------
    /**
     * trust spec certificate
     * @param factory
     * @return
     */
    public Http setSSLSocketFactory(SSLSocketFactory factory) {
        this.sslSocketFactory = factory;
        return this;
    }

    public Http setSSLSocketFactory(SSLContext sslContext) {
        return setSSLSocketFactory(sslContext.getSocketFactory());
    }

    // --------------------------------request------------------------------
    public <T> T request(JavaType type) {
        return Jsons.fromJson(request(), type);
    }

    public <T> T request(Class<T> type) {
        return Jsons.fromJson(request(), type);
    }

    /**
     * 发送请求获取响应数据
     * @return
     */
    public String request() {
        HttpRequest request = request0();
        try {
            return request.body();
        } finally {
            disconnect(request);
        }
    }

    public void download(String filepath) {
        try (OutputStream out = new FileOutputStream(filepath)) {
            download(out);
        } catch (IOException e) {
            throw new HttpException("download error: " + filepath, e);
        }
    }

    public byte[] download() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        download(output);
        return output.toByteArray();
    }

    /**
     * http下载
     * @param output    output to stream of response data
     */
    //private static final Pattern FILENAME_PATTERN = Pattern.compile("(?i)^.*;.*filename=(.*)$");
    public void download(OutputStream output) {
        BufferedOutputStream bos = null;
        HttpRequest request = request0();
        try {
            if (HttpStatus.Series.valueOf(status) == HttpStatus.Series.SUCCESSFUL) {
                /*// 获取文件名
                String disposition = UrlCoder.decodeURIComponent(request.header("content-Disposition"));
                Matcher matcher = FILENAME_PATTERN.matcher(disposition);
                if (matcher.find()) {
                    String filename = matcher.group(1);
                }*/
                bos = new BufferedOutputStream(output);
                request.receive(bos);
            } else {
                throw new HttpException("request failed, status: " + request.code());
            }
        } finally {
            disconnect(request);
            Closeables.closeConsole(bos);
        }
    }

    // ------------------------------------------------------response headers
    public Map<String, List<String>> getRespHeaders() {
        return respHeaders;
    }

    public Map<String, String> getReqHeaders() {
        return headers;
    }

    public String[] getRespHeaders(String name) {
        if (respHeaders == null) {
            return null;
        }

        List<String> values = respHeaders.get(name);
        if (values == null) {
            return null;
        }
        return values.toArray(new String[values.size()]);
    }

    public String getRespHeader(String name) {
        if (respHeaders == null) {
            return null;
        }

        List<String> values = respHeaders.get(name);
        return (values == null || values.isEmpty())
               ? null : values.get(0);
    }

    public HttpStatus getStatus() {
        return status;
    }

    // ------------------------------------------------------private methods
    private HttpRequest request0() {
        HttpRequest request;
        switch (method) {
            case GET:
                request = HttpRequest.get(url, params, encode);
                break;
            case POST:
                request = HttpRequest.post(url, params, encode);
                break;
            case PUT:
                request = HttpRequest.put(url, params, encode);
                break;
            case HEAD:
                request = HttpRequest.head(url, params, encode);
                break;
            case DELETE:
                request = HttpRequest.delete(url, params, encode);
                break;
            case TRACE:
                request = HttpRequest.trace(url);
                break;
            case OPTIONS:
                request = HttpRequest.options(url);
                break;
            default:
                throw new UnsupportedOperationException("unsupported http method " + method.name());
        }

        request.connectTimeout(connectTimeout).readTimeout(readTimeout)
               .decompress(true).acceptGzipEncoding().headers(headers);

        if (!StringUtils.isEmpty(contentType)) {
            request.contentType(contentType, contentCharset);
        }

        if (!StringUtils.isEmpty(accept)) {
            request.accept(accept);
        }

        request.trustAllHosts();
        if (this.sslSocketFactory != null) {
            request.setSSLSocketFactory(this.sslSocketFactory);
        } else {
            request.trustAllCerts();
        }

        if (!StringUtils.isEmpty(data)) {
            request.send(data);
        }

        for (MimePart part : parts) {
            request.part(part.formName, part.fileName, part.partType, part.stream);
        }

        status = request.status();
        return request;
    }

    private void disconnect(HttpRequest request) {
        if (request != null) {
            this.respHeaders = request.headers(); // get the response headers
            try {
                request.disconnect();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /**
     * Http method
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, TRACE, OPTIONS
    }

    /**
     * File upload
     */
    private static final class MimePart {
        final String formName;    // 表单域字段名
        final String fileName;    // 文件名
        final String partType;    // 附件类型
        final InputStream stream; // 文件流

        MimePart(String formName, String fileName, String partType, Object mime) {
            if (mime instanceof byte[]) {
                this.stream = new ByteArrayInputStream((byte[]) mime);
            } else if (mime instanceof Byte[]) {
                this.stream = new ByteArrayInputStream(ArrayUtils.toPrimitive((Byte[]) mime));
            } else if (mime instanceof String || mime instanceof File) {
                File file = (mime instanceof File) ? (File) mime : new File((String) mime);
                try {
                    this.stream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            } else if (mime instanceof InputStream) {
                this.stream = (InputStream) mime;
            } else {
                throw new IllegalArgumentException("mime must be one of them: file, "
                                                 + "file path, byte array, input stream.");
            }

            this.formName = formName;
            this.fileName = fileName;
            this.partType = partType;
        }
    }

}
