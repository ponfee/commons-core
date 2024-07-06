/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.concurrent;

import cn.ponfee.commons.exception.Throwables.ThrowingRunnable;

import java.util.LinkedList;
import java.util.List;

/**
 * Single thread runtime shutdown hook.
 *
 * @author Ponfee
 */
public class SingleThreadShutdownHook {

    private static final Object LOCK = new Object();

    private static HookThread thread = null;

    public static void addHook(Runnable hook) {
        synchronized (LOCK) {
            if (thread == null) {
                thread = new HookThread();
                Runtime.getRuntime().addShutdownHook(thread);
            }
            thread.hooks.add(hook);
        }
    }

    private static class HookThread extends Thread {
        private final List<Runnable> hooks = new LinkedList<>();

        @Override
        public void run() {
            synchronized (LOCK) {
                hooks.forEach(e -> ThrowingRunnable.doCaught(e::run));
            }
        }
    }

}
