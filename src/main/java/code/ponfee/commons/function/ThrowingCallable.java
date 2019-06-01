package code.ponfee.commons.function;

import java.util.concurrent.Callable;

/**
 * Lambda function unchecked exception
 * 
 * @author Ponfee
 * @param <R> the result type of method {@code call}
 * @param <T> the type of the call "call" method possible occur exception
 */
@FunctionalInterface
public interface ThrowingCallable<R, T extends Throwable> {

    R call() throws T;

    static <R, T extends Throwable> Callable<R> unchecked(ThrowingCallable<R, T> f) {
        return () -> {
            try {
                return f.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
