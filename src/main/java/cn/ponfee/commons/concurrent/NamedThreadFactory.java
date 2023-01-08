/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.concurrent;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂
 * 
 * @author Ponfee
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

    private final AtomicInteger threadNo = new AtomicInteger(1);

    private final String prefix;

    /**
     * User Thread(用户线程)、Daemon Thread(守护线程)
     */
    private final boolean daemon;

    private final ThreadGroup group;

    public NamedThreadFactory() {
        this(null, Thread.currentThread().isDaemon());
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, Thread.currentThread().isDaemon());
    }

    public NamedThreadFactory(String prefix, boolean daemon) {
        if (StringUtils.isBlank(prefix)) {
            prefix = "pool-" + POOL_SEQ.getAndIncrement();
        }
        SecurityManager s = System.getSecurityManager();

        this.prefix = prefix + "-thread-";
        this.daemon = daemon;
        this.group = (s == null) 
                     ? Thread.currentThread().getThreadGroup() 
                     : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        String name = prefix + threadNo.getAndIncrement();
        Thread thread = new Thread(group, runnable, name, 0);
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }

    public ThreadGroup getThreadGroup() {
        return group;
    }
}
