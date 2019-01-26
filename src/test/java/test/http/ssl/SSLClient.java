package test.http.ssl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class SSLClient {

    private static String CLIENT_KEY_STORE = SSLClient.class.getClassLoader().getResource("").getPath()+"/META-INF/client_ks";
    private static String CLIENT_KEY_STORE_PASSWORD = "123456";

    public static void main(String[] args) throws Exception {
        // 打印网络通信信息
        System.setProperty("javax.net.debug", "ssl,handshake");

        // 设置客户端对服务端的信任库
        System.setProperty("javax.net.ssl.trustStore", CLIENT_KEY_STORE);
        
        SSLClient client = new SSLClient();
//        Socket s = client.clientWithoutCert(); // 单向
        Socket s = client.clientWithCert(); // 双向

        PrintWriter writer = new PrintWriter(s.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        writer.println("你 好！");
        writer.flush();
        System.out.println(reader.readLine());
        s.close();
    }

    // 单向认证
    private Socket clientWithoutCert() throws Exception {
        SocketFactory sf = SSLSocketFactory.getDefault();
        Socket s = sf.createSocket("localhost", 8000);
        return s;
    }
    
    // 双向认证
    private Socket clientWithCert() throws Exception {
        // 加载客户端密钥库
        KeyStore ks = KeyStore.getInstance("jceks");
        ks.load(new FileInputStream(CLIENT_KEY_STORE), null);
        
        KeyManagerFactory kf = KeyManagerFactory.getInstance("SunX509");
        kf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());
        
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kf.getKeyManagers(), null, null);

        SocketFactory factory = context.getSocketFactory();
        Socket s = factory.createSocket("localhost", 8000);
        return s;
    }
}
