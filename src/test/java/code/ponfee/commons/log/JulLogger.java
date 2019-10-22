package code.ponfee.commons.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * 
 */
public class JulLogger {
    private static Logger logger = Logger.getLogger(JulLogger.class.getSimpleName());

    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        logger.log(Level.SEVERE, "JulLogger");
    }
}
