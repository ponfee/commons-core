package code.ponfee.commons.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiFunction;

import com.google.common.base.Preconditions;

/**
 * 异步批量数据中转站
 * @author Ponfee
 * @param <T>
 */
public final class AsyncBatchTransmitter<T> {

    // 单消费者用LinkedBlockingQueue，多消费者用ConcurrentLinkedQueue
    private final Queue<T> queue = new LinkedBlockingQueue<>(); // capacity: Integer.MAX_VALUE
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
        this.batch = new AsyncBatchThread(processor, thresholdPeriod, 
                                          thresholdChunk, executor);
    }

    /**
     * put one
     * @param t
     * @return
     */
    public boolean put(T t) {
        return this.queue.offer(t);
    }

    /**
     * batch put
     * @param ts
     * @return
     */
    @SafeVarargs
    public final boolean put(T... ts) {
        if (ts == null || ts.length == 0) {
            return false;
        }

        boolean flag = true;
        for (T t : ts) {
            flag &= this.queue.offer(t);
        }
        return flag;
    }

    /**
     * batch put
     * @param list
     * @return
     */
    public boolean put(List<T> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }

        boolean flag = true;
        for (T t : list) {
            flag &= this.queue.offer(t);
        }
        return flag;
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
        public @Override void run() {
            T t;
            List<T> list = new ArrayList<>(thresholdChunk);
            for (;;) {
                if (isEnd && queue.isEmpty() && cumulate() > 2 * thresholdPeriod) {
                    if (requireDestroyWhenEnd) {
                        try {
                            executor.shutdown();
                        } catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                    }
                    break; // exit for loop when end
                }

                // 尽量不要使用queue.size()，时间复杂度O(n)
                if (!queue.isEmpty()) {
                    for (int n = thresholdChunk - list.size(), i = 0; i < n; i++) {
                        t = queue.poll();
                        if (t == null) {
                            break; // break inner loop
                        } else {
                            list.add(t);
                        }
                    }
                }

                if (   list.size() == thresholdChunk 
                    || ( !list.isEmpty() && (isEnd || cumulate() > thresholdPeriod) )
                ) {
                    // task抛异常后：
                    //   execute输出错误信息，线程结束，后续任务会创建新线程执行，会抛出异常
                    //   submit不输出错误信息，线程继续分配执行其它任务，不会抛出异常，除非你调用Future.get()
                    executor.submit(processor.apply(list, isEnd && queue.isEmpty())); // 提交到异步批量处理
                    list = new ArrayList<>(thresholdChunk);
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

        long cumulate() {
            return System.currentTimeMillis() - lastConsumeTimeMillis;
        }
    }

}
