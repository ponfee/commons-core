package code.ponfee.commons.util;

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
                        logMsg = ObjectUtils.uuid32() + " - " + message.get();
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
