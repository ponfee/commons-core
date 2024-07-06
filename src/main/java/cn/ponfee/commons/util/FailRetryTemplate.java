/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Fail retry template(template method pattern)
 *
 * @author Ponfee
 */
public class FailRetryTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(FailRetryTemplate.class);

    public static <T> T execute(Supplier<T> normal, Supplier<String> message) throws Exception {
        return execute(normal, normal, 5, message);
    }

    public static <T> T execute(Supplier<T> normal, Supplier<T> fallback,
                                int failRetryCount, Supplier<String> message) throws Exception {
        int i = 0;
        Exception ex;
        String logMsg = null;
        do {
            try {
                if (i == 0) {
                    return normal.get();
                } else {
                    return fallback.get();
                }
            } catch (Exception e) {
                ex = e;
                if (i < failRetryCount) {
                    // not the last loop
                    if (logMsg == null) {
                        logMsg = UuidUtils.uuid32() + " - " + message.get();
                    }
                    int count = i + 1;
                    LOG.error("Execute failed, will retrying - " + count + " - " + logMsg, e);
                    Thread.sleep(5000L * count);
                }
            }
        } while (++i <= failRetryCount);

        throw ex;
    }

}
