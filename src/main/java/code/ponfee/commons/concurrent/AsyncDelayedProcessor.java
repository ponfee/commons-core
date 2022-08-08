package code.ponfee.commons.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Async delayed data processor
 *
 * @param <E> the element type
 * @author Ponfee
 */
public final class AsyncDelayedProcessor<E> {

    private final static Logger LOG = LoggerFactory.getLogger(AsyncDelayedProcessor.class);

    private final int capacity;
    private final int delayInMilliseconds;
    private final AsyncBatchThread<E> async;

    public AsyncDelayedProcessor(Consumer<E> processor) {
        this(20, 5000, 1, processor);
    }

    /**
     * @param capacity            the queue capacity
     * @param delayInMilliseconds the delayInMilliseconds
     * @param maximumPoolSize     the maximumPoolSize
     * @param processor           the data processor
     */
    public AsyncDelayedProcessor(int capacity,
                                 int delayInMilliseconds,
                                 int maximumPoolSize,
                                 Consumer<E> processor) {
        this.capacity = capacity;
        this.delayInMilliseconds = delayInMilliseconds;
        this.async = new AsyncBatchThread<>(processor, maximumPoolSize);
    }

    /**
     * Puts an element to queue
     *
     * @param element the element
     */
    public boolean put(E element) {
        if (async.stopped.get() || async.queue.size() > capacity) {
            return false;
        }
        return async.queue.offer(DelayedData.of(element, delayInMilliseconds));
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
        MultithreadExecutors.stopThread(async, 10, 100, 2000);
    }

    /**
     * Async delayed process into this alone thread
     */
    private static class AsyncBatchThread<E> extends Thread {

        private final DelayQueue<DelayedData<E>> queue = new DelayQueue<>();
        private final AtomicBoolean stopped = new AtomicBoolean(false);

        private final Consumer<E> processor;            // 数据处理器
        private final ThreadPoolExecutor asyncExecutor; // 异步执行器

        private AsyncBatchThread(Consumer<E> processor,
                                 int maximumProcessorSize) {
            this.processor = processor;

            ThreadPoolExecutor executor = null;
            if (maximumProcessorSize > 1) {
                executor = ThreadPoolExecutors.create(
                    1, maximumProcessorSize, 300, 0,
                    "async-delayed-processor-worker",
                    ThreadPoolExecutors.ALWAYS_CALLER_RUNS
                );
            }
            this.asyncExecutor = executor;

            super.setName("async-delayed-processor-thread-" + Integer.toHexString(hashCode()));
            super.setDaemon(false);
            super.start();
        }

        @Override
        public void run() {
            int round = 0;

            while (!stopped.get()) {
                if (++round == 997) {
                    round = 0;
                    LOG.info("delayed_queue_size {}", queue.size());
                }

                DelayedData<E> delayed;
                try {
                    delayed = queue.poll(3000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    LOG.error("Delayed queue pool occur interrupted.", e);
                    stopped.compareAndSet(false, true);
                    Thread.currentThread().interrupt();
                    break;
                }

                if (delayed != null) {
                    E data = delayed.getData();
                    if (asyncExecutor != null) {
                        asyncExecutor.submit(() -> processor.accept(data));
                    } else {
                        processor.accept(data);
                    }
                }
            }

            if (asyncExecutor != null) {
                // destroy the async executor
                ThreadPoolExecutors.shutdown(asyncExecutor);
            }
        }
    }

}
