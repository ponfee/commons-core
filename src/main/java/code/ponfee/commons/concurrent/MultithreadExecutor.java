package code.ponfee.commons.concurrent;

import static code.ponfee.commons.concurrent.ThreadPoolExecutors.INFINITY_QUEUE_EXECUTOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

/**
 * Multi Thread executor
 * 
 * @author Ponfee
 */
public class MultithreadExecutor {

    private static Logger logger = LoggerFactory.getLogger(MultithreadExecutor.class);

    /**
     * Exec async
     * 
     * usual use in test case
     * 
     * @param threadNumber the exec thread number
     * @param command the command
     * @param execSeconds the execSeconds
     */
    public static void execAsync(int threadNumber, Runnable command, int execSeconds) {
        Stopwatch watch = Stopwatch.createStarted();
        AtomicBoolean flag = new AtomicBoolean(true);

        // CALLER_RUN_EXECUTOR：caller run will be dead loop
        // caller thread will be loop exec command, can't to run the after code
        CompletableFuture<?>[] futures = IntStream.range(0, threadNumber).mapToObj(
            x -> CompletableFuture.runAsync(() -> {
                while (flag.get() && !Thread.currentThread().isInterrupted()) {
                    command.run();
                }
            }, INFINITY_QUEUE_EXECUTOR)
       ).toArray(CompletableFuture[]::new);

        try {
            Thread.sleep(execSeconds * 1000L); // parent thread sleep
            flag.set(false);
            CompletableFuture.allOf(futures).join();
        } catch (InterruptedException e) {
            flag.set(false);
            throw new RuntimeException(e);
        } finally {
            logger.info("multi thread exec async duration: {}", watch.stop());
        }
    }

    // -----------------------------------------------------------------execAsync
    public static void runAsync(Runnable command, int threadNumber) {
        runAsync(command, threadNumber, INFINITY_QUEUE_EXECUTOR);
    }

    /**
     * Exec async
     *  
     * @param command the command
     * @param threadNumber  the thread number for exec command
     * @param executor  thread executor service
     */
    public static void runAsync(Runnable command, int threadNumber, 
                                Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        CompletableFuture.allOf(
            IntStream.range(0, threadNumber).mapToObj(
                x -> CompletableFuture.runAsync(command, executor)
            ).toArray(CompletableFuture[]::new)
        ).join();
        logger.info("multi thread run async duration: {}", watch.stop());
    }

    // -----------------------------------------------------------------callAsync
    public static <U> List<U> callAsync(Supplier<U> supplier, int threadNumber) {
        return callAsync(supplier, threadNumber, INFINITY_QUEUE_EXECUTOR);
    }

    public static <U> List<U> callAsync(Supplier<U> supplier, 
                                        int threadNumber, 
                                        Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        List<U> result = IntStream.range(0, threadNumber).mapToObj(
            x -> CompletableFuture.supplyAsync(supplier)
        ).collect(
            Collectors.toList()
        ).stream().map(
            CompletableFuture::join
        ).collect(
            Collectors.toList()
        );
        logger.info("multi thread call async duration: {}", watch.stop());
        return result;
    }

    // -----------------------------------------------------------------runAsync
    public static <T> void runAsync(Collection<T> coll, Consumer<T> action) {
        runAsync(coll, action, INFINITY_QUEUE_EXECUTOR);
    }

    /**
     * Run async, action the T collection
     * 
     * @param coll the T collection
     * @param action the T action
     * @param executor  thread executor service
     */
    public static <T> void runAsync(Collection<T> coll, 
                                    Consumer<T> action, 
                                    Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        coll.stream().map(
            x -> CompletableFuture.runAsync(() -> action.accept(x), executor)
        ).collect(
            Collectors.toList()
        ).forEach(
            CompletableFuture::join
        );
        logger.info("multi thread run async duration: {}", watch.stop());
    }

    // -----------------------------------------------------------------callAsync
    public static <T, U> List<U> callAsync(Collection<T> coll, 
                                           Function<T, U> mapper) {
        return callAsync(coll, mapper, INFINITY_QUEUE_EXECUTOR);
    }

    /**
     * Call async, mapped T to U
     * 
     * @param coll the T collection
     * @param mapper  the mapper of T to U
     * @param executor thread executor service
     * @return the U collection
     */
    public static <T, U> List<U> callAsync(Collection<T> coll, 
                                           Function<T, U> mapper, 
                                           Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        List<U> result = coll.stream().map(
            x -> CompletableFuture.supplyAsync(() -> mapper.apply(x), executor)
        ).collect(
            Collectors.toList()
        ).stream().map(
            CompletableFuture::join
        ).collect(
            Collectors.toList()
        );
        logger.info("multi thread call async duration: {}", watch.stop());
        return result;
    }

    // -----------------------------------------------------------------join
    public static <T> List<T> join(
        CompletionService<T> service, int count) {
        List<T> result = new ArrayList<>(count);
        join(service, count, result::add);
        return result;
    }

    public static <T> void joinDiscard(
        CompletionService<T> service, int count) {
        join(service, count, t -> {});
    }

    public static <T> void join(CompletionService<T> service, 
                                int count, Consumer<T> accept) {
        try {
            while (count > 0) {
                Future<T> future = service.take(); // block until a task done
                count--;
                accept.accept(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
