package code.ponfee.commons;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ErrorPage;

public class TomcatWebServer {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        String webBase = new File("src/main/webapp").getAbsolutePath();
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(".");

        // Add AprLifecycleListener
        StandardServer server = (StandardServer) tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);

        Context webContext = tomcat.addWebapp("/", webBase);
        ErrorPage notFound = new ErrorPage();
        notFound.setErrorCode(404);
        notFound.setLocation("/main.html");
        webContext.addErrorPage(notFound);
        webContext.addWelcomeFile("main.html");

        // tomcat start
        tomcat.start();
        tomcat.getServer().await();
    }

}
