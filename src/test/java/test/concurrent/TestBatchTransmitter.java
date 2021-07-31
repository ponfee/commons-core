package test.concurrent;

import code.ponfee.commons.concurrent.AsyncBatchTransmitter;
import org.junit.Assert;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestBatchTransmitter {

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger summer = new AtomicInteger();
        AtomicBoolean end = new AtomicBoolean(false);
        final AsyncBatchTransmitter<Integer> transmitter = new AsyncBatchTransmitter<>((list, isEnd) -> 
             new Runnable() {
                @Override
                public void run() {
                    summer.addAndGet(list.size());
                    System.out.println(list.size() + "==" + Thread.currentThread().getId() + "-" + Thread.currentThread().getName() + ", " + isEnd);
                    if (isEnd) {
                        end.set(true);
                    }
                    //System.out.println(1 / 0); // submit方式不会打印异常
                }
            }
        );

        AtomicBoolean flag = new AtomicBoolean(true);
        AtomicInteger increment = new AtomicInteger(0);
        int n = 4;
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                while (flag.get()) {
                    transmitter.put(increment.incrementAndGet());
                    try {
                        Thread.sleep(11 + ThreadLocalRandom.current().nextInt(19));
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
        transmitter.end();
        for (Thread thread : threads) {
            thread.join();
        }
        while (!end.get()) {
            // noop-loop
        }
        System.out.println(increment.get());
        System.out.println(summer.get());
        Assert.assertEquals(increment.get(), summer.get());
    }
}
