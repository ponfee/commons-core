package code.ponfee.commons.base;

import javax.security.auth.Destroyable;

import code.ponfee.commons.exception.CheckedException;

/**
 * Release resources
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface Releasable {

    MethodInvoker RELEASER = new MethodInvoker("close", "destroy", "release");

    void release();

    static void release(Object caller) {
        if (caller == null) {
            return;
        }

        try {
            if (caller instanceof AutoCloseable) {
                ((AutoCloseable) caller).close();
            } else if (caller instanceof Destroyable) {
                ((Destroyable) caller).destroy();
            } else if (caller instanceof Releasable) {
                ((Releasable) caller).release();
            } else {
                RELEASER.invoke(caller);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CheckedException(e);
        }
    }

}
