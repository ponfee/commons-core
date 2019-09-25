package code.ponfee.commons.log;

import org.apache.log4j.Logger;

/**
 */
public class Log4jLogger {
    private static Logger logger = Logger.getLogger(Log4jLogger.class);

    public static void main(String[] args) {
        logger.error("Log4jLogger");
    }
}
