package test.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class TestInterrupt {

    public static void main(String[] args) throws InterruptedException {
        Lock lock = new ReentrantLock();


        Thread t1 = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("t1 start..");
                Thread.sleep(3000);
                System.out.println("t1 end..");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //lock.unlock();
            }
        });

        Thread t2 = new Thread(() -> {
            System.out.println("t2 start..."+Thread.currentThread().isInterrupted());
            try {
                //lock.lockInterruptibly(); // 会感知中断，会抛出InterruptedException，会重置中断状态（false）
                LockSupport.park(); // 会感知中断，不会抛异常，不会重置中断状态（true）
                /*long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 2000) {
                    // 正常执行情况（非阻塞）
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("t2 end..."+Thread.currentThread().isInterrupted());
        });

        t1.start();
        Thread.sleep(100);
        t2.start();
        Thread.sleep(100);

        t2.interrupt();
        t1.join();
        t2.join();
        System.out.println(t2.isInterrupted());
    }
}
