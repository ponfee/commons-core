package code.ponfee.commons.concurrent;

import org.slf4j.MDC;

import java.util.Map;

/**
 * Traced runnable
 * 
 * @author Ponfee
 */
public final class TracedRunnable implements Runnable {

    private final Runnable runnable;

    private TracedRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public static TracedRunnable of(Runnable runnable) {
        return new TracedRunnable(runnable);
    }

    @Override
    public void run() {
        Map<String, String> ctx = MDC.getCopyOfContextMap();
        if (ctx != null) {
            MDC.setContextMap(ctx);
        }
        try {
            runnable.run();
        } finally {
            MDC.clear();
        }
    }
}
