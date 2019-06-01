package code.ponfee.commons.function;

import java.util.function.Supplier;

/**
 * Lambda function unchecked exception
 * 
 * @author Ponfee
 * @param <R> the type of results supplied by this supplier
 * @param <T> the type of the call get method possible occur exception
 */
@FunctionalInterface
public interface ThrowingSupplier<R, T extends Throwable> {

    R get() throws T;

    static <R, T extends Throwable> Supplier<R> unchecked(ThrowingSupplier<R, T> f) {
        return () -> {
            try {
                return f.get();
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
