package code.ponfee.commons.concurrent;

import code.ponfee.commons.exception.Throwables;
import code.ponfee.commons.math.Numbers;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Thread pool executor utility
 * 
 * https://blog.csdn.net/Holmofy/article/details/73237153
 * https://blog.csdn.net/holmofy/article/details/77411854
 * 
 * @author Ponfee
 */
public final class ThreadPoolExecutors {

    public static final int MAX_CAP = 0x7FFF; // max #workers - 1

    // ----------------------------------------------------------build-in rejected policy
    public static final RejectedExecutionHandler ABORT = new AbortPolicy();

    public static final RejectedExecutionHandler DISCARD = new DiscardPolicy();

    // if not shutdown then run
    public static final RejectedExecutionHandler CALLER_RUNS = new CallerRunsPolicy();

    // if not shutdown then discard oldest and execute the new
    public static final RejectedExecutionHandler DISCARD_OLDEST = new DiscardOldestPolicy();

    // if not shutdown then put queue until enqueue
    public static final RejectedExecutionHandler BLOCK_CALLER = (task, executor) -> {
        if (!executor.isShutdown()) {
            try {
                executor.getQueue().put(task);
            } catch (InterruptedException e) {
                throw new RuntimeException("Put a task to queue occur error: BLOCK_PRODUCER", e);
            }
        }
    };

    // anyway always run
    public static final RejectedExecutionHandler ALWAYS_CALLER_RUNS = (task, executor) -> task.run();

    // anyway always discard oldest and execute the new
    public static final RejectedExecutionHandler ALWAYS_DISCARD_OLDEST = (task, executor) -> {
        executor.getQueue().poll();
        executor.execute(task);
    };

    // anyway always put queue until enqueue
    public static final RejectedExecutionHandler ALWAYS_BLOCK_CALLER = (task, executor) -> {
        try {
            executor.getQueue().put(task);
        } catch (InterruptedException e) {
            throw new RuntimeException("Put a task to queue occur error: BLOCK_PRODUCER", e);
        }
    };

    // ----------------------------------------------------------build-in scheduler/executor
    public static final ScheduledExecutorService CALLER_RUN_SCHEDULER =
        new DelegatedScheduledExecutorService("caller-run-scheduler", CALLER_RUNS);

