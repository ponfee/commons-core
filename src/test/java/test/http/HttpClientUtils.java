package test.http;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.json.Jsons;

public class HttpClientUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    private final static Charset UTF8 = Charset.forName("UTF-8");

    public static final CloseableHttpClient HTTP_CLIENT;
    static {
        // 初始化线程池
        RequestConfig params = RequestConfig.custom().setConnectTimeout(3000).setConnectionRequestTimeout(1000)
                                            .setSocketTimeout(4000).setExpectContinueEnabled(true).build();

        PoolingHttpClientConnectionManager pccm = new PoolingHttpClientConnectionManager();
        pccm.setMaxTotal(300); // 连接池最大并发连接数
        pccm.setDefaultMaxPerRoute(50); // 单路由最大并发数
        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount > 1) {
                    return false; // 重试1次,从1开始
                } else if (exception instanceof NoHttpResponseException) {
                    logger.info("[NoHttpResponseException has retry request:" + context.toString() + "][executionCount:" + executionCount + "]");
                    return true;
                } else if (exception instanceof SocketException) {
                    logger.info("[SocketException has retry request:" + context.toString() + "][executionCount:" + executionCount + "]");
                    return true;
                } else {
                    return false;
                }
            }
        };
        HTTP_CLIENT = HttpClients.custom().setConnectionManager(pccm).setDefaultRequestConfig(params)
                                 .setRetryHandler(retryHandler).build();
    }

    public static String post(String url, Map<String, ?> params, Integer connReqTimeout, 
                              Integer connTimeout, Integer socketTimeout) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if (params != null && !params.isEmpty()) {
            for (Entry<String, ?> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
            }
        }

        logger.info("post-req:url:{},param:{}", url, Jsons.NORMAL.string(params));
        HttpPost post = new HttpPost(url);
        RequestConfig.Builder builder = RequestConfig.custom();
        if (connReqTimeout != null && connReqTimeout > 0) {
            builder.setConnectionRequestTimeout(connReqTimeout);
        }
        if (connTimeout != null && connTimeout > 0) {
            builder.setConnectTimeout(connTimeout);
        }
        if (socketTimeout != null && socketTimeout > 0) {
            builder.setSocketTimeout(socketTimeout);
        }
        post.setConfig(builder.build());
        post.setEntity(new UrlEncodedFormEntity(nvps, UTF8));

        CloseableHttpResponse response = null;
        try {
            response = HTTP_CLIENT.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), UTF8);
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error("[HttpClientUtils][invoke][method:" + post.getMethod() + " URI:" + post.getURI() + "] is request exception", e);
            return null;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("[HttpClientUtils][invoke][method:" + post.getMethod() + " URI:" + post.getURI() + "] is closed exception", e);
                }
            }
        }
    }

}
