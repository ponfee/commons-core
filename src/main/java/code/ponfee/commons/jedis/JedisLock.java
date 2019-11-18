package code.ponfee.commons.jedis;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.ObjectUtils;

/**
 * <pre>
 * class X {
 *   public void m() {
 *     Lock lock = new JedisLock(jedisClient, "lockKey", 5);
 *     lock.lock();  // block until acquire lock or timeout
 *     try {
 *       // ... method body
 *     } finally {
 *       lock.unlock()
 *     }
 *   }
 * }
 * 
 * class Y {
 *   public void m() {
 *     Lock lock = new JedisLock(jedisClient, "lockKey", 5);
 *     if (!lock.tryLock()) return;
 *     try {
 *       // ... method body
 *     } finally {
 *       lock.unlock();
 *     }
 *   }
 * }
 * 
 * class Z {
 *   public void m() {
 *     Lock lock = new JedisLock(jedisClient, "lockKey", 5);
 *     // auto timeout release lock
 *     if (!lock.tryLock(100, TimeUnit.MILLISECONDS)) return;
 *     try {
 *       // ... method body
 *     } finally {
 *       lock.unlock();
 *     }
 *   }
 * }
 * </pre>
 * 
 * 基于redis的分布式锁
 * 使用redis lua script功能实现
 * 
 * http://www.54tianzhisheng.cn/2018/04/24/Distributed_lock/
 * 
 * @author Ponfee
 */
public class JedisLock implements Lock, java.io.Serializable {

    private static final long serialVersionUID = -6209919116306827731L;
    private static Logger logger = LoggerFactory.getLogger(JedisLock.class);

    public static final String UNLOCK_SCRIPT =
        "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end;";
    private static final byte[] UNLOCK_SCRIPT_BYTES = UNLOCK_SCRIPT.getBytes();

    private static final int MAX_TOMEOUT_SECONDS = 86400; // 最大超 时为1天
    private static final int MIN_TOMEOUT_SECONDS = 1; // 最小超 时为1秒
    private static final int MIN_SLEEP_MILLIS = 7; // 最小休眠时间为7毫秒
    private static final byte[] KEY_PREFIX = "lock:".getBytes();

    // https://github.com/alibaba/transmittable-thread-local
    private static final transient ThreadLocal<byte[]> LOCK_VALUE = new ThreadLocal<>();

    private final transient JedisClient jedisClient;
    private final byte[] lockKey;
    private final int timeoutSeconds; // 锁的超时时间，防止死锁
    private final long sleepMillis;

    public JedisLock(JedisClient jedisClient, String lockKey) {
        this(jedisClient, lockKey, MAX_TOMEOUT_SECONDS);
    }

    public JedisLock(JedisClient jedisClient, String lockKey, int timeoutSeconds) {
        this(jedisClient, lockKey, timeoutSeconds, 9);
    }

    /**
     * 锁对象构造函数
     * @param jedisClient        jedisClient实例
     * @param lockKey            待加锁的键
     * @param timeoutSeconds     锁超时时间（防止死锁）
     * @param sleepMillis        休眠时间（毫秒）
     */
    public JedisLock(JedisClient jedisClient, String lockKey, int timeoutSeconds, int sleepMillis) {
        Preconditions.checkArgument(jedisClient != null, "jedis client cannot be null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(lockKey), "lock key cannot be null");

        this.jedisClient = jedisClient;
        this.lockKey = Bytes.concat(KEY_PREFIX, lockKey.getBytes()); // add prefix key by "jedis:lock:"
        timeoutSeconds = Math.abs(timeoutSeconds);
        if (timeoutSeconds > MAX_TOMEOUT_SECONDS) {
            timeoutSeconds = MAX_TOMEOUT_SECONDS;
        } else if (timeoutSeconds < MIN_TOMEOUT_SECONDS) {
            timeoutSeconds = MIN_TOMEOUT_SECONDS;
        }
        this.timeoutSeconds = timeoutSeconds;
        this.sleepMillis = Numbers.bounds(
            sleepMillis, MIN_SLEEP_MILLIS, 
            (int) TimeUnit.SECONDS.toMillis(timeoutSeconds)
        );
    }

    /**
     * 等待锁直到获取
     */
    @Override
    public void lock() {
        while (!tryLock()) {
            try {
                TimeUnit.MILLISECONDS.sleep(sleepMillis);
            } catch (InterruptedException e) {
                logger.error("jedis lock sleep occur interrupted exception", e);
            }
        }
    }

    /**
     * 等待锁直到获取成功或抛出InterruptedException异常
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        for (;;) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (tryLock()) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(sleepMillis); // to sleep for prevent endless loop
        }
    }

    /**
     * 尝试获取锁，成功返回true，失败返回false
     */
    @Override
    public boolean tryLock() {
        byte[] lockValue = ObjectUtils.uuid();
        if (jedisClient.valueOps().setnx(lockKey, lockValue, timeoutSeconds)) {
            LOCK_VALUE.set(lockValue);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 尝试获取锁，成功返回true，失败返回false
     * 线程中断则抛出interrupted异常
     */
    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        timeout = unit.toNanos(timeout);
        long startTime = System.nanoTime();
        for (;;) {
            // Thread.currentThread().isInterrupted(): ClearInterrupted -> false
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (tryLock()) {
                return true;
            }
            if (System.nanoTime() - startTime > timeout) {
                return false; // 等待超时则返回
            }
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        }
    }

    /**
     * 释放锁
     */
    @Override
    public void unlock() {
        byte[] lockValue = LOCK_VALUE.get();
        if (lockValue != null) {
            jedisClient.scriptOps().eval(lockKey, UNLOCK_SCRIPT_BYTES, Collections.singletonList(lockValue));
            LOCK_VALUE.remove();
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------------------extends public methods
    /**
     * <pre> 
     *  {@code
     *     class X {
     *       Lock lock = new JedisLock(jedisClient, "lockKey", 5);
     *       // ...
     *       public void m() {
     *         assert !lock.isHeldByCurrentThread();
     *         lock.lock();
     *         try {
     *             // ... method body
     *         } finally {
     *             lock.unlock();
     *         }
     *       }
     *     }
     *  }
     * </pre>
     * 当前线程是否持有锁
     * @return
     */
    public boolean isHeldByCurrentThread() {
        byte[] value = LOCK_VALUE.get();
        return value != null && Arrays.equals(value, jedisClient.valueOps().get(lockKey));
    }

    /**
     * 是否已锁（任何线程）
     * @return
     */
    public boolean isLocked() {
        return jedisClient.valueOps().get(lockKey) != null;
    }

    public void forceUnlock() {
        jedisClient.keysOps().del(lockKey);
    }

}
