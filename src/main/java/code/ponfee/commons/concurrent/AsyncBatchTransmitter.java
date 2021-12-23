package code.ponfee.commons.concurrent;

import code.ponfee.commons.exception.CheckedException;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiFunction;

/**
 * 异步批量数据中转站
 * 
 * https://www.jianshu.com/p/d9b54670fa20
 *  {@link Thread#setUncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)}
 *  {@link java.util.concurrent.Executors#newCachedThreadPool(java.util.concurrent.ThreadFactory)}
 *
 * https://github.com/JCTools/JCTools
 * https://github.com/LMAX-Exchange/disruptor
 * 
 * @author Ponfee
 * @param <T>
 */
public final class AsyncBatchTransmitter<T> {

    // 单消费者用LinkedBlockingQueue，多消费者用ConcurrentLinkedQueue
    private final LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>(); // capacity: Integer.MAX_VALUE
    private final AsyncBatchThread batch;
    private volatile boolean isEnd = false;

    public AsyncBatchTransmitter(BiFunction<List<T>, Boolean, Runnable> processor) {
        this(processor, 1000, 200);
    }

    public AsyncBatchTransmitter(BiFunction<List<T>, Boolean, Runnable> processor, 
                                 int thresholdPeriod, int thresholdChunk) {
        this(processor, thresholdPeriod, thresholdChunk, null);
    }

    /**
     * @param processor       处理器
     * @param thresholdPeriod 消费周期阀值
     * @param thresholdChunk  消费数量阀值
     * @param executor        线程执行器
     */
    public AsyncBatchTransmitter(BiFunction<List<T>, Boolean, Runnable> processor, 
                                 int thresholdPeriod, int thresholdChunk, 
                                 ExecutorService executor) {
        this.batch = new AsyncBatchThread(
            processor, thresholdPeriod, thresholdChunk, executor
        );
    }

    /**
     * Puts an element to queue
     * 
     * @param t
     * @return
     */
    public void put(T t) {
        try {
            this.queue.put(t);
        } catch (InterruptedException e) {
            throw new CheckedException(e);
        }
    }

    /**
     * batch put
     * @param ts
     * @return
     */
    @SafeVarargs
    public final void put(T... ts) {
        if (ts == null || ts.length == 0) {
            return;
        }

        for (T t : ts) {
            this.put(t);
        }
    }

    /**
     * batch put
     * @param list
     * @return
     */
    public void put(List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (T t : list) {
            this.put(t);
        }
    }

    /**
     * 结束
     */
    public synchronized void end() {
        if (isEnd) {
            return;
        }
        this.batch.refresh();
        this.isEnd = true;
        this.batch.refresh();
    }

    /**
     * asnyc batch consume into this alone thread
     */
    private final class AsyncBatchThread extends Thread {
        final BiFunction<List<T>, Boolean, Runnable> processor; // 处理器
        final int sleepTimeMillis; // 休眠时间
        final int thresholdPeriod; // 消费周期阀值
        final int thresholdChunk; // 消费数量阀值
        final boolean requireDestroyWhenEnd;
        final ExecutorService executor;

        long lastConsumeTimeMillis = System.currentTimeMillis(); // 最近刷新时间

        /**
         * @param processor        处理器
         * @param executor         线程执行器
         * @param thresholdPeriod  消费周期阀值
         * @param thresholdChunk   消费数量阀值
         */
        AsyncBatchThread(BiFunction<List<T>, Boolean, Runnable> processor, 
                         int thresholdPeriod, int thresholdChunk, 
                         ExecutorService executor) {
            Preconditions.checkArgument(thresholdPeriod > 0);
            Preconditions.checkArgument(thresholdChunk > 0);

            this.processor = processor;
            this.sleepTimeMillis = Math.max(9, thresholdPeriod >>> 1);
            this.thresholdPeriod = thresholdPeriod;
            this.thresholdChunk = thresholdChunk;
            if (executor == null) {
                this.requireDestroyWhenEnd = true;
                this.executor = ThreadPoolExecutors.create(
                    1, Runtime.getRuntime().availableProcessors(), 
                    120, 0, "async-batch-transmitter"
                );
                Runtime.getRuntime().addShutdownHook(
                    new Thread(this.executor::shutdown)
                );
            } else {
                this.requireDestroyWhenEnd = false;
                this.executor = executor;
            }
            super.setName("async-batch-transmitter-thread-" + Integer.toHexString(hashCode()));
            super.setDaemon(true);
            super.start(); // 启动线程
        }

        /**
         * thread inner run, don't to direct call this method
         * it is a thread and the alone thread
         */
        @Override
        public void run() {
            List<T> list = new ArrayList<>(thresholdChunk);
            for (int left = thresholdChunk;;) {
                if (isEnd && queue.isEmpty() && duration() > (thresholdPeriod << 1)) {
                    if (requireDestroyWhenEnd) {
                        ThreadPoolExecutors.shutdown(executor);
                    }
                    break; // exit for loop when end
                }

                // 尽量不要使用queue.size()，时间复杂度O(n)
                if (!queue.isEmpty() && left > 0) {
                    left -= queue.drainTo(list, left);
                }

                if (left == 0 || (!list.isEmpty() && (isEnd || duration() > thresholdPeriod))) {
                    // task抛异常后：
                    //   execute输出错误信息，线程结束，后续任务会创建新线程执行，会抛出异常
                    //   submit不输出错误信息，线程继续分配执行其它任务，不会抛出异常，除非你调用Future.get()
                    executor.submit(processor.apply(list, isEnd && queue.isEmpty())); // 提交到异步批量处理
                    list = new ArrayList<>(left = thresholdChunk);
                    refresh();
                } else {
                    try {
                        Thread.sleep(sleepTimeMillis); // to sleep for prevent endless loop
                    } catch (InterruptedException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }

        void refresh() {
            lastConsumeTimeMillis = System.currentTimeMillis();
        }

        long duration() {
            return System.currentTimeMillis() - lastConsumeTimeMillis;
        }
    }

}
