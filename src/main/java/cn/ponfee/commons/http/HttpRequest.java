/*
 * Copyright (c) 2014 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 * 
 * <dependency>
 *   <groupId>com.github.kevinsawicki</groupId>
 *   <artifactId>http-request</artifactId>
 *   <version>6.0</version>
 * </dependency>
 */
package cn.ponfee.commons.http;

import cn.ponfee.commons.collect.Collects;
import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.util.ObjectUtils;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import static java.net.HttpURLConnection.*;
import static java.net.Proxy.Type.HTTP;

/**
 * A fluid interface for making HTTP requests using an underlying
 * {@link HttpURLConnection} (or sub-class).
 * <p>
 * Each instance supports making a single request and cannot be reused for
 * further requests.
 * 
 * @author Kevin Sawicki, Ponfee
 *
 * Reference from internet and with optimization 
 * https://github.com/kevinsawicki/http-request
 */
public class HttpRequest {

    /**
     * 'gzip' encoding header value
     */
    public static final String ENCODING_GZIP = "gzip";

    /**
     * 'Accept' header name
     */
    public static final String HEADER_ACCEPT = "Accept";

    /**
     * 'Accept-Charset' header name
     */
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";

    /**
     * 'Accept-Encoding' header name
     */
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * 'Authorization' header name
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * 'Cache-Control' header name
     */
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    /**
     * 'Content-Encoding' header name
     */
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";

    /**
     * 'Content-Length' header name
     */
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";

    /**
     * 'Content-Type' header name
     */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * 'Date' header name
     */
    public static final String HEADER_DATE = "Date";

    /**
     * 'ETag' header name
     */
    public static final String HEADER_ETAG = "ETag";

    /**
     * 'Expires' header name
     */
    public static final String HEADER_EXPIRES = "Expires";

    /**
     * 'If-None-Match' header name
     */
    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";

    /**
     * 'Last-Modified' header name
     */
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    /**
     * 'Location' header name
     */
    public static final String HEADER_LOCATION = "Location";

    /**
     * 'Proxy-Authorization' header name
     */
    public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";

    /**
     * 'Referer' header name
     */
    public static final String HEADER_REFERER = "Referer";

    /**
     * 'Server' header name
     */
    public static final String HEADER_SERVER = "Server";

    /**
     * 'User-Agent' header name
     */
    public static final String HEADER_USER_AGENT = "User-Agent";

    /**
     * 'DELETE' request method
     */
    public static final String METHOD_DELETE = "DELETE";

    /**
     * 'GET' request method
     */
    public static final String METHOD_GET = "GET";

    /**
     * 'HEAD' request method
     */
    public static final String METHOD_HEAD = "HEAD";

    /**
     * 'OPTIONS' options method
     */
    public static final String METHOD_OPTIONS = "OPTIONS";

    /**
     * 'POST' request method
     */
    public static final String METHOD_POST = "POST";

    /**
     * 'PUT' request method
     */
    public static final String METHOD_PUT = "PUT";

    /**
     * 'TRACE' request method
     */
    public static final String METHOD_TRACE = "TRACE";

    /**
     * 'charset' header value parameter
     */
    public static final String PARAM_CHARSET = "charset";

    private static final String BOUNDARY = "00content0boundary00";

    private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; boundary=" + BOUNDARY;

    private static final String CRLF = "\r\n";

    private static final String[] EMPTY_STRINGS = {};

    private static final HostnameVerifier TRUSTED_VERIFIER = (hostname, session) -> /*hostname.equalsIgnoreCase(session.getPeerHost())*/true;

