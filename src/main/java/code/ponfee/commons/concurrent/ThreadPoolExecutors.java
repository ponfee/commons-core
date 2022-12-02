package code.ponfee.commons.concurrent;

import code.ponfee.commons.math.Numbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;

/**
 * Thread pool executor utility
 * <a href="https://blog.csdn.net/Holmofy/article/details/73237153">Multiple Thread</a>
 *
 * @author Ponfee
 */
public final class ThreadPoolExecutors {
    private final static Logger LOG = LoggerFactory.getLogger(ThreadPoolExecutors.class);

    public static final int MAX_CAP = 0x7FFF; // max #workers - 1

    // ----------------------------------------------------------build-in rejected policy
    /**
     * abort and throw RejectedExecutionException
     */
    public static final RejectedExecutionHandler ABORT = new AbortPolicy();

    /**
     * discard the task
     */
    public static final RejectedExecutionHandler DISCARD = new DiscardPolicy();

    /**
     * if not shutdown then run
     */
    public static final RejectedExecutionHandler CALLER_RUNS = new CallerRunsPolicy();

    /**
     * if not shutdown then discard oldest and execute the new
     */
    public static final RejectedExecutionHandler DISCARD_OLDEST = new DiscardOldestPolicy();

    /**
     * if not shutdown then put queue until enqueue
     */
    public static final RejectedExecutionHandler CALLER_BLOCKS = (task, executor) -> {
        if (!executor.isShutdown()) {
            try {
                executor.getQueue().put(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Put a task to queue occur error: BLOCK_PRODUCER", e);
            }
        }
    };

    /**
     * anyway always run task, even if thread pool is shutdown
     */
    public static final RejectedExecutionHandler ALWAYS_CALLER_RUNS = (task, executor) -> task.run();

    // ----------------------------------------------------------
    public static ThreadPoolExecutor create(int corePoolSize,
                                            int maximumPoolSize,
                                            long keepAliveTimeSeconds) {
        return create(corePoolSize, maximumPoolSize, keepAliveTimeSeconds, 0, null, null);
    }

    public static ThreadPoolExecutor create(int corePoolSize,
                                            int maximumPoolSize,
                                            long keepAliveTimeSeconds,
                                            int queueCapacity) {
        return create(corePoolSize, maximumPoolSize, keepAliveTimeSeconds, queueCapacity, null, null);
    }

    public static ThreadPoolExecutor create(int corePoolSize,
                                            int maximumPoolSize,
                                            long keepAliveTimeSeconds,
                                            int queueCapacity,
                                            RejectedExecutionHandler rejectedHandler) {
        return create(corePoolSize, maximumPoolSize, keepAliveTimeSeconds, queueCapacity, null, rejectedHandler);
    }

    public static ThreadPoolExecutor create(int corePoolSize,
                                            int maximumPoolSize,
                                            long keepAliveTimeSeconds,
                                            int queueCapacity,
                                            String threadName) {
        return create(corePoolSize, maximumPoolSize, keepAliveTimeSeconds, queueCapacity, threadName, null);
    }

    /**
     * 线程池创建器
     *
     * @param corePoolSize         核心线程数
     * @param maximumPoolSize      最大线程数
     * @param keepAliveTimeSeconds 线程存活时间(秒)
     * @param queueCapacity        队列长度
     * @param threadName           线程名称
     * @param rejectedHandler      拒绝策略
     * @return a ThreadPoolExecutor instance
     */
    public static ThreadPoolExecutor create(int corePoolSize,
                                            int maximumPoolSize,
                                            long keepAliveTimeSeconds,
                                            int queueCapacity,
                                            String threadName,
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
            corePoolSize, maximumPoolSize, keepAliveTimeSeconds, TimeUnit.SECONDS,
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
        /*
        while (!executorService.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                Throwables.console(e);
            }
        }
        */
        try {
            while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                // noop loop
            }
            return true;
        } catch (Exception e) {
            LOG.error("Shutdown ExecutorService occur error.", e);
            executorService.shutdownNow();
            Threads.interruptIfNecessary(e);
            return false;
        }
    }

    /**
     * Shutdown the executorService max wait time
     *
     * @param executorService the executorService
     * @param awaitSeconds    the await seconds
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
        } catch (Exception e) {
            LOG.error("Shutdown ExecutorService occur error.", e);
            if (!hasCallShutdownNow) {
                executorService.shutdownNow();
            }
        }
        return isSafeTerminated;
    }

}
