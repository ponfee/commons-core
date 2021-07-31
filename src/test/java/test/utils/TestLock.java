package test.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestLock {
    public static void main(String[] args) throws InterruptedException {
        Lock lock = new ReentrantLock();

        Thread t1 = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("t1...");
                //Thread.sleep(150);
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(100);
                lock.lock();
                System.out.println("t2...");
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t2");

        Thread t3 = new Thread(() -> {
            try {
                Thread.sleep(200);
                lock.lock();
                System.out.println("t3...");
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t3");

        Thread t4 = new Thread(() -> {
            try {
                Thread.sleep(300);
                lock.lock();
                System.out.println("t4...");
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t4");

        Thread t5 = new Thread(() -> {
            try {
                Thread.sleep(400);
                lock.lock();
                System.out.println("t5...");
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t5");

        Thread t6 = new Thread(() -> {
            try {
                Thread.sleep(500);
                lock.lock();
                System.out.println("t6...");
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t6");

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();


        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        t6.join();
    }



}
