package test.concurrent;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

import code.ponfee.commons.concurrent.AsyncBatchTransmitter;

public class TestBatchTransmitter {

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger summer = new AtomicInteger();
        final AsyncBatchTransmitter<Integer> transmitter = new AsyncBatchTransmitter<>((list, isEnd) -> {
            return () -> {
                summer.addAndGet(list.size());
                System.out.println(list.size() + "==" + Thread.currentThread().getId()
                    + "-" + Thread.currentThread().getName() + ", " + isEnd);
                //System.out.println(1 / 0); // submit方式不会打印异常
            };
        });

        AtomicBoolean flag = new AtomicBoolean(true);
        AtomicInteger increment = new AtomicInteger(0);
        int n = 100;
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                while (flag.get()) {
                    transmitter.put(increment.incrementAndGet());
                    try {
                        Thread.sleep(161 + ThreadLocalRandom.current().nextInt(1161));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
            threads[i] = thread;
        }
        Thread.sleep(30000);
        flag.set(false);
        transmitter.end();
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println(increment.get());
        System.out.println(summer.get());
        Assert.assertEquals(increment.get(), summer.get());
    }
}
