package code.ponfee.commons.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <pre>
 *  异步数据批处理
 *
 *  execute：直接抛出异常，在线程外部无法捕获异常，想要捕获该异常，可以实现UncaughtExceptionHandler接口
 *  submit ：不会抛出异常，需要调用返回值Future对象的get方法
 *
 *  <a href="https://github.com/JCTools/JCTools">JCTools</a>
 *  <a href="https://github.com/LMAX-Exchange/disruptor">disruptor</a>
 * </pre>
 *
 * @param <T>
 * @author Ponfee
 * @see Thread#setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)
 */
public final class AsyncBatchProcessor<T> {

    private final static Logger LOG = LoggerFactory.getLogger(AsyncBatchProcessor.class);

    private final AsyncBatchThread<T> async;

    public AsyncBatchProcessor(BatchProcessor<T> processor) {
        this(processor, 100, 200, 2);
    }

    /**
     * @param processor        批处理器
     * @param periodTimeMillis 处理周期(毫秒)
     * @param batchSize        批量大小
     * @param maximumPoolSize  最大线程数
     */
    public AsyncBatchProcessor(BatchProcessor<T> processor,
                               int periodTimeMillis,
                               int batchSize,
                               int maximumPoolSize) {
        this.async = new AsyncBatchThread<>(
            processor, periodTimeMillis, batchSize, maximumPoolSize
        );
    }

    /**
     * Puts an element to queue
     *
     * @param element the element
     */
    public boolean put(T element) {
        return !async.stopped.get() && async.queue.offer(element);
    }

    /**
     * Batch put elements to queue.
     *
     * @param elements the elements
     */
    public boolean put(T[] elements) {
        if (async.stopped.get() || elements == null || elements.length == 0) {
            return false;
        }

        for (T element : elements) {
            if (!async.queue.offer(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Batch put elements to queue.
     *
     * @param elements the list of elements
     */
    public boolean put(List<T> elements) {
        if (async.stopped.get() || elements == null || elements.isEmpty()) {
            return false;
        }

        for (T element : elements) {
            if (!async.queue.offer(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Do stop
     *
     * @return {@code true} if stop success
     */
    public boolean stop() {
        return async.stopped.compareAndSet(false, true);
    }

    public void stopAndAwait() throws InterruptedException {
        stop();
        while (!MultithreadExecutors.isStopped(async)) {
            Thread.sleep(async.periodTimeMillis);
        }
    }

    /**
     * Async batch consume into this alone thread
     */
    private static class AsyncBatchThread<T> extends Thread {
        private static final int MINIMUM_PERIOD_TIME_MILLIS = 9;

        // 单消费者用LinkedBlockingQueue，多消费者用ConcurrentLinkedQueue
        private final LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
        private final AtomicBoolean        stopped = new AtomicBoolean(false);

        private final BatchProcessor<T> processor; // 处理器
        private final int periodTimeMillis;        // 消费周期(毫秒)
        private final int sleepTimeMillis;         // 休眠时间
        private final int batchSize;               // 批量大小
        private final int asyncExecuteThreshold;   // 启用异步执行器的阈值
        private final int maximumPoolSize;         // 最大线程数

        private long nextRefreshTimeMillis = 0L;   // 下一次刷新时间

        /**
         * @param processor        批处理器
         * @param periodTimeMillis 处理周期(毫秒)
         * @param batchSize        批量大小
         * @param maximumPoolSize  最大线程数
         */
        private AsyncBatchThread(BatchProcessor<T> processor,
                                 int periodTimeMillis,
                                 int batchSize,
                                 int maximumPoolSize) {
            Assert.isTrue(periodTimeMillis >= MINIMUM_PERIOD_TIME_MILLIS, "Period time millis must greater than " + MINIMUM_PERIOD_TIME_MILLIS + ", but actual " + periodTimeMillis);
            Assert.isTrue(batchSize > 0, "Batch size cannot negative number.");
            Assert.isTrue(maximumPoolSize > 0, "Maximum pool size cannot negative number.");

            this.processor = processor;
            this.periodTimeMillis = periodTimeMillis;
            this.sleepTimeMillis = (periodTimeMillis >>> 1);
            this.batchSize = batchSize;
            this.asyncExecuteThreshold = batchSize + (batchSize >>> 1);
            this.maximumPoolSize = maximumPoolSize;

            super.setName("async-batch-processor-thread-" + Integer.toHexString(hashCode()));
            super.setDaemon(false);
            super.start();
        }

        /**
         * thread inner run, don't to direct call this method
         * it is a thread and the alone thread
         */
        @Override
        public void run() {
            // async thread pool executor
            ThreadPoolExecutor asyncExecutor = null;
            ArrayList<T> list = new ArrayList<>(batchSize);

            for (int left = batchSize; ; ) {
                if (isEnd()) {
                    if (asyncExecutor != null) {
                        // wait a moment if async execute
                        try {
                            Thread.sleep(periodTimeMillis);
                        } catch (InterruptedException e) {
                            LOG.error("Thread#sleep occur error.", e);
                            Thread.currentThread().interrupt();
                        }
                    }

                    // double check
                    if (isEnd()) {
                        if (asyncExecutor != null) {
                            // destroy the async executor
                            ThreadPoolExecutors.shutdown(asyncExecutor);
                        }

                        // exit for loop if end
                        break;
                    }
                }

                // 尽量不要使用queue.size()，时间复杂度O(n)
                if (!queue.isEmpty() && left > 0) {
                    left -= queue.drainTo(list, left);
                }

                long currentTimeMillis = System.currentTimeMillis();
                if (left == 0 || (!list.isEmpty() && (stopped.get() || currentTimeMillis >= nextRefreshTimeMillis))) {
                    if (asyncExecutor == null && left == 0 && queue.size() > asyncExecuteThreshold) {
                        asyncExecutor = ThreadPoolExecutors.create(
                            1, maximumPoolSize, 300, 2,
                            "async-batch-processor-worker",
                            ThreadPoolExecutors.ALWAYS_CALLER_RUNS
                        );
                        LOG.info("Asnyc batch processor created thread pool executor: {}", new ThreadPoolMonitor(asyncExecutor));
                    }

                    if (asyncExecutor != null) {
                        // async thread pool execute
                        // task抛异常后：
                        //   execute输出错误信息，线程结束，后续任务会创建新线程执行，会抛出异常
                        //   submit不输出错误信息，线程继续分配执行其它任务，不会抛出异常，除非你调用Future.get()
                        final List<T> data = list;
                        asyncExecutor.submit(() -> processor.process(data, isEnd()));
                        list = new ArrayList<>(left = batchSize);
                    } else {
                        // current thread execute
                        processor.process(list, isEnd());
                        // reuse the list object
                        list.clear();
                        left = batchSize;
                    }

                    nextRefreshTimeMillis = currentTimeMillis + periodTimeMillis;
                } else if (!stopped.get()) {
                    try {
                        // to sleep for prevent endless loop
                        Thread.sleep(sleepTimeMillis);
                    } catch (InterruptedException e) {
                        LOG.error("Thread#sleep occur error.", e);
                        stopped.compareAndSet(false, true);
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        private boolean isEnd() {
            return stopped.get() && queue.isEmpty();
        }
    }

    @FunctionalInterface
    public interface BatchProcessor<T> {
        void process(List<T> t, boolean stopped);
    }

}
