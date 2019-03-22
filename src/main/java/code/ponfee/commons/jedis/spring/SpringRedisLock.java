package code.ponfee.commons.jedis.spring;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import com.google.common.base.Preconditions;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.util.Bytes;

import javax.annotation.Nonnull;

/**
 * <pre>
 * class X {
 *   public void m() {
 *     Lock lock = new SpringRedisLock(redis, "lockKey", 5);
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
 *     Lock lock = new SpringRedisLock(redis, "lockKey", 5);
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
 *     Lock lock = new SpringRedisLock(redis, "lockKey", 5);
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
 * 使用redis transaction功能实现
 * 
 * @author Ponfee
 */
public class SpringRedisLock implements Lock, java.io.Serializable {

    private static final long serialVersionUID = -6209919116306827731L;
    private static Logger logger = LoggerFactory.getLogger(SpringRedisLock.class);

    private static final int MAX_TOMEOUT_SECONDS = 86400; // 最大超 时为1天
    private static final int MIN_TOMEOUT_SECONDS = 1; // 最小超 时为1秒
    private static final int MIN_SLEEP_MILLIS = 9; // 最小休眠时间为9毫秒
    private static final String KEY_PREFIX = "lock:";
    private static final transient ThreadLocal<byte[]> LOCK_VALUE = new ThreadLocal<>();

    private final transient RedisTemplate<String, byte[]> redis;
    private final String lockKey;
    private final int timeoutSeconds; // 锁的超时时间，防止死锁
    private final long timeoutMillis;
    private final long sleepMillis;

    public SpringRedisLock(RedisTemplate<String, byte[]> redis, String lockKey) {
        this(redis, lockKey, MAX_TOMEOUT_SECONDS);
    }

    public SpringRedisLock(RedisTemplate<String, byte[]> redis, 
                           String lockKey, int timeoutSeconds) {
        this(redis, lockKey, timeoutSeconds, 9);
    }

    /**
     * 锁对象构造函数
     * @param redis              spring redis template
     * @param lockKey            待加锁的键
     * @param timeoutSeconds     锁超时时间（防止死锁）
     * @param sleepMillis        休眠时间（毫秒）
     */
    public SpringRedisLock(RedisTemplate<String, byte[]> redis, String lockKey, 
                           int timeoutSeconds, int sleepMillis) {
        Preconditions.checkArgument(redis != null, "jedis template cannot be null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(lockKey), "lock key cannot be null");

        this.redis = redis;
        this.lockKey = KEY_PREFIX + lockKey; // add prefix key by "jedis:lock:"
        timeoutSeconds = Math.abs(timeoutSeconds);
        if (timeoutSeconds > MAX_TOMEOUT_SECONDS) {
            timeoutSeconds = MAX_TOMEOUT_SECONDS;
        } else if (timeoutSeconds < MIN_TOMEOUT_SECONDS) {
            timeoutSeconds = MIN_TOMEOUT_SECONDS;
        }
        this.timeoutSeconds = timeoutSeconds;
        this.timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);
        this.sleepMillis = Numbers.bounds(sleepMillis, MIN_SLEEP_MILLIS, (int) timeoutMillis);
    }

