/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * 
 * Close the AutoCloseable utility
 * 
 * @author Ponfee
 */
public final class Closeables {

    private static final Logger LOG = LoggerFactory.getLogger(Closeables.class);

    /**
     * Close and ignore
     * 
     * @param closeable the Closeable
     */
    public static void ignore(@Nullable AutoCloseable closeable) {
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
                LOG.error(errMsg, e);
            }
        }
    }

}
