package test.http.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class HTTPServerSample {

    private static final int BACK_LOG = 10; // 允许最大连接数

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8888), BACK_LOG);
            server.createContext("/HTTPServerSample", new MyHandler()); // 用MyHandler类处理请求
            server.setExecutor(null); // creates a default executor
            server.start(); // http://localhost:8888/HTTPServerSample
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

@SuppressWarnings("restriction")
class MyHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        InputStream input = exchange.getRequestBody();
        String response = "<h3>" + LocalDateTime.now() + "</h3>";
        exchange.sendResponseHeaders(200, response.length());
        OutputStream output = exchange.getResponseBody();
        output.write(response.getBytes());
        output.close();
        input.close();
    }

}