    private static final SSLSocketFactory TRUSTED_FACTORY;
    static {
        try {
            SSLContext context = Providers.getSSLContext("TLS");

            context.init(null, new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // Intentionally left blank
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // Intentionally left blank
                    }
                }
            }, new SecureRandom(new SecureRandom(ObjectUtils.uuid()).generateSeed(20)));

            TRUSTED_FACTORY = context.getSocketFactory();

        } catch (GeneralSecurityException e) {
            throw new HttpException(
                new IOException("Security exception configuring SSL context", e)
            );
        }
    }

    private static String getValidCharset(String charset) {
        if (charset != null && charset.length() > 0) {
            return charset;
        } else {
            return Files.UTF_8;
        }
    }

    private static StringBuilder addPathSeparator(String baseUrl, StringBuilder result) {
        // Add trailing slash if the base URL doesn't have any path segments.
        //
        // The following test is checking for the last slash not being part of
        // the protocol to host separator: '://'.
        if (baseUrl.indexOf(':') + 2 == baseUrl.lastIndexOf('/')) {
            result.append('/');
        }
        return result;
    }

    private static StringBuilder addParamPrefix(String baseUrl, StringBuilder result) {
        // Add '?' if missing and add '&' if params already exist in base url
        int queryStart = baseUrl.indexOf('?');
        int lastChar = result.length() - 1;
        if (queryStart == -1) {
            result.append('?');
        } else if (queryStart < lastChar && baseUrl.charAt(lastChar) != '&') {
            result.append('&');
        }
        return result;
    }

    private static StringBuilder addParam(Object key, Object value, StringBuilder result) {
        if (value != null && value.getClass().isArray()) {
            value = Collects.toList(value);
        }

        if (value instanceof Iterable<?>) {
            Iterator<?> iterator = ((Iterable<?>) value).iterator();
            while (iterator.hasNext()) {
                result.append(key);
                result.append("[]=");
                Object element = iterator.next();
                if (element != null) {
                    result.append(element);
                }
                if (iterator.hasNext()) {
                    result.append("&");
                }
            }
        } else {
            result.append(key);
            result.append("=");
            if (value != null) {
                result.append(value);
            }
        }

        return result;
    }

    /**
     * Creates {@link HttpURLConnection HTTP connections} for
     * {@link URL urls}.
     */
    public interface ConnectionFactory {
        /**
         * Open an {@link HttpURLConnection} for the specified {@link URL}.
         * @throws IOException
         */
        HttpURLConnection create(URL url) throws IOException;

        /**
         * Open an {@link HttpURLConnection} for the specified {@link URL}
         * and {@link Proxy}.
         * @throws IOException
         */
        HttpURLConnection create(URL url, Proxy proxy) throws IOException;

        /**
         * A {@link ConnectionFactory} which uses the built-in
         * {@link URL#openConnection()}
         */
        ConnectionFactory DEFAULT = new ConnectionFactory() {
            @Override
            public HttpURLConnection create(URL url) throws IOException {
                return (HttpURLConnection) url.openConnection();
            }

            @Override
            public HttpURLConnection create(URL url, Proxy proxy) throws IOException {
                return (HttpURLConnection) url.openConnection(proxy);
            }
        };
    }

    private static ConnectionFactory connectionFactory = ConnectionFactory.DEFAULT;

    /**
     * Specify the {@link ConnectionFactory} used to create new requests.
     */
    public static void setConnectionFactory(ConnectionFactory cf) {
        connectionFactory = cf != null ? cf : ConnectionFactory.DEFAULT;
    }

    /**
     * Callback interface for reporting upload progress for a request.
     */
    public interface UploadProgress {
        /**
         * Callback invoked as data is uploaded by the request.
         *
         * @param uploaded The number of bytes already uploaded
         * @param total The total number of bytes that will be uploaded or -1 if
         *              the length is unknown.
         */
        void onUpload(long uploaded, long total);

        UploadProgress DEFAULT = (uploaded, total) -> {
            // do-non
        };
    }

    /**
     * Operation that handles executing a callback once complete and handling
     * nested exceptions
     *
     * @param <V>
     */
    private abstract static class Operation<V> implements Callable<V> {

        /**
         * Run operation
         *
         * @return result
         * @throws HttpException
         * @throws IOException
         */
        protected abstract V run() throws HttpException, IOException;

        /**
         * Operation complete callback
         *
         * @throws IOException
         */
        protected abstract void done() throws IOException;

        @Override
        public final V call() throws HttpException {
            try {
                return run();
            } catch (HttpException e) {
                throw e;
            } catch (Exception e) {
                throw new HttpException(e);
            } finally {
                try {
                    done();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    /**
     * Class that ensures a {@link Closeable} gets closed with proper exception
     * handling.
     *
     * @param <V>
     */
    private abstract static class CloseOperation<V> extends Operation<V> {

        private final Closeable closeable;

        private final boolean ignoreCloseExceptions;

        /**
         * Create closer for operation
         *
         * @param closeable
         * @param ignoreCloseExceptions
         */
        protected CloseOperation(Closeable closeable, boolean ignoreCloseExceptions) {
            this.closeable = closeable;
            this.ignoreCloseExceptions = ignoreCloseExceptions;
        }

        @Override
        protected void done() throws IOException {
            if (closeable instanceof Flushable) {
                ((Flushable) closeable).flush();
            }

            if (ignoreCloseExceptions) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace(); // ignored
                }
            } else {
                closeable.close();
            }
        }
    }

    /**
     * Class that and ensures a {@link Flushable} gets flushed with proper
     * exception handling.
     *
     * @param <V>
     */
    private abstract static class FlushOperation<V> extends Operation<V> {

        private final Flushable flushable;

        /**
         * Create flush operation
         *
         * @param flushable
         */
        protected FlushOperation(Flushable flushable) {
            this.flushable = flushable;
        }

        @Override
        protected void done() throws IOException {
            flushable.flush();
        }
    }

    /**
     * Request output stream
     */
    public static class RequestOutputStream extends BufferedOutputStream {

        private final CharsetEncoder encoder;

        /**
         * Create request output stream
         *
         * @param stream
         * @param charset
         * @param bufferSize
         */
        public RequestOutputStream(OutputStream stream, String charset, int bufferSize) {
            super(stream, bufferSize);

            encoder = Charset.forName(getValidCharset(charset)).newEncoder();
        }

        /**
         * Write string to stream
         *
         * @param value
         * @return this stream
         * @throws IOException
         */
        public RequestOutputStream write(String value) throws IOException {
            ByteBuffer bytes = encoder.encode(CharBuffer.wrap(value));
            super.write(bytes.array(), 0, bytes.limit());
            return this;
        }
    }

    /**
     * Encode the given URL as an ASCII {@link String}
     * <p>
     * This method ensures the path and query segments of the URL are properly
     * encoded such as ' ' characters being encoded to '%20' or any UTF-8
     * characters that are non-ASCII. No encoding of URLs is done by default by
     * the {@link HttpRequest} constructors and so if URL encoding is needed this
     * method should be called before calling the {@link HttpRequest} constructor.
     *
     * @param url
     * @return encoded URL
     * @throws HttpException
     */
    public static String encode(CharSequence url)
        throws HttpException {
        URL u;
        try {
            u = new URL(url.toString());
        } catch (IOException e) {
            throw new HttpException(e);
        }

        String host = u.getHost();
        int port = u.getPort();
        if (port != -1) {
            host = host + ':' + port;
        }

        try {
            String s = new URI(u.getProtocol(), host, u.getPath(), u.getQuery(), null)
                            .toASCIIString();
            int paramsStart = s.indexOf('?');
            if (paramsStart > 0 && paramsStart + 1 < s.length()) {
                s = s.substring(0, paramsStart + 1) + s.substring(paramsStart + 1).replace("+", "%2B");
            }
            return s;
        } catch (URISyntaxException e) {
            throw new HttpException(new IOException("Parsing URI failed", e));
        }
    }

    /**
     * Append given map as query parameters to the base URL
     * <p>
     * Each map entry's key will be a parameter name and the value's
     * {@link Object#toString()} will be the parameter value.
     *
     * @param url
     * @param params
     * @return URL with appended query params
     */
    public static String append(CharSequence url, Map<?, ?> params) {
        String baseUrl = url.toString();
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder result = new StringBuilder(baseUrl);

        addPathSeparator(baseUrl, result);
        addParamPrefix(baseUrl, result);

        Entry<?, ?> entry;
        Iterator<?> iterator = params.entrySet().iterator();
        entry = (Entry<?, ?>) iterator.next();
        addParam(entry.getKey().toString(), entry.getValue(), result);

        while (iterator.hasNext()) {
            result.append('&');
            entry = (Entry<?, ?>) iterator.next();
            addParam(entry.getKey().toString(), entry.getValue(), result);
        }

        return result.toString();
    }

    /**
     * Append given name/value pairs as query parameters to the base URL
     * <p>
     * The params argument is interpreted as a sequence of name/value pairs so the
     * given number of params must be divisible by 2.
     *
     * @param url
     * @param params name/value pairs
     * @return URL with appended query params
     */
    public static String append(CharSequence url, Object... params) {
        String baseUrl = url.toString();
        if (params == null || params.length == 0) {
            return baseUrl;
        }

        if ((params.length & 0x01) == 1) {
            throw new IllegalArgumentException("Must specify an even number of parameter names/values");
        }

        StringBuilder result = new StringBuilder(baseUrl);

        addPathSeparator(baseUrl, result);
        addParamPrefix(baseUrl, result);

        addParam(params[0], params[1], result);

        for (int i = 2; i < params.length; i += 2) {
            result.append('&');
            addParam(params[i], params[i + 1], result);
        }

        return result.toString();
    }

    /**
     * Start a 'GET' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest get(CharSequence url) throws HttpException {
        return new HttpRequest(url, METHOD_GET);
    }

    /**
     * Start a 'GET' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest get(URL url) throws HttpException {
        return new HttpRequest(url, METHOD_GET);
    }

    /**
     * Start a 'GET' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params The query parameters to include as part of the baseUrl
     * @param encode true to encode the full URL
     *
     * @see #append(CharSequence, Map)
     * @see #encode(CharSequence)
     *
     * @return request
     */
    public static HttpRequest get(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    /**
     * Start a 'GET' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode true to encode the full URL
     * @param params the name/value query parameter pairs to 
     *               include as part of the baseUrl
     *
     * @see #append(CharSequence, Object...)
     * @see #encode(CharSequence)
     *
     * @return request
     */
    public static HttpRequest get(CharSequence baseUrl, boolean encode, Object... params) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    /**
     * Start a 'POST' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest post(CharSequence url) throws HttpException {
        return new HttpRequest(url, METHOD_POST);
    }

    /**
     * Start a 'POST' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest post(URL url) throws HttpException {
        return new HttpRequest(url, METHOD_POST);
    }

    /**
     * Start a 'POST' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params the query parameters to include as part of the baseUrl
     * @param encode true to encode the full URL
     *
     * @see #append(CharSequence, Map)
     * @see #encode(CharSequence)
     *
     * @return request
     */
    public static HttpRequest post(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
        String url = append(baseUrl, params);
        return post(encode ? encode(url) : url);
    }

    /**
     * Start a 'POST' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode  true to encode the full URL
     * @param params  the name/value query parameter pairs to 
     *                include as part of the baseUrl
     *
     * @see #append(CharSequence, Object...)
     * @see #encode(CharSequence)
     *
     * @return request
     */
    public static HttpRequest post(CharSequence baseUrl, boolean encode, Object... params) {
        String url = append(baseUrl, params);
        return post(encode ? encode(url) : url);
    }

    /**
     * Start a 'PUT' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest put(CharSequence url) throws HttpException {
        return new HttpRequest(url, METHOD_PUT);
    }

    /**
     * Start a 'PUT' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest put(URL url) throws HttpException {
        return new HttpRequest(url, METHOD_PUT);
    }

    /**
     * Start a 'PUT' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params the query parameters to include as part of the baseUrl
     * @param encode true to encode the full URL
     *
     * @see #append(CharSequence, Map)
     * @see #encode(CharSequence)
     *
     * @return request
     */
    public static HttpRequest put(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
        String url = append(baseUrl, params);
        return put(encode ? encode(url) : url);
    }

    /**
     * Start a 'PUT' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode true to encode the full URL
     * @param params the name/value query parameter pairs to 
     *               include as part of the baseUrl
     *
     * @see #append(CharSequence, Object...)
     * @see #encode(CharSequence)
     *
     * @return request
     */
    public static HttpRequest put(CharSequence baseUrl, boolean encode, Object... params) {
        String url = append(baseUrl, params);
        return put(encode ? encode(url) : url);
    }

    /**
     * Start a 'DELETE' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest delete(CharSequence url) throws HttpException {
        return new HttpRequest(url, METHOD_DELETE);
    }

    /**
     * Start a 'DELETE' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest delete(URL url) throws HttpException {
        return new HttpRequest(url, METHOD_DELETE);
    }

    /**
     * Start a 'DELETE' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params The query parameters to include as part of the baseUrl
     * @param encode true to encode the full URL
     *
     * @see #append(CharSequence, Map)
     * @see #encode(CharSequence)
     *
     * @return request
     */
    public static HttpRequest delete(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
        String url = append(baseUrl, params);
        return delete(encode ? encode(url) : url);
    }

    /**
     * Start a 'DELETE' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode  true to encode the full URL
     * @param params  the name/value query parameter pairs to 
     *                include as part of the baseUrl
     *
     * @see #append(CharSequence, Object...)
     * @see #encode(CharSequence)
     *
     * @return request
     */
    public static HttpRequest delete(CharSequence baseUrl, boolean encode, Object... params) {
        String url = append(baseUrl, params);
        return delete(encode ? encode(url) : url);
    }

    /**
     * Start a 'HEAD' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest head(CharSequence url) throws HttpException {
        return new HttpRequest(url, METHOD_HEAD);
    }

    /**
     * Start a 'HEAD' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest head(URL url) throws HttpException {
        return new HttpRequest(url, METHOD_HEAD);
    }

    /**
     * Start a 'HEAD' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params The query parameters to include as part of the baseUrl
     * @param encode true to encode the full URL
     * @see #append(CharSequence, Map)
     * @see #encode(CharSequence)
     * @return request
     */
    public static HttpRequest head(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
        String url = append(baseUrl, params);
        return head(encode ? encode(url) : url);
    }

    /**
     * Start a 'GET' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode true to encode the full URL
     * @param params the name/value query parameter pairs to include 
     *               as part of the baseUrl
     * @see #append(CharSequence, Object...)
     * @see #encode(CharSequence)
     * @return request
     */
    public static HttpRequest head(CharSequence baseUrl,
                                   boolean encode, Object... params) {
        String url = append(baseUrl, params);
        return head(encode ? encode(url) : url);
    }

    /**
     * Start an 'OPTIONS' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest options(CharSequence url)
        throws HttpException {
        return new HttpRequest(url, METHOD_OPTIONS);
    }

    /**
     * Start an 'OPTIONS' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest options(URL url) throws HttpException {
        return new HttpRequest(url, METHOD_OPTIONS);
    }

    /**
     * Start a 'TRACE' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest trace(CharSequence url)
        throws HttpException {
        return new HttpRequest(url, METHOD_TRACE);
    }

    /**
     * Start a 'TRACE' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpException
     */
    public static HttpRequest trace(URL url) throws HttpException {
        return new HttpRequest(url, METHOD_TRACE);
    }

    /**
     * Set the 'http.keepAlive' property to the given value.
     * <p>
     * This setting will apply to all requests.
     *
     * @param keepAlive
     */
    public static void keepAlive(boolean keepAlive) {
        setProperty("http.keepAlive", Boolean.toString(keepAlive));
    }

    /**
     * Set the 'http.maxConnections' property to the given value.
     * <p>
     * This setting will apply to all requests.
     *
     * @param maxConnections
     */
    public static void maxConnections(int maxConnections) {
        setProperty("http.maxConnections", Integer.toString(maxConnections));
    }

    /**
     * Set the 'http.proxyHost' and 'https.proxyHost' properties to the given host
     * value.
     * <p>
     * This setting will apply to all requests.
     *
     * @param host
     */
    public static void proxyHost(String host) {
        setProperty("http.proxyHost", host);
        setProperty("https.proxyHost", host);
    }

    /**
     * Set the 'http.proxyPort' and 'https.proxyPort' properties to the given port
     * number.
     * <p>
     * This setting will apply to all requests.
     *
     * @param port
     */
    public static void proxyPort(int port) {
        String portValue = Integer.toString(port);
        setProperty("http.proxyPort", portValue);
        setProperty("https.proxyPort", portValue);
    }

    /**
     * Set the 'http.nonProxyHosts' property to the given host values.
     * <p>
     * Hosts will be separated by a '|' character.
     * <p>
     * This setting will apply to all requests.
     *
     * @param hosts
     */
    public static void nonProxyHosts(String... hosts) {
        if (hosts != null && hosts.length > 0) {
            StringBuilder separated = new StringBuilder();
            int last = hosts.length - 1;
            for (int i = 0; i < last; i++) {
                separated.append(hosts[i]).append('|');
            }
            separated.append(hosts[last]);
            setProperty("http.nonProxyHosts", separated.toString());
        } else {
            setProperty("http.nonProxyHosts", null);
        }
    }

    /**
     * Set property to given value.
     * <p>
     * Specifying a null value will cause the property to be cleared
     *
     * @param name
     * @param value
     * @return previous value
     */
    private static String setProperty(String name, String value) {
        PrivilegedAction<String> action;
        if (value != null) {
            action = () -> System.setProperty(name, value);
        } else {
            action = () -> System.clearProperty(name);
        }
        return AccessController.doPrivileged(action);
    }

    private HttpURLConnection connection = null;

    private final URL url;

    private final String requestMethod;

    private RequestOutputStream output;

    private boolean multipart;

    private boolean form;

    private boolean ignoreCloseExceptions = true;

    private boolean decompress = false;

    private int bufferSize = 8192;

    private long totalSize = -1;

    private long totalWritten = 0;

    private String httpProxyHost;

    private int httpProxyPort;

    private UploadProgress progress = UploadProgress.DEFAULT;

    /**
     * Create HTTP connection wrapper
     *
     * @param url Remote resource URL.
     * @param method HTTP request method (e.g., "GET", "POST").
     * @throws HttpException
     */
    public HttpRequest(CharSequence url, String method) throws HttpException {
        try {
            this.url = new URL(url.toString());
        } catch (MalformedURLException e) {
            throw new HttpException(e);
        }
        this.requestMethod = method;
    }

    /**
     * Create HTTP connection wrapper
     *
     * @param url Remote resource URL.
     * @param method HTTP request method (e.g., "GET", "POST").
     * @throws HttpException
     */
    public HttpRequest(URL url, String method)
        throws HttpException {
        this.url = url;
        this.requestMethod = method;
    }

    private Proxy createProxy() {
        return new Proxy(HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort));
    }

    private HttpURLConnection createConnection() {
        try {
            HttpURLConnection conn;
            if (httpProxyHost != null) {
                conn = connectionFactory.create(url, createProxy());
            } else {
                conn = connectionFactory.create(url);
            }
            conn.setRequestMethod(requestMethod);
            return conn;
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    @Override
    public String toString() {
        return method() + ' ' + url();
    }

    /**
     * Get underlying connection
     *
     * @return connection
     */
    public HttpURLConnection getConnection() {
        if (connection == null) {
            connection = createConnection();
        }
        return connection;
    }

    /**
     * Set whether or not to ignore exceptions that occur from calling
     * {@link Closeable#close()}
     * <p>
     * The default value of this setting is <code>true</code>
     *
     * @param ignore
     * @return this request
     */
    public HttpRequest ignoreCloseExceptions(boolean ignore) {
        ignoreCloseExceptions = ignore;
        return this;
    }

    /**
     * Get whether or not exceptions thrown by {@link Closeable#close()} are
     * ignored
     *
     * @return true if ignoring, false if throwing
     */
    public boolean ignoreCloseExceptions() {
        return ignoreCloseExceptions;
    }

    /**
     * Get the status code of the response
     *
     * @return the response code
     * @throws HttpException
     */
    public int code() throws HttpException {
        try {
            closeOutput();
            return getConnection().getResponseCode();
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    /**
     * Set the value of the given {@link AtomicInteger} to the status code of the
     * response
     *
     * @param output
     * @return this request
     * @throws HttpException
     */
    public HttpRequest code(AtomicInteger output) throws HttpException {
        output.set(code());
        return this;
    }

    /**
     * Is the response code a 200 OK?
     *
     * @return true if 200, false otherwise
     * @throws HttpException
     */
    public boolean ok() throws HttpException {
        return HTTP_OK == code();
    }

    /**
     * Is the response code a 201 Created?
     *
     * @return true if 201, false otherwise
     * @throws HttpException
     */
    public boolean created() throws HttpException {
        return HTTP_CREATED == code();
    }

    /**
     * Is the response code a 204 No Content?
     *
     * @return true if 204, false otherwise
     * @throws HttpException
     */
    public boolean noContent() throws HttpException {
        return HTTP_NO_CONTENT == code();
    }

    /**
     * Is the response code a 500 Internal Server Error?
     *
     * @return true if 500, false otherwise
     * @throws HttpException
     */
    public boolean serverError() throws HttpException {
        return HTTP_INTERNAL_ERROR == code();
    }

    /**
     * Is the response code a 400 Bad Request?
     *
     * @return true if 400, false otherwise
     * @throws HttpException
     */
    public boolean badRequest() throws HttpException {
        return HTTP_BAD_REQUEST == code();
    }

    /**
     * Is the response code a 404 Not Found?
     *
     * @return true if 404, false otherwise
     * @throws HttpException
     */
    public boolean notFound() throws HttpException {
        return HTTP_NOT_FOUND == code();
    }

    /**
     * Is the response code a 304 Not Modified?
     *
     * @return true if 304, false otherwise
     * @throws HttpException
     */
    public boolean notModified() throws HttpException {
        return HTTP_NOT_MODIFIED == code();
    }

    /**
     * Gets the response status enum
     * 
     * @return a enum for response http status
     */
    public HttpStatus status() {
        return HttpStatus.valueOf(code());
    }

    /**
     * Get status message of the response
     * @return message   OK
     * @throws HttpException
     */
    public String message() throws HttpException {
        try {
            closeOutput();
            return getConnection().getResponseMessage();
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    /**
     * Disconnect the connection
     *
     * @return this request
     */
    public HttpRequest disconnect() {
        getConnection().disconnect();
        return this;
    }

    /**
     * Set chunked streaming mode to the given size
     *
     * @param size
     * @return this request
     */
    public HttpRequest chunk(int size) {
        getConnection().setChunkedStreamingMode(size);
        return this;
    }

    /**
     * Set the size used when buffering and copying between streams
     * <p>
     * This size is also used for send and receive buffers created for both char
     * and byte arrays
     * <p>
     * The default buffer size is 8,192 bytes
     *
     * @param size
     * @return this request
     */
    public HttpRequest bufferSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("size must be greater than zero");
        }
        bufferSize = size;
        return this;
    }

    /**
     * Get the configured buffer size
     * <p>
     * The default buffer size is 8,192 bytes
     *
     * @return buffer size
     */
    public int bufferSize() {
        return bufferSize;
    }

    /**
     * Set whether or not the response body should be automatically decompress
     * when read from.
     * <p>
     * This will only affect requests that have the 'Content-Encoding' response
     * header set to 'gzip'.
     * <p>
     * This causes all receive methods to use a {@link GZIPInputStream} when
     * applicable so that higher level streams and readers can read the data
     * decompress.
     * <p>
     * Setting this option does not cause any request headers to be set
     * automatically so {@link #acceptGzipEncoding()} should be used in
     * conjunction with this setting to tell the server to gzip the response.
     *
     * @param decompress
     * @return this request
     */
    public HttpRequest decompress(boolean decompress) {
        this.decompress = decompress;
        return this;
    }

    /**
     * Create byte array output stream
     *
     * @return stream
     */
    protected ByteArrayOutputStream byteStream() {
        int size = contentLength();
        if (size > 0) {
            return new ByteArrayOutputStream(size);
        } else {
            return new ByteArrayOutputStream();
        }
    }

    /**
     * Get response as {@link String} in given character set
     * <p>
     * This will fall back to using the UTF-8 character set if the given charset
     * is null
     *
     * @param charset
     * @return string
     * @throws HttpException
     */
    public String body(String charset) throws HttpException {
        ByteArrayOutputStream output = byteStream();
        try {
            copy(buffer(), output);
            return output.toString(getValidCharset(charset));
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    /**
     * Get response as {@link String} using character set returned from
     * {@link #charset()}
     *
     * @return string
     * @throws HttpException
     */
    public String body() throws HttpException {
        return body(charset());
    }

    /**
     * Get the response body as a {@link String} and set it as the value of the
     * given reference.
     *
     * @param output
     * @return this request
     * @throws HttpException
     */
    public HttpRequest body(AtomicReference<String> output) 
        throws HttpException {
        output.set(body());
        return this;
    }

    /**
     * Get the response body as a {@link String} and set it as the value of the
     * given reference.
     *
     * @param output
     * @param charset
     * @return this request
     * @throws HttpException
     */
    public HttpRequest body(AtomicReference<String> output, String charset) 
        throws HttpException {
        output.set(body(charset));
        return this;
    }

    /**
     * Is the response body empty?
     *
     * @return true if the Content-Length response header is 0, false otherwise
     * @throws HttpException
     */
    public boolean isBodyEmpty() throws HttpException {
        return contentLength() == 0;
    }

    /**
     * Get response as byte array
     *
     * @return byte array
     */
    public byte[] bytes() {
        ByteArrayOutputStream output = byteStream();
        copy(buffer(), output);
        return output.toByteArray();
    }

    /**
     * Get response in a buffered stream
     *
     * @see #bufferSize(int)
     * @return stream
     * @throws HttpException
     */
    public BufferedInputStream buffer() throws HttpException {
        return new BufferedInputStream(stream(), bufferSize);
    }

    /**
     * Get stream to response body
     *
     * @return stream
     * @throws HttpException
     */
    public InputStream stream() throws HttpException {
        InputStream stream;
        if (code() < HTTP_BAD_REQUEST) {
            try {
                stream = getConnection().getInputStream();
            } catch (IOException e) {
                throw new HttpException(e);
            }
        } else {
            stream = getConnection().getErrorStream();
            if (stream == null) {
                try {
                    stream = getConnection().getInputStream();
                } catch (IOException e) {
                    if (contentLength() > 0) {
                        throw new HttpException(e);
                    } else {
                        stream = new ByteArrayInputStream(new byte[0]);
                    }
                }
            }
        }

        if (!decompress || !ENCODING_GZIP.equals(contentEncoding())) {
            return stream;
        } else {
            try {
                return new GZIPInputStream(stream);
            } catch (IOException e) {
                throw new HttpException(e);
            }
        }
    }

    /**
     * Get reader to response body using given character set.
     * <p>
     * This will fall back to using the UTF-8 character set if the given charset
     * is null
     *
     * @param charset
     * @return reader
     * @throws HttpException
     */
    public InputStreamReader reader(String charset)
        throws HttpException {
        try {
            return new InputStreamReader(stream(), getValidCharset(charset));
        } catch (UnsupportedEncodingException e) {
            throw new HttpException(e);
        }
    }

    /**
     * Get reader to response body using the character set returned from
     * {@link #charset()}
     *
     * @return reader
     * @throws HttpException
     */
    public InputStreamReader reader() throws HttpException {
        return reader(charset());
    }

    /**
     * Get buffered reader to response body using the given character set r and
     * the configured buffer size
     *
     *
     * @see #bufferSize(int)
     * @param charset
     * @return reader
     * @throws HttpException
     */
    public BufferedReader bufferedReader(String charset)
        throws HttpException {
        return new BufferedReader(reader(charset), bufferSize);
    }

    /**
     * Get buffered reader to response body using the character set returned from
     * {@link #charset()} and the configured buffer size
     *
     * @see #bufferSize(int)
     * @return reader
     * @throws HttpException
     */
    public BufferedReader bufferedReader() throws HttpException {
        return bufferedReader(charset());
    }

    /**
     * Stream response body to file
     *
     * @param file
     * @return this request
     * @throws HttpException
     */
    public HttpRequest receive(File file) throws HttpException {
        OutputStream output;
        try {
            output = new BufferedOutputStream(new FileOutputStream(file), bufferSize);
        } catch (FileNotFoundException e) {
            throw new HttpException(e);
        }

        return new CloseOperation<HttpRequest>(output, ignoreCloseExceptions) {
            @Override
            protected HttpRequest run() throws HttpException {
                return receive(output);
            }
        }.call();
    }

    /**
     * Stream response to given output stream
     *
     * @param output
     * @return this request
     */
    public HttpRequest receive(OutputStream output) {
        return copy(buffer(), output);
    }

    /**
     * Stream response to given print stream
     *
     * @param output
     * @return this request
     * @throws HttpException
     */
    public HttpRequest receive(PrintStream output) throws HttpException {
        return receive((OutputStream) output);
    }

    /**
     * Receive response into the given appendable
     *
     * @param appendable
     * @return this request
     * @throws HttpException
     */
    public HttpRequest receive(Appendable appendable) throws HttpException {
        BufferedReader reader = bufferedReader();

        return new CloseOperation<HttpRequest>(reader, ignoreCloseExceptions) {
            @Override
            public HttpRequest run() throws IOException {
                CharBuffer buffer = CharBuffer.allocate(bufferSize);
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    buffer.rewind();
                    appendable.append(buffer, 0, read);
                    buffer.rewind();
                }
                return HttpRequest.this;
            }
        }.call();
    }

    /**
     * Receive response into the given writer
     *
     * @param writer
     * @return this request
     * @throws HttpException
     */
    public HttpRequest receive(Writer writer) throws HttpException {
        BufferedReader reader = bufferedReader();

        return new CloseOperation<HttpRequest>(reader, ignoreCloseExceptions) {
            @Override
            public HttpRequest run() {
                return copy(reader, writer);
            }
        }.call();
    }

    /**
     * Set read timeout on connection to given value
     *
     * @param timeout
     * @return this request
     */
    public HttpRequest readTimeout(int timeout) {
        getConnection().setReadTimeout(timeout);
        return this;
    }

    /**
     * Set connect timeout on connection to given value
     *
     * @param timeout
     * @return this request
     */
    public HttpRequest connectTimeout(int timeout) {
        getConnection().setConnectTimeout(timeout);
        return this;
    }

    /**
     * Set header name to given value
     *
     * @param name
     * @param value
     * @return this request
     */
    public HttpRequest header(String name, String value) {
        getConnection().setRequestProperty(name, value);
        return this;
    }

    /**
     * Set header name to given value
     *
     * @param name
     * @param value
     * @return this request
     */
    public HttpRequest header(String name, Number value) {
        return header(name, value != null ? value.toString() : null);
    }

    /**
     * Set all headers found in given map where the keys are the header names and
     * the values are the header values
     *
     * @param headers
     * @return this request
     */
    public HttpRequest headers(Map<String, String> headers) {
        if (!headers.isEmpty()) {
            for (Entry<String, String> header : headers.entrySet()) {
                header(header);
            }
        }
        return this;
    }

    /**
     * Set header to have given entry's key as the name and value as the value
     *
     * @param header
     * @return this request
     */
    public HttpRequest header(Entry<String, String> header) {
        return header(header.getKey(), header.getValue());
    }

    /**
     * Get a response header
     *
     * @param name
     * @return response header
     * @throws HttpException
     */
    public String header(String name) throws HttpException {
        closeOutputQuietly();
        return getConnection().getHeaderField(name);
    }

    /**
     * Get all the response headers
     *
     * @return map of response header names to their value(s)
     * @throws HttpException
     */
    public Map<String, List<String>> headers() throws HttpException {
        closeOutputQuietly();
        return getConnection().getHeaderFields();
    }

    /**
     * Get a date header from the response falling back to returning -1 if the
     * header is missing or parsing fails
     *
     * @param name
     * @return date, -1 on failures
     * @throws HttpException
     */
    public long dateHeader(String name) throws HttpException {
        return dateHeader(name, -1L);
    }

    /**
     * Get a date header from the response falling back to returning the given
     * default value if the header is missing or parsing fails
     *
     * @param name
     * @param defaultValue
     * @return date, default value on failures
     * @throws HttpException
     */
    public long dateHeader(String name, long defaultValue) throws HttpException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldDate(name, defaultValue);
    }

    /**
     * Get an integer header from the response falling back to returning -1 if the
     * header is missing or parsing fails
     *
     * @param name
     * @return header value as an integer, -1 when missing or parsing fails
     * @throws HttpException
     */
    public int intHeader(String name) throws HttpException {
        return intHeader(name, -1);
    }

    /**
     * Get an integer header value from the response falling back to the given
     * default value if the header is missing or if parsing fails
     *
     * @param name
     * @param defaultValue
     * @return header value as an integer, default value when missing or parsing
     *         fails
     * @throws HttpException
     */
    public int intHeader(String name, int defaultValue) 
        throws HttpException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldInt(name, defaultValue);
    }

    /**
     * Get all values of the given header from the response
     *
     * @param name
     * @return non-null but possibly empty array of {@link String} header values
     */
    public String[] headers(String name) {
        Map<String, List<String>> headers = headers();
        if (headers == null || headers.isEmpty()) {
            return EMPTY_STRINGS;
        }

        List<String> values = headers.get(name);
        if (values != null && !values.isEmpty()) {
            return values.toArray(new String[0]);
        } else {
            return EMPTY_STRINGS;
        }
    }

    /**
     * Get parameter with given name from header value in response
     *
     * @param headerName
     * @param paramName
     * @return parameter value or null if missing
     */
    public String parameter(String headerName, String paramName) {
        return getParam(header(headerName), paramName);
    }

    /**
     * Get all parameters from header value in response
     * <p>
     * This will be all key=value pairs after the first ';' that are separated by
     * a ';'
     *
     * @param headerName
     * @return non-null but possibly empty map of parameter headers
     */
    public Map<String, String> parameters(String headerName) {
        return getParams(header(headerName));
    }

    /**
     * Get parameter values from header value
     *
     * @param header
     * @return parameter value or null if none
     */
    protected Map<String, String> getParams(String header) {
        if (header == null || header.length() == 0) {
            return Collections.emptyMap();
        }

        int headerLength = header.length();
        int start = header.indexOf(';') + 1;
        if (start == 0 || start == headerLength) {
            return Collections.emptyMap();
        }

        int end = header.indexOf(';', start);
        if (end == -1) {
            end = headerLength;
        }

        Map<String, String> params = new LinkedHashMap<>();
        while (start < end) {
            int nameEnd = header.indexOf('=', start);
            if (nameEnd != -1 && nameEnd < end) {
                String name = header.substring(start, nameEnd).trim();
                if (name.length() > 0) {
                    String value = header.substring(nameEnd + 1, end).trim();
                    int length = value.length();
                    if (length != 0) {
                        if (length > 2 && '"' == value.charAt(0) 
                            && '"' == value.charAt(length - 1)) {
                            params.put(name, value.substring(1, length - 1));
                        } else {
                            params.put(name, value);
                        }
                    }
                }
            }

            start = end + 1;
            end = header.indexOf(';', start);
            if (end == -1) {
                end = headerLength;
            }
        }

        return params;
    }

    /**
     * Get parameter value from header value
     *
     * @param value
     * @param paramName
     * @return parameter value or null if none
     */
    protected String getParam(String value, String paramName) {
        if (value == null || value.length() == 0) {
            return null;
        }

        int length = value.length();
        int start = value.indexOf(';') + 1;
        if (start == 0 || start == length) {
            return null;
        }

        int end = value.indexOf(';', start);
        if (end == -1) {
            end = length;
        }

        while (start < end) {
            int nameEnd = value.indexOf('=', start);
            if (nameEnd != -1 && nameEnd < end
                && paramName.equals(value.substring(start, nameEnd).trim())) {
                String paramValue = value.substring(nameEnd + 1, end).trim();
                int valueLength = paramValue.length();
                if (valueLength != 0) {
                    if (valueLength > 2 && '"' == paramValue.charAt(0)
                        && '"' == paramValue.charAt(valueLength - 1)) {
                        return paramValue.substring(1, valueLength - 1);
                    } else {
                        return paramValue;
                    }
                }
            }

            start = end + 1;
            end = value.indexOf(';', start);
            if (end == -1) {
                end = length;
            }
        }

        return null;
    }

    /**
     * Get 'charset' parameter from 'Content-Type' response header
     *
     * @return charset or null if none
     */
    public String charset() {
        return parameter(HEADER_CONTENT_TYPE, PARAM_CHARSET);
    }

    /**
     * Set the 'User-Agent' header to given value
     *
     * @param userAgent
     * @return this request
     */
    public HttpRequest userAgent(String userAgent) {
        return header(HEADER_USER_AGENT, userAgent);
    }

    /**
     * Set the 'Referer' header to given value
     *
     * @param referer
     * @return this request
     */
    public HttpRequest referer(String referer) {
        return header(HEADER_REFERER, referer);
    }

    /**
     * Set value of {@link HttpURLConnection#setUseCaches(boolean)}
     *
     * @param useCaches
     * @return this request
     */
    public HttpRequest useCaches(boolean useCaches) {
        getConnection().setUseCaches(useCaches);
        return this;
    }

    /**
     * Set the 'Accept-Encoding' header to given value
     *
     * @param acceptEncoding
     * @return this request
     */
    public HttpRequest acceptEncoding(String acceptEncoding) {
        return header(HEADER_ACCEPT_ENCODING, acceptEncoding);
    }

    /**
     * Set the 'Accept-Encoding' header to 'gzip'
     *
     * @see #decompress(boolean)
     * @return this request
     */
    public HttpRequest acceptGzipEncoding() {
        return acceptEncoding(ENCODING_GZIP);
    }

    /**
     * Set the 'Accept-Charset' header to given value
     *
     * @param acceptCharset
     * @return this request
     */
    public HttpRequest acceptCharset(String acceptCharset) {
        return header(HEADER_ACCEPT_CHARSET, acceptCharset);
    }

    /**
     * Get the 'Content-Encoding' header from the response
     *
     * @return this request
     */
    public String contentEncoding() {
        return header(HEADER_CONTENT_ENCODING);
    }

    /**
     * Get the 'Server' header from the response
     *
     * @return server
     */
    public String server() {
        return header(HEADER_SERVER);
    }

    /**
     * Get the 'Date' header from the response
     *
     * @return date value, -1 on failures
     */
    public long date() {
        return dateHeader(HEADER_DATE);
    }

    /**
     * Get the 'Cache-Control' header from the response
     *
     * @return cache control
     */
    public String cacheControl() {
        return header(HEADER_CACHE_CONTROL);
    }

    /**
     * Get the 'ETag' header from the response
     *
     * @return entity tag
     */
    public String eTag() {
        return header(HEADER_ETAG);
    }

    /**
     * Get the 'Expires' header from the response
     *
     * @return expires value, -1 on failures
     */
    public long expires() {
        return dateHeader(HEADER_EXPIRES);
    }

    /**
     * Get the 'Last-Modified' header from the response
     *
     * @return last modified value, -1 on failures
     */
    public long lastModified() {
        return dateHeader(HEADER_LAST_MODIFIED);
    }

    /**
     * Get the 'Location' header from the response
     *
     * @return location
     */
    public String location() {
        return header(HEADER_LOCATION);
    }

    /**
     * Set the 'Authorization' header to given value
     *
     * @param authorization
     * @return this request
     */
    public HttpRequest authorization(String authorization) {
        return header(HEADER_AUTHORIZATION, authorization);
    }

    /**
     * Set the 'Proxy-Authorization' header to given value
     *
     * @param proxyAuthorization
     * @return this request
     */
    public HttpRequest proxyAuthorization(String proxyAuthorization) {
        return header(HEADER_PROXY_AUTHORIZATION, proxyAuthorization);
    }

    /**
     * Set the 'Authorization' header to given values in Basic authentication
     * format
     *
     * @param name
     * @param password
     * @return this request
     */
    public HttpRequest basic(String name, String password) {
        byte[] data = (name + ':' + password).getBytes(StandardCharsets.US_ASCII);
        return authorization("Basic " + Base64.getEncoder().encodeToString(data));
    }

    /**
     * Set the 'Proxy-Authorization' header to given values in Basic authentication
     * format
     *
     * @param name
     * @param password
     * @return this request
     */
    public HttpRequest proxyBasic(String name, String password) {
        byte[] data = (name + ':' + password).getBytes(StandardCharsets.US_ASCII);
        return proxyAuthorization("Basic " + Base64.getEncoder().encodeToString(data));
    }

    /**
     * Set the 'If-Modified-Since' request header to the given value
     *
     * @param ifModifiedSince
     * @return this request
     */
    public HttpRequest ifModifiedSince(long ifModifiedSince) {
        getConnection().setIfModifiedSince(ifModifiedSince);
        return this;
    }

    /**
     * Set the 'If-None-Match' request header to the given value
     *
     * @param ifNoneMatch
     * @return this request
     */
    public HttpRequest ifNoneMatch(String ifNoneMatch) {
        return header(HEADER_IF_NONE_MATCH, ifNoneMatch);
    }

    /**
     * Set the 'Content-Type' request header to the given value
     *
     * @param contentType
     * @return this request
     */
    public HttpRequest contentType(String contentType) {
        return contentType(contentType, null);
    }

    /**
     * Set the 'Content-Type' request header to the given value and charset
     *
     * @param contentType
     * @param charset
     * @return this request
     */
    public HttpRequest contentType(String contentType, String charset) {
        if (charset != null && charset.length() > 0) {
            String separator = "; " + PARAM_CHARSET + '=';
            return header(HEADER_CONTENT_TYPE, contentType + separator + charset);
        } else {
            return header(HEADER_CONTENT_TYPE, contentType);
        }
    }

    /**
     * Get the 'Content-Type' header from the response
     *
     * @return response header value
     */
    public String contentType() {
        return header(HEADER_CONTENT_TYPE);
    }

    /**
     * Get the 'Content-Length' header from the response
     *
     * @return response header value
     */
    public int contentLength() {
        return intHeader(HEADER_CONTENT_LENGTH);
    }

    /**
     * Set the 'Content-Length' request header to the given value
     *
     * @param contentLength
     * @return this request
     */
    public HttpRequest contentLength(String contentLength) {
        return contentLength(Integer.parseInt(contentLength));
    }

    /**
     * Set the 'Content-Length' request header to the given value
     *
     * @param contentLength
     * @return this request
     */
    public HttpRequest contentLength(int contentLength) {
        getConnection().setFixedLengthStreamingMode(contentLength);
        return this;
    }

    /**
     * Set the 'Accept' header to given value
     *
     * @param accept
     * @return this request
     */
    public HttpRequest accept(String accept) {
        return header(HEADER_ACCEPT, accept);
    }

    /**
     * Set the 'Accept' header to 'application/json'
     *
     * @return this request
     */
    public HttpRequest acceptJson() {
        return accept(ContentType.APPLICATION_JSON.value());
    }

    /**
     * Copy from input stream to output stream
     *
     * @param input
     * @param output
     * @return this request
     * @throws IOException
     */
    protected HttpRequest copy(InputStream input, OutputStream output) {
        return new CloseOperation<HttpRequest>(input, ignoreCloseExceptions) {
            @Override
            public HttpRequest run() throws IOException {
                byte[] buffer = new byte[bufferSize];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    totalWritten += read;
                    progress.onUpload(totalWritten, totalSize);
                }
                return HttpRequest.this;
            }
        }.call();
    }

    /**
     * Copy from reader to writer
     *
     * @param input
     * @param output
     * @return this request
     * @throws IOException
     */
    protected HttpRequest copy(Reader input, Writer output) {
        return new CloseOperation<HttpRequest>(input, ignoreCloseExceptions) {
            @Override
            public HttpRequest run() throws IOException {
                char[] buffer = new char[bufferSize];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    totalWritten += read;
                    progress.onUpload(totalWritten, -1);
                }
                return HttpRequest.this;
            }
        }.call();
    }

    /**
     * Set the UploadProgress callback for this request
     *
     * @param callback
     * @return this request
     */
    public HttpRequest progress(UploadProgress callback) {
        if (callback == null) {
            progress = UploadProgress.DEFAULT;
        } else {
            progress = callback;
        }
        return this;
    }

    private HttpRequest incrementTotalSize(long size) {
        if (totalSize == -1) {
            totalSize = 0;
        }
        totalSize += size;
        return this;
    }

    /**
     * Close output stream
     *
     * @return this request
     * @throws HttpException
     * @throws IOException
     */
    protected HttpRequest closeOutput() throws IOException {
        progress(null);

        if (output == null) {
            return this;
        }

        if (multipart) {
            output.write(CRLF + "--" + BOUNDARY + "--" + CRLF);
        }
        if (ignoreCloseExceptions) {
            try {
                output.close();
            } catch (IOException ignored) {
                ignored.printStackTrace(); // ignored
            }
        } else {
            output.close();
        }
        output = null;
        return this;
    }

    /**
     * Call {@link #closeOutput()} and re-throw a caught {@link IOException}s as
     * an {@link HttpException}
     *
     * @return this request
     * @throws HttpException
     */
    protected HttpRequest closeOutputQuietly() throws HttpException {
        try {
            return closeOutput();
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    /**
     * Open output stream
     *
     * @return this request
     * @throws IOException
     */
    protected HttpRequest openOutput() throws IOException {
        if (output != null) {
            return this;
        }
        getConnection().setDoOutput(true);
        String charset = getParam(getConnection().getRequestProperty(HEADER_CONTENT_TYPE), PARAM_CHARSET);
        output = new RequestOutputStream(getConnection().getOutputStream(), charset, bufferSize);
        return this;
    }

    /**
     * Start part of a multipart
     *
     * @return this request
     * @throws IOException
     */
    protected HttpRequest startPart() throws IOException {
        if (!multipart) {
            multipart = true;
            contentType(CONTENT_TYPE_MULTIPART).openOutput();
            output.write("--" + BOUNDARY + CRLF);
        } else {
            output.write(CRLF + "--" + BOUNDARY + CRLF);
        }
        return this;
    }

    /**
     * Write part header
     *
     * @param name
     * @param filename
     * @return this request
     * @throws IOException
     */
    protected HttpRequest writePartHeader(String name, String filename) {
        return writePartHeader(name, filename, null);
    }

    /**
     * Write part header
     *
     * @param name
     * @param filename
     * @param contentType
     * @return this request
     * @throws IOException
     */
    protected HttpRequest writePartHeader(String name, String filename, String contentType) {
        StringBuilder partBuffer = new StringBuilder();
        partBuffer.append("form-data; name=\"").append(name);
        if (filename != null) {
            partBuffer.append("\"; filename=\"").append(filename);
        }
        partBuffer.append('"');
        partHeader("Content-Disposition", partBuffer.toString());
        if (contentType != null) {
            partHeader(HEADER_CONTENT_TYPE, contentType);
        }
        return send(CRLF);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     */
    public HttpRequest part(String name, String part) {
        return part(name, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param part
     * @return this request
     * @throws HttpException
     */
    public HttpRequest part(String name, String filename, String part) 
        throws HttpException {
        return part(name, filename, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param contentType value of the Content-Type part header
     * @param part
     * @return this request
     * @throws HttpException
     */
    public HttpRequest part(String name, String filename, String contentType, String part) 
        throws HttpException {
        try {
            startPart();
            writePartHeader(name, filename, contentType);
            output.write(part);
        } catch (IOException e) {
            throw new HttpException(e);
        }
        return this;
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     * @throws HttpException
     */
    public HttpRequest part(String name, Number part)
        throws HttpException {
        return part(name, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param part
     * @return this request
     */
    public HttpRequest part(String name, String filename, Number part) {
        return part(name, filename, Objects.toString(part, null));
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     * @throws HttpException
     */
    public HttpRequest part(String name, File part)
        throws HttpException {
        return part(name, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param part
     * @return this request
     * @throws HttpException
     */
    public HttpRequest part(String name, String filename, File part) 
        throws HttpException {
        return part(name, filename, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param contentType value of the Content-Type part header
     * @param part
     * @return this request
     * @throws HttpException
     */
    public HttpRequest part(String name, String filename, String contentType, File part) 
        throws HttpException {
        InputStream stream;
        try {
            stream = new BufferedInputStream(new FileInputStream(part));
            incrementTotalSize(part.length());
        } catch (IOException e) {
            throw new HttpException(e);
        }
        return part(name, filename, contentType, stream);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     * @throws HttpException
     */
    public HttpRequest part(String name, InputStream part)
        throws HttpException {
        return part(name, null, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param contentType value of the Content-Type part header
     * @param part
     * @return this request
     * @throws HttpException
     */
    public HttpRequest part(String name, String filename, String contentType, InputStream part)
        throws HttpException {
        try {
            startPart();
            writePartHeader(name, filename, contentType);
            copy(part, output);
        } catch (IOException e) {
            throw new HttpException(e);
        }
        return this;
    }

    /**
     * Write a multipart header to the response body
     *
     * @param name
     * @param value
     * @return this request
     * @throws HttpException
     */
    public HttpRequest partHeader(String name, String value)
        throws HttpException {
        return send(name).send(": ").send(value).send(CRLF);
    }

    /**
     * Write contents of file to request body
     *
     * @param input
     * @return this request
     * @throws HttpException
     */
    public HttpRequest send(File input) throws HttpException {
        InputStream stream;
        try {
            stream = new BufferedInputStream(new FileInputStream(input));
            incrementTotalSize(input.length());
        } catch (FileNotFoundException e) {
            throw new HttpException(e);
        }
        return send(stream);
    }

    /**
     * Write byte array to request body
     *
     * @param input
     * @return this request
     * @throws HttpException
     */
    public HttpRequest send(byte[] input) throws HttpException {
        if (input == null) {
            return this;
        }
        incrementTotalSize(input.length);
        return send(new ByteArrayInputStream(input));
    }

    /**
     * Write stream to request body
     * <p>
     * The given stream will be closed once sending completes
     *
     * @param input
     * @return this request
     * @throws HttpException
     */
    public HttpRequest send(InputStream input) throws HttpException {
        try {
            openOutput();
            copy(input, output);
        } catch (IOException e) {
            throw new HttpException(e);
        }
        return this;
    }

    /**
     * Write reader to request body
     * <p>
     * The given reader will be closed once sending completes
     *
     * @param input
     * @return this request
     * @throws HttpException
     */
    public HttpRequest send(Reader input) throws HttpException {
        try {
            openOutput();
        } catch (IOException e) {
            throw new HttpException(e);
        }

        Writer writer = new OutputStreamWriter(output, output.encoder.charset());

        return new FlushOperation<HttpRequest>(writer) {
            @Override
            protected HttpRequest run() {
                return copy(input, writer);
            }
        }.call();
    }

    /**
     * Write char sequence to request body
     * <p>
     * The charset configured via {@link #contentType(String)} will be used and
     * UTF-8 will be used if it is unset.
     *
     * @param value
     * @return this request
     * @throws HttpException
     */
    public HttpRequest send(CharSequence value) throws HttpException {
        try {
            openOutput();
            output.write(value.toString());
        } catch (IOException e) {
            throw new HttpException(e);
        }
        return this;
    }

    /**
     * Create writer to request output stream
     *
     * @return writer
     * @throws HttpException
     */
    public OutputStreamWriter writer() throws HttpException {
        try {
            openOutput();
            return new OutputStreamWriter(output, output.encoder.charset());
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    /**
     * Write the values in the map as form data to the request body
     * <p>
     * The pairs specified will be URL-encoded in UTF-8 and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param values
     * @return this request
     * @throws HttpException
     */
    public HttpRequest form(Map<?, ?> values) throws HttpException {
        return form(values, Files.UTF_8);
    }

    /**
     * Write the key and value in the entry as form data to the request body
     * <p>
     * The pair specified will be URL-encoded in UTF-8 and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param entry
     * @return this request
     * @throws HttpException
     */
    public HttpRequest form(Entry<?, ?> entry) throws HttpException {
        return form(entry, Files.UTF_8);
    }

    /**
     * Write the key and value in the entry as form data to the request body
     * <p>
     * The pair specified will be URL-encoded and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param entry
     * @param charset
     * @return this request
     * @throws HttpException
     */
    public HttpRequest form(Entry<?, ?> entry, String charset)
        throws HttpException {
        return form(entry.getKey(), entry.getValue(), charset);
    }

    /**
     * Write the name/value pair as form data to the request body
     * <p>
     * The pair specified will be URL-encoded in UTF-8 and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param name
     * @param value
     * @return this request
     * @throws HttpException
     */
    public HttpRequest form(Object name, Object value) throws HttpException {
        return form(name, value, Files.UTF_8);
    }

    /**
     * Write the name/value pair as form data to the request body
     * <p>
     * The values specified will be URL-encoded and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param name
     * @param value
     * @param charset
     * @return this request
     * @throws HttpException
     */
    public HttpRequest form(Object name, Object value, String charset)
        throws HttpException {
        boolean first = !form;
        if (first) {
            contentType(ContentType.APPLICATION_FORM_URLENCODED.value(), charset);
            form = true;
        }
        charset = getValidCharset(charset);
        try {
            openOutput();
            if (!first) {
                output.write('&');
            }
            output.write(URLEncoder.encode(name.toString(), charset));
            output.write('=');
            if (value != null) {
                output.write(URLEncoder.encode(value.toString(), charset));
            }
        } catch (IOException e) {
            throw new HttpException(e);
        }
        return this;
    }

    /**
     * Write the values in the map as encoded form data to the request body
     *
     * @param values
     * @param charset
     * @return this request
     * @throws HttpException
     */
    public HttpRequest form(Map<?, ?> values, String charset)
        throws HttpException {
        if (!values.isEmpty()) {
            for (Entry<?, ?> entry : values.entrySet()) {
                form(entry, charset);
            }
        }
        return this;
    }

    /**
     * Configure HTTPS connection to trust all certificates
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     * @throws HttpException
     */
    public HttpRequest trustAllCerts() throws HttpException {
        HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(TRUSTED_FACTORY);
        }
        return this;
    }

    /**
     * 设置SSLSocketFactory
     * @param factory SSLSocketFactory
     * @return this
     */
    public HttpRequest setSSLSocketFactory(SSLSocketFactory factory) {
        HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(factory);
        }
        return this;
    }

    /**
     * Configure HTTPS connection to trust all hosts using a custom
     * {@link HostnameVerifier} that always returns <code>true</code> for each
     * host verified
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     */
    public HttpRequest trustAllHosts() {
        HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setHostnameVerifier(TRUSTED_VERIFIER);
        }
        return this;
    }

    /**
     * Get the {@link URL} of this request's connection
     *
     * @return request URL
     */
    public URL url() {
        return getConnection().getURL();
    }

    /**
     * Get the HTTP method of this request
     *
     * @return method
     */
    public String method() {
        return getConnection().getRequestMethod();
    }

    /**
     * Configure an HTTP proxy on this connection. Use {{@link #proxyBasic(String, String)} if
     * this proxy requires basic authentication.
     *
     * @param proxyHost
     * @param proxyPort
     * @return this request
     */
    public HttpRequest useProxy(String proxyHost, int proxyPort) {
        if (connection != null) {
            throw new IllegalStateException("The connection has already been created. This "
                         + "method must be called before reading or writing to the request.");
        }

        this.httpProxyHost = proxyHost;
        this.httpProxyPort = proxyPort;
        return this;
    }

    /**
     * Set whether or not the underlying connection should follow redirects in
     * the response.
     *
     * @param followRedirects - true fo follow redirects, false to not.
     * @return this request
     */
    public HttpRequest followRedirects(boolean followRedirects) {
        getConnection().setInstanceFollowRedirects(followRedirects);
        return this;
    }

}
