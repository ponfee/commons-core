/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.base;

import cn.ponfee.commons.exception.ServerException;

/**
 * Release resources
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface Releasable {

    NoArgMethodInvoker RELEASER = new NoArgMethodInvoker("close", "destroy", "release");

    /**
     * 释放资源
     */
    void release();

    static void release(Object caller) {
        if (caller == null) {
            return;
        }

        try {
            if (caller instanceof AutoCloseable) {
                ((AutoCloseable) caller).close();
            } else if (caller instanceof Releasable) {
                Releasable releasable = (Releasable) caller;
                if (!releasable.isReleased()) {
                    ((Releasable) caller).release();
                }
            } else {
                RELEASER.invoke(caller);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException(e);
        }
    }

    /**
     * 是否已经释放，true为已经释放，false未释放
     *
     * @return {@code true}已经释放
     */
    default boolean isReleased() {
        return false;
    }
}
