package code.ponfee.commons.io;

import javax.annotation.Nullable;
import javax.security.auth.Destroyable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.cache.Releasable;

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

    // ---------------------------------------------------------------------------AutoCloseable
    /**
     * Close with console if occur exception
     * 
     * @param closeable the Closeable
     */
    public static void console(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void log(@Nullable AutoCloseable closeable) {
        log(closeable, "");
    }

    /**
     * Close the AutoCloseable, if occur exception then log error message
     * 
     * @param closeable the Closeable
     * @param errMsg    the error message
     */
    public static void log(@Nullable AutoCloseable closeable, String errMsg) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.error(errMsg, e);
            }
        }
    }

    // ---------------------------------------------------------------------------Destroyable
    /**
     * Destroy with console if occur exception
     * 
     * @param destroyable the Destroyable
     */
    public static void console(@Nullable Destroyable destroyable) {
        if (destroyable != null) {
            try {
                destroyable.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void log(@Nullable Destroyable destroyable) {
        log(destroyable, "");
    }

    /**
     * Destroy the Destroyable, if occur exception then log error message
     * 
     * @param destroyable the Destroyable
     * @param errMsg      the error message
     */
    public static void log(@Nullable Destroyable destroyable, String errMsg) {
        if (destroyable != null) {
            try {
                destroyable.destroy();
            } catch (Exception e) {
                logger.error(errMsg, e);
            }
        }
    }

    // ---------------------------------------------------------------------------Releasable
    /**
     * Release with console if occur exception
     * 
     * @param releasable the Releasable
     */
    public static void console(@Nullable Releasable releasable) {
        if (releasable != null) {
            try {
                releasable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void log(@Nullable Releasable releasable) {
        log(releasable, "");
    }

    /**
     * Release the Releasable, if occur exception then log error message
     * 
     * @param releasable the Releasable
     * @param errMsg     the error message
     */
    public static void log(@Nullable Releasable releasable, String errMsg) {
        if (releasable != null) {
            try {
                releasable.close();
            } catch (Exception e) {
                logger.error(errMsg, e);
            }
        }
    }
}
