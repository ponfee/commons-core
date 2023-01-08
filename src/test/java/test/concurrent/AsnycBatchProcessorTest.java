package test.concurrent;

import cn.ponfee.commons.concurrent.AsyncBatchProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AsnycBatchProcessorTest {

    @Test
    public void test1() throws InterruptedException {
        System.out.println(20 + (20 >>> 1));

        AtomicInteger summer = new AtomicInteger();
        AtomicBoolean end = new AtomicBoolean(false);

        final AsyncBatchProcessor<Integer> processor = new AsyncBatchProcessor<>((list, isEnd) -> {
            summer.addAndGet(list.size());
            System.out.println(list.size() + "==" + Thread.currentThread().getId() + "-" + Thread.currentThread().getName() + ", " + isEnd);
            if (isEnd) {
                end.set(true);
            }
            //System.out.println(1 / 0); // submit方式不会打印异常
        }, 100, 200, 10);

        AtomicBoolean flag = new AtomicBoolean(true);
        AtomicInteger increment = new AtomicInteger(0);
        int n = 13;
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                while (flag.get()) {
                    try {
                        processor.put(increment.incrementAndGet());
                        Thread.sleep(3 + ThreadLocalRandom.current().nextInt(7));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
            threads[i] = thread;
        }
        Thread.sleep(10000);
        flag.set(false);
        processor.stopAndAwait();
        for (Thread thread : threads) {
            thread.join();
        }
        while (!end.get()) {
            // noop-loop
        }
        System.out.println(increment.get());
        System.out.println(summer.get());
        Assert.assertEquals(increment.get(), summer.get());
        System.out.println("end");
    }

    @Test
    public void test2() throws InterruptedException {
        AtomicInteger summer = new AtomicInteger();
        AtomicBoolean end = new AtomicBoolean(false);

        final AsyncBatchProcessor<Integer> processor = new AsyncBatchProcessor<>((list, isEnd) -> {
            summer.addAndGet(list.size());
            System.out.println(list.size() + "==" + Thread.currentThread().getId() + "-" + Thread.currentThread().getName() + ", " + isEnd);
            if (isEnd) {
                end.set(true);
            }
            //System.out.println(1 / 0); // submit方式不会打印异常
        }, 200, 50, 10);

        AtomicBoolean flag = new AtomicBoolean(true);
        AtomicInteger increment = new AtomicInteger(0);
        int n = 20;
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                while (flag.get()) {
                    try {
                        processor.put(increment.incrementAndGet());
                        Thread.sleep(3 + ThreadLocalRandom.current().nextInt(7));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
            threads[i] = thread;
        }
        Thread.sleep(10000);
        flag.set(false);
        processor.stopAndAwait();
        for (Thread thread : threads) {
            thread.join();
        }
        while (!end.get()) {
            // noop-loop
        }
        System.out.println(increment.get());
        System.out.println(summer.get());
        Assert.assertEquals(increment.get(), summer.get());
        System.out.println("end");
    }
}
