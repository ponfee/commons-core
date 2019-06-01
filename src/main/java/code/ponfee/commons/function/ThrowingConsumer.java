package code.ponfee.commons.function;

import java.util.function.Consumer;

/**
 * Lambda function unchecked exception
 * 
 * @author Ponfee
 * @param <T> the type of the input to the operation
 * @param <E> the type of the call accept method possible occur exception
 */
public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T t) throws E;

    static <T, E extends Throwable> Consumer<T> unchecked(ThrowingConsumer<T, E> consumer) {
        return (t) -> {
            try {
                consumer.accept(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
