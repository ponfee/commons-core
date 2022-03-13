package code.ponfee.commons.exception;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Checked exception for use in lambda
 * 
 * @author Ponfee
 */
public final class CheckedThrowing {

    public static <R, T extends Throwable> Callable<R> callable(ThrowingCallable<R, T> c) {
        return ThrowingCallable.checked(c);
    }

    public static <T, E extends Throwable> Consumer<T> consumer(ThrowingConsumer<T, E> c) {
        return ThrowingConsumer.checked(c);
    }

    public static <T, R, E extends Throwable> Function<T, R> function(ThrowingFunction<T, R, E> f) {
        return ThrowingFunction.checked(f);
    }

    public static <R, T extends Throwable> Supplier<R> supplier(ThrowingSupplier<R, T> s) {
        return ThrowingSupplier.checked(s);
    }

    /**
     * eg: new Thread(CheckedThrowing.runnable(printer::print))
     *
     * @param r    the ThrowingRunnable
     * @param <T>  the type of Throwable
     * @return Runnable instance
     */
    public static <T extends Throwable> Runnable runnable(ThrowingRunnable<T> r) {
        return ThrowingRunnable.checked(r);
    }

    // -------------------------------------------------------------------------------definitions
    /**
     * Lambda function checked exception
     * 
     * @param <R> the result type of method {@code call}
     * @param <T> the type of the call "call" method possible occur exception
     */
    @FunctionalInterface
    public interface ThrowingCallable<R, T extends Throwable> {
        R call() throws T;

        static <R, T extends Throwable> Callable<R> checked(ThrowingCallable<R, T> c) {
            return () -> {
                try {
                    return c.call();
                } catch (RuntimeException t) {
                    throw t;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
        }
    }

    /**
     * Lambda function checked exception
     * 
     * @param <E> the type of the input to the operation
     * @param <T> the type of the call accept method possible occur exception
     */
    public interface ThrowingConsumer<E, T extends Throwable> {
        void accept(E e) throws T;

        static <E, T extends Throwable> Consumer<E> checked(ThrowingConsumer<E, T> c) {
            return e -> {
                try {
                    c.accept(e);
                } catch (RuntimeException t) {
                    throw t;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
        }
    }

    /**
     * Lambda function checked exception
     * 
     * @param <E> the type of the input to the function
     * @param <R> the type of the result of the function
     * @param <T> the type of the call apply method possible occur exception
     */
    @FunctionalInterface
    public interface ThrowingFunction<E, R, T extends Throwable> {
        R apply(E e) throws T;

        static <E, R, T extends Throwable> Function<E, R> checked(ThrowingFunction<E, R, T> f) {
            return e -> {
                try {
                    return f.apply(e);
                } catch (RuntimeException t) {
                    throw t;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
        }
    }

    /**
     * Lambda function checked exception
     * 
     * @param <R> the type of results supplied by this supplier
     * @param <T> the type of the call get method possible occur exception
     */
    @FunctionalInterface
    public interface ThrowingSupplier<R, T extends Throwable> {
        R get() throws T;

        static <R, T extends Throwable> Supplier<R> checked(ThrowingSupplier<R, T> s) {
            return () -> {
                try {
                    return s.get();
                } catch (RuntimeException t) {
                    throw t;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable<T extends Throwable> {
        void run() throws T;

        static <T extends Throwable> Runnable checked(ThrowingRunnable<T> r) {
            return () -> {
                try {
                    r.run();
                } catch (RuntimeException t) {
                    throw t;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
        }
    }

    @FunctionalInterface
    public interface ThrowingComparator<E, T extends Throwable> {
        int compare(E e1, E e2) throws T;

        static <E, T extends Throwable> Comparator<? super E> checked(ThrowingComparator<E, T> c) {
            return (e1, e2) -> {
                try {
                    return c.compare(e1, e2);
                } catch (RuntimeException t) {
                    throw t;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
        }
    }

}
