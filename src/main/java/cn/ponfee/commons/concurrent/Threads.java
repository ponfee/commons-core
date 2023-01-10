/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.concurrent;

import cn.ponfee.commons.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread Utilities
 * 
 * @author Ponfee
 */
public final class Threads {
    private final static Logger LOG = LoggerFactory.getLogger(ThreadPoolExecutors.class);

    /**
     * Returns the thread is whether stopped
     *
     * @param thread the thread
     * @return {@code true} if the thread is stopped
     */
    public static boolean isStopped(Thread thread) {
        return thread.getState() == Thread.State.TERMINATED;
    }

    /**
     * Stop the thread, and return boolean result of has called java.lang.Thread#stop()
     *
     * @param thread      the thread
     * @param sleepCount  the sleepCount
     * @param sleepMillis the sleepMillis
     * @param joinMillis  the joinMillis
     * @return {@code true} if called java.lang.Thread#stop()
     */
    public static boolean stopThread(Thread thread, int sleepCount, long sleepMillis, long joinMillis) {
        if (isStopped(thread)) {
            return false;
        }

        if (Thread.currentThread() == thread) {
            LOG.warn("Call stop on self thread: {}\n{}", thread.getName(), ObjectUtils.getStackTrace());
            thread.interrupt();
            return stopThread(thread);
        }

        LOG.info("Thread stopping: {}", thread.getName());
        while (sleepCount-- > 0 && sleepMillis > 0 && !isStopped(thread)) {
            try {
                // Wait some time
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                LOG.error("Waiting thread terminal interrupted: " + thread.getName(), e);
                thread.interrupt();
                Thread.currentThread().interrupt();
            }
        }

        if (!isStopped(thread)) {
            // interrupt and wait joined
            thread.interrupt();
            if (joinMillis > 0) {
                try {
                    thread.join(joinMillis);
                } catch (InterruptedException e) {
                    LOG.error("Join thread terminal interrupted: " + thread.getName(), e);
                    thread.interrupt();
                    Thread.currentThread().interrupt();
                }
            }
        }

        return stopThread(thread);
    }

    public static void interruptIfNecessary(Throwable t) {
        if (t instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stop the thread, and return boolean result of has called java.lang.Thread#stop()
     *
     * @param thread the thread
     * @return {@code true} if called java.lang.Thread#stop()
     */
    private static boolean stopThread(Thread thread) {
        if (isStopped(thread)) {
            return false;
        }

        synchronized (thread) {
            if (isStopped(thread)) {
                return false;
            }
            try {
                thread.stop();
                // cannot catch Throwable, because it will occur "java.lang.ThreadDeath: null"
            } catch (Exception e) {
                LOG.error("Invoke thread stop occur error: " + thread.getName(), e);
            }
            LOG.warn("Invoked java.lang.Thread#stop() method: {}", thread.getName());
        }

        return true;
    }

}
