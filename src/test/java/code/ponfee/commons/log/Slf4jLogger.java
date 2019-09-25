package code.ponfee.commons.log;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class Slf4jLogger {
    private static Logger logger = LoggerFactory.getLogger(Slf4jLogger.class);

    public static void main(String[] args) throws IOException {
        logger.error("Slf4jLogger");

        //SystemClassloader==APPClassloader
        // -classpath
        Enumeration<URL> r = ClassLoader.getSystemResources("org/slf4j/impl/StaticLoggerBinder.class");
        while (r.hasMoreElements()) {
            URL url = (URL) r.nextElement();
            System.out.println(url.getPath());
        }

        // ExtClassloader
        // JAVA_HOME/jre/lib/ext/

        // -Xbootclasspath
        URL[] urls = sun.misc.Launcher.getBootstrapClassPath().getURLs();
        for (int i = 0; i < urls.length; i++) {
            System.out.println(urls[i].toExternalForm());
        }
    }
}
