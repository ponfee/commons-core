package code.ponfee.commons.io;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Close the AutoCloseable utility
 * 
 * @author Ponfee
 */
public final class Closeables {

    private Closeables() {}

    private static Logger logger = LoggerFactory.getLogger(Closeables.class);

    /**
     * Close and ignore
     * 
     * @param closeable the Closeable
     */
    public static void closeIgnore(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // ignored
            }
        }
    }

    /**
     * Close with console if occur exception
     * 
     * @param closeable the Closeable
     */
    public static void closeConsole(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeLog(@Nullable AutoCloseable closeable) {
        closeLog(closeable, "");
    }

    /**
     * Close the AutoCloseable, if occur exception then log error message
     * 
     * @param closeable the Closeable
     * @param errMsg    the error message
     */
    public static void closeLog(@Nullable AutoCloseable closeable, String errMsg) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.error(errMsg, e);
            }
        }
    }

}
