package test.http;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class HttpPostTester {

    public static String post(String reqURL, Map<String, String> params) throws Exception {
        HttpPost httpPost = new HttpPost(reqURL);
        if (params != null) {
            List<BasicNameValuePair> nvps = new ArrayList<>();
            Set<Entry<String, String>> paramEntrys = params.entrySet();
            for (Entry<String, String> entry : paramEntrys) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
        }

        httpPost.setHeader("User-Agent", "datagrand/datareport/java sdk v1.0.0");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        // 设置默认时间 
        RequestConfig config = RequestConfig.custom().setConnectTimeout(3000)
                                            .setConnectionRequestTimeout(1000).setSocketTimeout(4000)
                                            .setExpectContinueEnabled(true).build();
        PoolingHttpClientConnectionManager pccm = new PoolingHttpClientConnectionManager();
        pccm.setMaxTotal(300); // 连接池最大并发连接数
        pccm.setDefaultMaxPerRoute(50); // 单路由最大并发数
        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount > 3) {
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    System.out.println("[NoHttpResponseException has retry request:" + context.toString() 
                                     + "][executionCount:" + executionCount + "]");
                    return true;
                } else if (exception instanceof SocketException) {
                    System.out.println("[SocketException has retry request:" + context.toString() 
                                     + "][executionCount:" + executionCount + "]");
                    return true;
                }
                return false;
            }
        };
        HttpClient httpClient = HttpClients.custom().setConnectionManager(pccm)
                                           .setDefaultRequestConfig(config)
                                           .setRetryHandler(retryHandler).build();
        
        HttpResponse response = httpClient.execute(httpPost);
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            System.out.printf("Did not receive successful HTTP response: status code = {}, status message = {}", status.getStatusCode(), status.getReasonPhrase());
            httpPost.abort();
        }

        String responseContent = "";
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            responseContent = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
        } else {
            System.out.printf("Http entity is null! request url is {},response status is {}", reqURL, response.getStatusLine());
        }
        return responseContent;
    }

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", "12345");
        params.put("title", "abc");
        params.put("textid", "123456778");
        params.put("text", "abcdefg");

        String res;
        try {
            res = post("http://commentapi.datagrand.com/bad_comment/meituan", params);
            System.out.println(res);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
