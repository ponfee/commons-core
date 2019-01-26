package test.http.ssl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

public class SSLServer extends Thread {

    private static final String SERVER_KEY_STORE = SSLServer.class.getClassLoader().getResource("").getPath()+"/META-INF/server_ks";
    private static final String PWD = "123456";
    private Socket socket;
    
    public SSLServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            
            String receptStr = reader.readLine();
            writer.write(receptStr);
            
            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        // 设置服务端信任库
        System.setProperty("javax.net.ssl.trustStore", SERVER_KEY_STORE);
        
        // 加载服务端密钥库
        KeyStore keyStore = KeyStore.getInstance("jceks");
        keyStore.load(new FileInputStream(SERVER_KEY_STORE), null);
        
        // 初始化密钥
        KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
        factory.init(keyStore, PWD.toCharArray());
        
        // 初始化上下文
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(factory.getKeyManagers(), null, null);
        
        // 创建服务端通信接口
        ServerSocketFactory serverSocketFactory = context.getServerSocketFactory();
        SSLServerSocket sslServerSocket = (SSLServerSocket)serverSocketFactory.createServerSocket(8000);
        //sslServerSocket.setNeedClientAuth(false); // 单向
        sslServerSocket.setNeedClientAuth(true); // 双向
        
        while (true) {
            new SSLServer(sslServerSocket.accept()).start();
        }
    }
    
}
