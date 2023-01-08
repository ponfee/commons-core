package cn.ponfee.commons.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Thread pool executor utility
 * 
 * https://blog.csdn.net/Holmofy/article/details/73237153
 * https://blog.csdn.net/holmofy/article/details/77411854
 * 
 * @author Ponfee
 */
public final class ThreadPoolTestUtils {
    public static final int MAX_CAP = 0x7FFF; // max #workers - 1

    // ----------------------------------------------------------build-in scheduler/executor
    public static final ScheduledExecutorService CALLER_RUN_SCHEDULER =
        new DelegatedScheduledExecutorService("caller-run-scheduler", ThreadPoolExecutors.CALLER_RUNS);

    public static final ExecutorService INFINITY_QUEUE_EXECUTOR =
        new DelegatedExecutorService("infinity-queue-executor", Integer.MAX_VALUE, ThreadPoolExecutors.CALLER_BLOCKS);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ThreadPoolExecutors.shutdown(((AbstractDelegatedExecutorService) CALLER_RUN_SCHEDULER).delegate);
            ThreadPoolExecutors.shutdown(((AbstractDelegatedExecutorService) INFINITY_QUEUE_EXECUTOR).delegate);
        }));
    }


    private static class DelegatedScheduledExecutorService
            extends AbstractDelegatedExecutorService implements ScheduledExecutorService {

        private DelegatedScheduledExecutorService(String threadName, RejectedExecutionHandler handler) {
            super(newScheduledExecutorService(threadName, handler));
        }

        private static ScheduledExecutorService newScheduledExecutorService(String threadName,
                                                                            RejectedExecutionHandler handler) {
            // maximumPoolSize=Integer.MAX_VALUE, DelayedWorkQueue, keepAliveTime=0
            ScheduledThreadPoolExecutor delegate = new ScheduledThreadPoolExecutor(
                    1, new NamedThreadFactory(threadName), handler
            );

            //delegate.allowCoreThreadTimeOut(true); // Error: Core threads must have nonzero keep alive times
            return delegate;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return ((ScheduledExecutorService) delegate).schedule(command, delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
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

        private DelegatedExecutorService(String threadName, int queueCapacity,
                                         RejectedExecutionHandler handler) {
            super(newExecutorService(threadName, queueCapacity, handler));
        }

        private static ExecutorService newExecutorService(String threadName, int queueCapacity,
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

        private AbstractDelegatedExecutorService(ExecutorService delegate) {
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
