package test.concurrent;

public class ReadWriteLock {

    private int readThreadCounter = 0; // 正在读取的线程数（0个或多个）
    private int waitingWriteCounter = 0; // 等待写入的线程数（0个或多个）
    private int writeThreadCounter = 0; // 正在写入的线程数（0个或1个）
    private boolean writable = true; // 是否对写入优先（默认为是）

    private ReadWriteLock() {}

    public static ReadWriteLock create() {
        return new ReadWriteLock();
    }

    // 读取加锁
    public synchronized void readLock() throws InterruptedException {
        // 若存在正在写入的线程，或当写入优先时存在等待写入的线程，则将当前线程设置为等待状态
        while (writeThreadCounter > 0 || (writable && waitingWriteCounter > 0)) {
            wait();
        }
        // 使正在读取的线程数加一
        readThreadCounter++;
    }

    // 读取解锁
    public synchronized void readUnlock() {
        // 使正在读取的线程数减一
        readThreadCounter--;
        // 读取结束，对写入优先
        writable = true;
        // 通知所有处于 wait 状态的线程
        notifyAll();
    }

    // 写入加锁
    public synchronized void writeLock() throws InterruptedException {
        // 使等待写入的线程数加一
        waitingWriteCounter++;
        try {
            // 若存在正在读取的线程，或存在正在写入的线程，则将当前线程设置为等待状态
            while (readThreadCounter > 0 || writeThreadCounter > 0) {
                wait();
            }
        } finally {
            // 使等待写入的线程数减一
            waitingWriteCounter--;
        }
        // 使正在写入的线程数加一
        writeThreadCounter++;
    }

    // 写入解锁
    public synchronized void writeUnlock() {
        // 使正在写入的线程数减一
        writeThreadCounter--;
        // 写入结束，对读取优先
        writable = false;
        // 通知所有处于等待状态的线程
        notifyAll();
    }
}