    public static final ExecutorService INFINITY_QUEUE_EXECUTOR =
        new DelegatedExecutorService("infinity-queue-executor", Integer.MAX_VALUE, BLOCK_CALLER);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown(((AbstractDelegatedExecutorService) CALLER_RUN_SCHEDULER).delegate);
            shutdown(((AbstractDelegatedExecutorService) INFINITY_QUEUE_EXECUTOR).delegate);
        }));
    }

    // ----------------------------------------------------------
    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        return create(corePoolSize, maximumPoolSize, keepAliveTime, 0, null, null);
    }

    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, 
                                            long keepAliveTime, int queueCapacity) {
        return create(corePoolSize, maximumPoolSize, keepAliveTime, queueCapacity, null, null);
    }

    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, long keepAliveTime, 
                                            int queueCapacity, RejectedExecutionHandler rejectedHandler) {
        return create(corePoolSize, maximumPoolSize, keepAliveTime, queueCapacity, null, rejectedHandler);
    }

    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, 
                                            long keepAliveTime, int queueCapacity, String threadName) {
        return create(corePoolSize, maximumPoolSize, keepAliveTime, queueCapacity, threadName, null);
    }

    /**
     * 线程池创建器
     * @param corePoolSize     核心线程数
     * @param maximumPoolSize  最大线程数
     * @param keepAliveTime    线程存活时间
     * @param queueCapacity    队列长度
     * @param threadName       线程名称
     * @param rejectedHandler  拒绝策略
     * @return a ThreadPoolExecutor instance
     */
    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, long keepAliveTime, 
                                            int queueCapacity, String threadName, 
                                            RejectedExecutionHandler rejectedHandler) {
        // work queue
        BlockingQueue<Runnable> workQueue = queueCapacity > 0
            ? new LinkedBlockingQueue<>(queueCapacity)
            : new SynchronousQueue<>();

        // thread factory, Executors.defaultThreadFactory()
        ThreadFactory threadFactory = new NamedThreadFactory(threadName);

        // rejected Handler Strategy 
        if (rejectedHandler == null) {
            rejectedHandler = CALLER_RUNS;
        }

        maximumPoolSize = Numbers.bounds(maximumPoolSize, 1, MAX_CAP);
        corePoolSize = Numbers.bounds(corePoolSize, 1, maximumPoolSize);

        // create ThreadPoolExecutor instance
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, 
            workQueue, threadFactory, rejectedHandler
        );

        // pool.prestartCoreThread(): 预先创建1条核心线程
        // pool.prestartAllCoreThreads(): 可预先创建corePoolSize数量的核心线程
        pool.allowCoreThreadTimeOut(true); // 设置允许核心线程超时关闭

        return pool;
    }

    /**
     * Shutdown the ExecutorService safe
     * 
     * @param executorService the executorService
     * @return is safe shutdown
     */
    public static boolean shutdown(ExecutorService executorService) {
        executorService.shutdown();
        /*while (!executorService.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Throwable t) {
                Throwables.console(t);
            }
        }*/
        try {
            while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                // noop loop
            }
            return true;
        } catch (Throwable t) {
            Throwables.console(t);
            executorService.shutdownNow();
            return false;
        }
    }

    /**
     * Shutdown the executorService max wait time
     * 
     * @param executorService the executorService
     * @param awaitSeconds the await seconds
     * @return is safe shutdown
     */
    public static boolean shutdown(ExecutorService executorService, int awaitSeconds) {
        executorService.shutdown();
        boolean isSafeTerminated = false, hasCallShutdownNow = false;
        try {
            isSafeTerminated = executorService.awaitTermination(awaitSeconds, TimeUnit.SECONDS);
            if (!isSafeTerminated) {
                hasCallShutdownNow = true;
                executorService.shutdownNow();
            }
        } catch (Throwable t) {
            Throwables.console(t);
            if (!hasCallShutdownNow) {
                executorService.shutdownNow();
            }
        }
        return isSafeTerminated;
    }

    private static class DelegatedScheduledExecutorService
        extends AbstractDelegatedExecutorService implements ScheduledExecutorService {

        DelegatedScheduledExecutorService(String threadName,
                                          RejectedExecutionHandler handler) {
            super(newScheduledExecutorService(threadName, handler));
        }

        static ScheduledExecutorService newScheduledExecutorService(
            String threadName, RejectedExecutionHandler handler) {
            // maximumPoolSize=Integer.MAX_VALUE, DelayedWorkQueue, keepAliveTime=0
            ScheduledThreadPoolExecutor delegate = new ScheduledThreadPoolExecutor(
                1, new NamedThreadFactory(threadName), handler
            );

            // allowCoreThreadTimeOut(true); // Error: Core threads must have nonzero keep alive times
            return delegate;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return ((ScheduledExecutorService) delegate).schedule(command, delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, 
                                               long delay, TimeUnit unit) {
            return ((ScheduledExecutorService) delegate).schedule(callable, delay, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, 
                                                      long period, TimeUnit unit) {
            return ((ScheduledExecutorService) delegate).scheduleAtFixedRate(
                command, initialDelay, period, unit
            );
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, 
                                                         long delay, TimeUnit unit) {
            return ((ScheduledExecutorService) delegate).scheduleWithFixedDelay(
                command, initialDelay, delay, unit
            );
        }
    }

    private static class DelegatedExecutorService extends AbstractDelegatedExecutorService {

        DelegatedExecutorService(String threadName, int queueCapacity,
                                 RejectedExecutionHandler handler) {
            super(newExecutorService(threadName, queueCapacity, handler));
        }

        static ExecutorService newExecutorService(String threadName, int queueCapacity,
                                                  RejectedExecutionHandler handler) {
            int corePoolSize = Math.max(Runtime.getRuntime().availableProcessors(), 1);
            int maximumPoolSize = Math.min(corePoolSize << 3, MAX_CAP);
            corePoolSize = Math.min(corePoolSize << 2, maximumPoolSize);

            BlockingQueue<Runnable> workQueue;
            if (queueCapacity > 0) {
                workQueue = new LinkedBlockingQueue<>(queueCapacity);
            } else {
                workQueue = new SynchronousQueue<>();
            }

            // create ThreadPoolExecutor instance
            ThreadPoolExecutor delegate = new ThreadPoolExecutor(
                corePoolSize, maximumPoolSize, 120, TimeUnit.SECONDS, 
                workQueue, new NamedThreadFactory(threadName), handler
            );
            delegate.allowCoreThreadTimeOut(true); // 设置允许核心线程超时关闭
            return delegate;
        }
    }

    private static class AbstractDelegatedExecutorService implements ExecutorService {

        final ExecutorService delegate;

        AbstractDelegatedExecutorService(ExecutorService delegate) {
            this.delegate = delegate;
        }

        @Override @Deprecated
        public void shutdown() {
            throw new UnsupportedOperationException();
        }

        @Override @Deprecated
        public List<Runnable> shutdownNow() {
            throw new UnsupportedOperationException();
        }

        @Override @Deprecated
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isShutdown() {
            return this.delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return this.delegate.isTerminated();
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return delegate.submit(task);
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return delegate.submit(task, result);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return delegate.submit(task);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) 
            throws InterruptedException {
            return delegate.invokeAll(tasks);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, 
                                             long timeout, TimeUnit unit) 
            throws InterruptedException {
            return delegate.invokeAll(tasks, timeout, unit);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) 
            throws InterruptedException, ExecutionException {
            return delegate.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, 
                               long timeout, TimeUnit unit) 
            throws InterruptedException, ExecutionException, TimeoutException {
            return delegate.invokeAny(tasks, timeout, unit);
        }

        @Override
        public void execute(Runnable command) {
            delegate.execute(command);
        }
    }

}
