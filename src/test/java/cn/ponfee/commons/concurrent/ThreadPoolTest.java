package cn.ponfee.commons.concurrent;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import com.google.common.base.Stopwatch;

import cn.ponfee.commons.date.Dates;

public class ThreadPoolTest {

    public static void main(String[] args) throws Exception {
        System.out.println("main-thread: " + Thread.currentThread().getName());
        //BLOCK_PRODUCER();
        //CALLER_RUN();

        deadlock(32, () -> { // XXX 33以上会出现死循环
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            //System.out.println("=============thread: "+ Thread.currentThread().getName());
        }, 2);
    }

    private static void deadlock(int threadNumber, Runnable command, int execSeconds) {
        Stopwatch watch = Stopwatch.createStarted();
        AtomicBoolean flag = new AtomicBoolean(true);

        CompletableFuture<?>[] futures = IntStream.range(0, threadNumber).mapToObj(
            x -> CompletableFuture.runAsync(() -> {
                while (flag.get() && !Thread.currentThread().isInterrupted()) {
                    command.run();
                }
            }, ThreadPoolTestUtils.INFINITY_QUEUE_EXECUTOR) // CALLER_RUN_EXECUTOR：caller run will be dead lock
       ).toArray(CompletableFuture[]::new);

        System.err.println("************************************************");
        try {
            Thread.sleep(execSeconds * 1000L); // parent thread sleep
            flag.set(false);
            CompletableFuture.allOf(futures).join();
        } catch (InterruptedException e) {
            flag.set(false);
            throw new RuntimeException(e);
        } finally {
            System.out.println("multi thread exec async duration: {}" + watch.stop());
            System.exit(0);
        }
    }

    private static class Comsumer implements Runnable {
        private final int num;

        public Comsumer(int num) {
            this.num = num;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000 + ThreadLocalRandom.current().nextInt(6000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("consume:=======" + Dates.format(new Date()) + "=========" + num + ", thread:" + Thread.currentThread().getName());
        }
    }

}