    /**
     * 等待锁直到获取
     */
    public @Override void lock() {
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
    public @Override void lockInterruptibly() throws InterruptedException {
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public @Override boolean tryLock() {
        BoundValueOperations<String, byte[]> valueOps = redis.boundValueOps(lockKey);

        byte[] lockValue = generateValue();
        Boolean result = valueOps.setIfAbsent(lockValue); // 竞争锁
        // 仅当lockKey不存在才能设置成功并返回true，否则setnx不做任何动作返回false
        if (result != null && result) {
            // 成功获取锁后需要设置失效期
            LOCK_VALUE.set(lockValue);
            valueOps.expire(timeoutSeconds, TimeUnit.SECONDS);
            return true;
        }

        return (boolean) redis.execute((SessionCallback) redisOps -> {
            redisOps.watch(lockKey); // 监视lockKey
            BoundValueOperations<String, byte[]> valOps = redisOps.boundValueOps(lockKey);
            byte[] value = valOps.get(); // 获取当前锁值
            if (value == null || value.length == 0) {
                redisOps.unwatch();
                return tryLock(); // 锁被释放，重新获取
            } else if (System.currentTimeMillis() <= parseValue(value)) {
                redisOps.unwatch();
                return Arrays.equals(LOCK_VALUE.get(), value); // 锁未超时则判断是否当前线程持有（可重入锁）
            } else {
                byte[] lockVal = generateValue();
                redisOps.multi();
                valOps.getAndSet(lockVal);
                valOps.expire(timeoutSeconds, TimeUnit.SECONDS);
                // 锁已超时，争抢锁（事务控制）
                List<Object> res = redisOps.exec(); // exec执行完后被监控的key会自动unwatch
                if (   CollectionUtils.isNotEmpty(res)
                    && Arrays.equals(value, (byte[]) res.get(0))
                ) {
                    LOCK_VALUE.set(lockVal);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * 尝试获取锁，成功返回true，失败返回false
     * 线程中断则抛出interrupted异常
     */
    public @Override boolean tryLock(long timeout, @Nonnull TimeUnit unit)
        throws InterruptedException {
        timeout = unit.toNanos(timeout);
        long startTime = System.nanoTime();
        for (;;) {
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
    @SuppressWarnings({ "unchecked" })
    public @Override void unlock() {
        doIfHoldLock(redisOps -> redisOps.delete(lockKey));
    }

    @SuppressWarnings({ "unchecked" })
    public void expireLock(int timeoutSeconds) {
        doIfHoldLock(redisOps -> redisOps.expire(lockKey, timeoutSeconds, TimeUnit.SECONDS));
    }

    public void delLock() {
        redis.delete(lockKey);
    }

    public @Override @Nonnull Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    /**
     * <pre> 
     *  {@code
     *     class X {
     *       Lock lock = new SpringRedisLock(redis, "lockKey", 5);
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
        return value != null && Arrays.equals(value, redis.opsForValue().get(lockKey));
    }

    /**
     * 是否已锁（任何线程）
     * @return
     */
    public boolean isLocked() {
        return redis.opsForValue().get(lockKey) != null;
    }

    /**
     * 解析值数据
     * @param value
     * @return
     */
    private long parseValue(byte[] value) {
        return Bytes.toLong(value, 16); // long value after 16 byte uuid
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void doIfHoldLock(Consumer<RedisOperations> action) {
        redis.execute((SessionCallback) redisOps -> {
            redisOps.watch(lockKey);
            BoundValueOperations<String, byte[]> valOps = redisOps.boundValueOps(lockKey);
            byte[] value = LOCK_VALUE.get(); // 获取当前线程保存的锁值
            if (value == null || !Arrays.equals(value, valOps.get())) {
                // 当前线程未获取过锁或锁已被其它线程获取
                redisOps.unwatch();
            } else {
                // 当前线程持有锁，需要释放锁
                redisOps.multi();
                action.accept(redisOps);
                redisOps.exec(); // 自动unwatch
            }
            LOCK_VALUE.remove(); // 删除
            return null;
        });
    }

    /**
     * 获取锁值
     * @return
     */
    private byte[] generateValue() {
        UUID uuid = UUID.randomUUID();
        long most  = uuid.getMostSignificantBits(), 
             least = uuid.getLeastSignificantBits(),
             time  = System.currentTimeMillis() + timeoutMillis;

        return new byte[] {
            (byte) (most  >>> 56), (byte) (most  >>> 48),
            (byte) (most  >>> 40), (byte) (most  >>> 32),
            (byte) (most  >>> 24), (byte) (most  >>> 16),
            (byte) (most  >>>  8), (byte) (most        ),

            (byte) (least >>> 56), (byte) (least >>> 48),
            (byte) (least >>> 40), (byte) (least >>> 32),
            (byte) (least >>> 24), (byte) (least >>> 16),
            (byte) (least >>>  8), (byte) (least       ),

            (byte) (time  >>> 56), (byte) (time  >>> 48),
            (byte) (time  >>> 40), (byte) (time  >>> 32),
            (byte) (time  >>> 24), (byte) (time  >>> 16),
            (byte) (time  >>>  8), (byte) (time        )
        };
    }

}
