package code.ponfee.commons;

import java.lang.management.ManagementFactory;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyWebServer {

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_WEBAPP = "src/main/webapp";
    private static final String DEFAULT_WEB = DEFAULT_WEBAPP + "/WEB-INF/web.xml";
    private static final String DEFAULT_PROJECT = "webapp";
    private static Server server;

    public static void main(String[] args) throws Exception {
        JettyWebServer.startJetty(getPortFromArgs(args));
    }

    private static int getPortFromArgs(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.valueOf(args[0]);
            } catch (NumberFormatException ignore) {
                System.err.println(ignore);
            }
        }
        return DEFAULT_PORT;
    }

    private static void startJetty(int port) throws Exception {
        server = new Server(port);
        server.setHandler(getWebAppContext());
        server.start();
        server.join();
    }

    public static void stopJetty() throws Exception {
        server.stop();
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Windows")) {
            Runtime.getRuntime().exec("Taskkill /f /IM " + pid);
        } else {
            String[] cmd = { "sh", "-c", "kill -9 " + pid };
            Runtime.getRuntime().exec(cmd);
        }
    }

    private static WebAppContext getWebAppContext() {
        WebAppContext context = new WebAppContext();
        context.setDescriptor(DEFAULT_WEB);
        context.setResourceBase(DEFAULT_WEBAPP);
        context.setContextPath(DEFAULT_PROJECT);
        context.setParentLoaderPriority(true);
        return context;
    }

}
