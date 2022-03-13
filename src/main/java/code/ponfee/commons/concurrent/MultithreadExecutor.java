package code.ponfee.commons.concurrent;

import com.google.common.base.Stopwatch;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Multi Thread executor
 * 
 * @author Ponfee
 */
public class MultithreadExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(MultithreadExecutor.class);

    /**
     * Exec async
     * 
     * usual use in test case
     * 
     * @param threadNumber the exec thread number
     * @param command      the command
     * @param execSeconds  the execSeconds
     * @param executor     the executor
     */
    public static void execAsync(int threadNumber, Runnable command, int execSeconds, 
                                 Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        AtomicBoolean flag = new AtomicBoolean(true);

        // CALLER_RUNS: caller run will be dead loop
        // caller thread will be loop exec command, can't to run the after code{flag.set(false)}
        // threadNumber > 32
        CompletableFuture<?>[] futures = IntStream
            .range(0, threadNumber)
            .mapToObj(
                i -> CompletableFuture.runAsync(() -> {
                    while (flag.get() && !Thread.currentThread().isInterrupted()) {
                        command.run();
                    }
                }, executor)
            )
            .toArray(CompletableFuture[]::new);

        try {
            Thread.sleep(execSeconds * 1000L); // parent thread sleep
            flag.set(false);
            CompletableFuture.allOf(futures).join();
        } catch (InterruptedException e) {
            flag.set(false);
            throw new RuntimeException(e);
        } finally {
            LOG.info("multi thread exec async duration: {}", watch.stop());
        }
    }

    // -----------------------------------------------------------------execAsync
    /**
     * Exec async
     *  
     * @param command      the command
     * @param threadNumber the thread number for exec command
     * @param executor     thread executor service
     */
    public static void runAsync(Runnable command, int threadNumber, Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        CompletableFuture
            .allOf(
                IntStream.range(0, threadNumber).mapToObj(
                    i -> CompletableFuture.runAsync(command, executor)
                ).toArray(CompletableFuture[]::new)
            )
            .join();
        LOG.info("multi thread run async duration: {}", watch.stop());
    }

    // -----------------------------------------------------------------callAsync
    public static <U> List<U> callAsync(Supplier<U> supplier, int threadNumber) {
        Stopwatch watch = Stopwatch.createStarted();
        List<U> result = IntStream
            .range(0, threadNumber)
            .mapToObj(i -> CompletableFuture.supplyAsync(supplier))
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        LOG.info("multi thread call async duration: {}", watch.stop());
        return result;
    }

    // -----------------------------------------------------------------runAsync
    /**
     * Run async, action the T collection
     * 
     * @param coll the T collection
     * @param action the T action
     * @param executor  thread executor service
     */
    public static <T> void runAsync(Collection<T> coll, Consumer<T> action, Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        coll.stream()
            .map(e -> CompletableFuture.runAsync(() -> action.accept(e), executor))
            .forEach(CompletableFuture::join);
        LOG.info("multi thread run async duration: {}", watch.stop());
    }

    // -----------------------------------------------------------------callAsync
    /**
     * Call async, mapped T to U
     * 
     * @param coll the T collection
     * @param mapper  the mapper of T to U
     * @param executor thread executor service
     * @return the U collection
     */
    public static <T, U> List<U> callAsync(Collection<T> coll, Function<T, U> mapper, Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        List<U> result = coll.stream()
                             .map(e -> CompletableFuture.supplyAsync(() -> mapper.apply(e), executor))
                             .map(CompletableFuture::join).collect(Collectors.toList());
        LOG.info("multi thread call async duration: {}", watch.stop());
        return result;
    }

    // -----------------------------------------------------------------join
    public static <T> List<T> join(CompletionService<T> service, int count) {
        List<T> result = new ArrayList<>(count);
        join(service, count, result::add);
        return result;
    }

    public static <T> void joinDiscard(CompletionService<T> service, int count) {
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

    /**
     * 根据数据（任务）数量来判断是否主线程执行还是提交到线程池执行
     *
     * @param data              the data
     * @param action            the action
     * @param dataSizeThreshold the dataSizeThreshold
     * @param executor          the executor
     * @param <T> data element type
     * @param <R> result element type
     * @return list for action result
     */
    public static <T, R> List<R> execute(Collection<T> data, Function<T, R> action,
                                         int dataSizeThreshold, Executor executor) {
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        if (dataSizeThreshold < 1 || data.size() < dataSizeThreshold) {
            return data.stream().map(action::apply).collect(Collectors.toList());
        }

        CompletionService<R> service = new ExecutorCompletionService<>(executor);
        data.forEach(e -> service.submit(() -> action.apply(e)));
        return join(service, data.size());
    }

}
