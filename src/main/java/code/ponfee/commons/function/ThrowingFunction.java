package code.ponfee.commons.function;

import java.util.function.Function;

/**
 * Lambda function unchecked exception
 * 
 * @author Ponfee
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of the call apply method possible occur exception
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {

    R apply(T t) throws E;

    static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
