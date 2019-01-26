//package test.http.ssl;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.security.KeyStore;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.conn.scheme.Scheme;
//import org.apache.http.conn.ssl.SSLSocketFactory;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;
//
//public class HttpsClient {
//
//    private static final String KEY_STORE_TYPE_JKS = "jks";
//    private static final String KEY_STORE_TYPE_P12 = "PKCS12";
//    private static final String SCHEME_HTTPS = "https";
//    private static final int HTTPS_PORT = 8443;
//    private static final String HTTPS_URL = "https://127.0.0.1:8443/";
//    private static final String KEY_STORE_CLIENT_PATH = "D:/ssl/client.p12";
//    private static final String KEY_STORE_TRUST_PATH = "D:/ssl/client.truststore";
//    private static final String KEY_STORE_PASSWORD = "123456";
//    private static final String KEY_STORE_TRUST_PASSWORD = "123456";
//
//    public static void main(String[] args) throws Exception {
//        ssl();
//    }
//
//    private static void ssl() throws Exception {
//        HttpClient httpClient = new DefaultHttpClient();
//        try {
//            // 加载客户端密钥库
//            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);
//            InputStream ksIn = new FileInputStream(KEY_STORE_CLIENT_PATH);
//            keyStore.load(ksIn, KEY_STORE_PASSWORD.toCharArray());
//            ksIn.close();
//
//            // 加载客户端信任库
//            KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_JKS);
//            InputStream tsIn = new FileInputStream(new File(KEY_STORE_TRUST_PATH));
//            trustStore.load(tsIn, KEY_STORE_TRUST_PASSWORD.toCharArray());
//            tsIn.close();
//            
//            // 初始化socketFactory
//            SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore, KEY_STORE_PASSWORD, trustStore);
//            Scheme sch = new Scheme(SCHEME_HTTPS, HTTPS_PORT, socketFactory);
//            httpClient.getConnectionManager().getSchemeRegistry().register(sch);
//            HttpGet httpget = new HttpGet(HTTPS_URL);
//            System.out.println("executing request" + httpget.getRequestLine());
//            HttpResponse response = httpClient.execute(httpget);
//            HttpEntity entity = response.getEntity();
//            System.out.println("----------------------------------------");
//            System.out.println(response.getStatusLine());
//            if (entity != null) {
//                System.out.println("Response content length: "
//                        + entity.getContentLength());
//                BufferedReader bufferedReader = new BufferedReader(
//                        new InputStreamReader(entity.getContent()));
//                String text;
//                while ((text = bufferedReader.readLine()) != null) {
//                    System.out.println(text);
//                }
//                bufferedReader.close();
//            }
//            EntityUtils.consume(entity);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            httpClient.getConnectionManager().shutdown();
//        }
//    }
//
//}
