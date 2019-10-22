package code.ponfee.commons.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 */
public class JclLogger {
    private static Log logger = LogFactory.getLog(JclLogger.class);

    public static void main(String[] args) {
        logger.error("JclLogger");
    }
}
