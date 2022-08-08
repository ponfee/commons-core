package code.ponfee.commons.concurrent;

import code.ponfee.commons.util.ObjectUtils;
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
 * <p> {@code Thread#stop()} will occur "java.lang.ThreadDeath: null" if try...catch wrapped in Throwable
 *
 * @author Ponfee
 */
public class MultithreadExecutors {

    private static final Logger LOG = LoggerFactory.getLogger(MultithreadExecutors.class);

    /**
     * Exec async, usual use in test case
     *
     * @param parallelism the parallelism
     * @param command     the command
     * @param execSeconds the execSeconds
     * @param executor    the executor
     */
    public static void execute(int parallelism, Runnable command,
                               int execSeconds, Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        AtomicBoolean flag = new AtomicBoolean(true);

        // CALLER_RUNS: caller run will be dead loop
        // caller thread will be loop exec command, can't to run the after code{flag.set(false)}
        // threadNumber > 32
        CompletableFuture<?>[] futures = IntStream
            .range(0, parallelism)
            .mapToObj(i -> (Runnable) () -> {
                while (flag.get() && !Thread.currentThread().isInterrupted()) {
                    command.run();
                }
            })
            .map(runnable -> CompletableFuture.runAsync(runnable, executor))
            .toArray(CompletableFuture[]::new);

        try {
            // parent thread sleep
            Thread.sleep(execSeconds * 1000L);
            flag.set(false);
            CompletableFuture.allOf(futures).join();
        } catch (InterruptedException e) {
            flag.set(false);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            LOG.info("multi thread exec async duration: {}", watch.stop());
        }
    }

    // -----------------------------------------------------------------execAsync

    /**
     * Exec async
     *
     * @param command     the command
     * @param parallelism the parallelism
     * @param executor    thread executor service
     */
    public static void execute(Runnable command, int parallelism, Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        CompletableFuture[] futures = IntStream.range(0, parallelism)
                                               .mapToObj(i -> CompletableFuture.runAsync(command, executor))
                                               .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        LOG.info("multi thread run async duration: {}", watch.stop());
    }

    // -----------------------------------------------------------------callAsync
    public static <U> List<U> execute(Supplier<U> supplier, int parallelism) {
        Stopwatch watch = Stopwatch.createStarted();
        List<U> result = IntStream.range(0, parallelism)
                                  .mapToObj(i -> CompletableFuture.supplyAsync(supplier))
                                  .collect(Collectors.toList())
                                  .stream()
                                  .map(CompletableFuture::join)
                                  .collect(Collectors.toList());
        LOG.info("multi thread call async duration: {}", watch.stop());
        return result;
    }

    // -----------------------------------------------------------------runAsync

    /**
     * Run async, action the T collection
     *
     * @param coll     the T collection
     * @param action   the T action
     * @param executor thread executor service
     */
    public static <T> void execute(Collection<T> coll, Consumer<T> action, Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        coll.stream()
            .map(e -> CompletableFuture.runAsync(() -> action.accept(e), executor))
            .collect(Collectors.toList())
            .forEach(CompletableFuture::join);
        LOG.info("multi thread run async duration: {}", watch.stop());
    }

    // -----------------------------------------------------------------callAsync

    /**
     * Call async, mapped T to U
     *
     * @param coll     the T collection
     * @param mapper   the mapper of T to U
     * @param executor thread executor service
     * @return the U collection
     */
    public static <T, U> List<U> execute(Collection<T> coll, Function<T, U> mapper, Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        List<U> result = coll.stream()
                             .map(e -> CompletableFuture.supplyAsync(() -> mapper.apply(e), executor))
                             .collect(Collectors.toList())
                             .stream()
                             .map(CompletableFuture::join)
                             .collect(Collectors.toList());
        LOG.info("multi thread call async duration: {}", watch.stop());
        return result;
    }

    /**
     * 根据数据（任务）数量来判断是否主线程执行还是提交到线程池执行
     *
     * @param data              the data
     * @param action            the action
     * @param dataSizeThreshold the dataSizeThreshold
     * @param executor          the executor
     * @param <T>               data element type
     * @param <R>               result element type
     * @return list for action result
     */
    public static <T, R> List<R> execute(Collection<T> data, Function<T, R> action,
                                         int dataSizeThreshold, Executor executor) {
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        if (dataSizeThreshold < 1 || data.size() < dataSizeThreshold) {
            return data.stream().map(action).collect(Collectors.toList());
        }

        CompletionService<R> service = new ExecutorCompletionService<>(executor);
        data.forEach(e -> service.submit(() -> action.apply(e)));
        return join(service, data.size());
    }

    /**
     * 根据数据（任务）数量来判断是否主线程执行还是提交到线程池执行
     *
     * @param data              the data
     * @param action            the action
     * @param dataSizeThreshold the dataSizeThreshold
     * @param executor          the executor
     * @param <T>               data element type
     */
    public static <T> void execute(Collection<T> data, Consumer<T> action,
                                   int dataSizeThreshold, Executor executor) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        if (dataSizeThreshold < 1 || data.size() < dataSizeThreshold) {
            data.forEach(action);
            return;
        }

        CompletionService<Void> service = new ExecutorCompletionService<>(executor);
        data.forEach(e -> service.submit(() -> action.accept(e), null));
        joinDiscard(service, data.size());
    }

    // -----------------------------------------------------------------join
    public static <T> List<T> join(CompletionService<T> service, int count) {
        List<T> result = new ArrayList<>(count);
        join(service, count, result::add);
        return result;
    }

    public static <T> void joinDiscard(CompletionService<T> service, int count) {
        join(service, count, t -> { });
    }

    public static <T> void join(CompletionService<T> service, int count, Consumer<T> accept) {
        try {
            while (count-- > 0) {
                // block until a task done
                Future<T> future = service.take();
                accept.accept(future.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the thread is whether stopped
     *
     * @param thread the thread
     * @return {@code true} if the thread is stopped
     */
    public static boolean isStopped(Thread thread) {
        return thread.getState() == Thread.State.TERMINATED;
    }

    /**
     * Stop the thread, and return boolean result of has called java.lang.Thread#stop()
     *
     * @param thread      the thread
     * @param sleepCount  the sleepCount
     * @param sleepMillis the sleepMillis
     * @param joinMillis  the joinMillis
     * @return {@code true} if called java.lang.Thread#stop()
     */
    public static boolean stopThread(Thread thread, int sleepCount, long sleepMillis, long joinMillis) {
        if (isStopped(thread)) {
            return false;
        }

        if (Thread.currentThread() == thread) {
            LOG.warn("Call stop on self thread: {}\n{}", thread.getName(), ObjectUtils.getStackTrace());
            thread.interrupt();
            return stopThread(thread);
        }

        LOG.info("Thread stopping: {}", thread.getName());
        while (sleepCount-- > 0 && sleepMillis > 0 && !isStopped(thread)) {
            try {
                // Wait some time
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                LOG.error("Waiting thread terminal interrupted: " + thread.getName(), e);
                thread.interrupt();
                Thread.currentThread().interrupt();
            }
        }

        if (!isStopped(thread)) {
            // interrupt and wait joined
            thread.interrupt();
            if (joinMillis > 0) {
                try {
                    thread.join(joinMillis);
                } catch (InterruptedException e) {
                    LOG.error("Join thread terminal interrupted: " + thread.getName(), e);
                    thread.interrupt();
                    Thread.currentThread().interrupt();
                }
            }
        }

        return stopThread(thread);
    }

    /**
     * Stop the thread, and return boolean result of has called java.lang.Thread#stop()
     *
     * @param thread the thread
     * @return {@code true} if called java.lang.Thread#stop()
     */
    private static boolean stopThread(Thread thread) {
        if (isStopped(thread)) {
            return false;
        }

        synchronized (thread) {
            if (isStopped(thread)) {
                return false;
            }
            try {
                thread.stop();
                // cannot catch Throwable, because it will occur "java.lang.ThreadDeath: null"
            } catch (Exception e) {
                LOG.error("Invoke thread stop occur error: " + thread.getName(), e);
            }
            LOG.warn("Invoked java.lang.Thread#stop() method: {}", thread.getName());
        }

        return true;
    }

}
