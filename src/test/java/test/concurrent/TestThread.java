package test.concurrent;

import java.util.Date;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class TestThread {

    private static final Date ORIGIN_DATE = toDate("1950-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
    private static final long ORIGIN_DATE_TIME = ORIGIN_DATE.getTime();

    private static final Lock LOCK = new ReentrantLock();
    private static volatile ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static volatile Map<String, Boolean> lastTwoBatch = new ConcurrentHashMap<>();

    public static void loop(int num) {
        Lock readLock = readWriteLock.readLock();
        for (int i = 0; i < num; i++) {
            readLock.lock();
            lastTwoBatch.put(randomBirthday(), true);
            readLock.unlock();
        }
        String lastButOne = null;
        LOCK.lock();
        try {
            if (lastTwoBatch.size() > 2) {
                Map<String, Boolean> temp = lastTwoBatch;
                lastTwoBatch = new ConcurrentHashMap<>();
                
                Lock writeLock = readWriteLock.writeLock();
                readWriteLock = new ReentrantReadWriteLock();
                writeLock.lock();
                TreeSet<String> set = new TreeSet<>(temp.keySet());
                temp.clear();
                writeLock.unlock();

                String lastOne = set.pollLast();
                lastButOne = set.pollLast();
                lastTwoBatch.put(lastOne, true);
                lastTwoBatch.put(lastButOne, true);
                set.clear();
            }
        } finally {
            LOCK.unlock();
        }
    }

    public static Date toDate(String dateStr, String pattern) {
        return DateTimeFormat.forPattern(pattern).parseDateTime(dateStr).toDate();
    }

    private static String randomBirthday() {
        long diffSeconds = (new Date().getTime() - ORIGIN_DATE_TIME) / 1000;
        long date = new DateTime(ORIGIN_DATE_TIME).plusSeconds((int) (Math.random() * diffSeconds)).getMillis();
        return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss"); // 2017-04-26 16:59:29
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        int num = 5000;
        CountDownLatch latch = new CountDownLatch(num);
        for (int i = 0; i < num; i++) {
            new Thread(() -> {
                loop(10);
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println(lastTwoBatch);
        System.out.println(System.currentTimeMillis() - start);
    }
}
