package code.ponfee.commons;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ErrorPage;

public class TomcatWebServer {

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        String webBase = new File("src/main/webapp").getAbsolutePath();
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(".");

        // Add AprLifecycleListener
        StandardServer server = (StandardServer) tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);

        Context webContext = tomcat.addWebapp("/", webBase);
        webContext.addErrorPage(createErrorPage(404, "404.html"));
        webContext.addErrorPage(createErrorPage(500, "50x.html"));
        webContext.addWelcomeFile("main.html");

        // tomcat start
        tomcat.start();
        tomcat.getServer().await();
    }

    private static ErrorPage createErrorPage(int code, String location) {
        ErrorPage errPage = new ErrorPage();
        errPage.setErrorCode(code);
        errPage.setLocation(location);
        return errPage;
    }

}
