package code.ponfee.commons.jedis.spring;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.google.common.base.Preconditions;

import code.ponfee.commons.jedis.JedisLock;
import code.ponfee.commons.jedis.JedisOperations;
import code.ponfee.commons.jedis.ValueOperations;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.ObjectUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

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
 * 使用redis lua script功能实现
 * 
 * @author Ponfee
 */
public class SpringRedisLock implements Lock, java.io.Serializable {

    private static final long serialVersionUID = -6209919116306827731L;
    private static Logger logger = LoggerFactory.getLogger(SpringRedisLock.class);

    private static final byte[] EX_BYTES = ValueOperations.EX.getBytes();
    private static final byte[] NX_BYTES = ValueOperations.NX.getBytes();
    private static final byte[] UNLOCK_SCRIPT_BYTES = JedisLock.UNLOCK_SCRIPT.getBytes();
    private static final Long UNLOCK_SUCCESS = 1L;

    private static final int MAX_TOMEOUT_SECONDS = 86400; // 最大超 时为1天
    private static final int MIN_TOMEOUT_SECONDS = 1; // 最小超 时为1秒
    private static final int MIN_SLEEP_MILLIS = 9; // 最小休眠时间为9毫秒
    private static final byte[] KEY_PREFIX = "lock:".getBytes();

    private static final transient ThreadLocal<byte[]> LOCK_VALUE = new ThreadLocal<>();

    private final transient RedisTemplate<byte[], byte[]> redisTemplate;
    private final byte[] lockKey;
    private final int timeoutSeconds; // 锁的超时时间，防止死锁
    private final long sleepMillis;

    public SpringRedisLock(RedisTemplate<byte[], byte[]> redis, String lockKey) {
        this(redis, lockKey, MAX_TOMEOUT_SECONDS);
    }

    public SpringRedisLock(RedisTemplate<byte[], byte[]> redis, 
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
    public SpringRedisLock(RedisTemplate<byte[], byte[]> redis, String lockKey, 
                           int timeoutSeconds, int sleepMillis) {
        Preconditions.checkArgument(redis != null, "jedis template cannot be null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(lockKey), "lock key cannot be null");

        this.redisTemplate = redis;
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
        return redisTemplate.execute((RedisCallback<Boolean>) conn -> {
            byte[] lockValue = ObjectUtils.uuid();
            String result = getJedis(conn, lockKey).set(
                lockKey, lockValue, NX_BYTES, EX_BYTES, timeoutSeconds
            );
            if (JedisOperations.SUCCESS_MSG.equals(result)) {
                LOCK_VALUE.set(lockValue);
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        });
    }

    /**
     * 尝试获取锁，成功返回true，失败返回false
     * 线程中断则抛出interrupted异常
     */
    @Override
    public boolean tryLock(long timeout, @Nonnull TimeUnit unit)
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
    @Override
    public void unlock() {
        final byte[] lockValue = LOCK_VALUE.get();
        if (lockValue == null) {
            return;
        }
        redisTemplate.execute((RedisCallback<Boolean>) conn -> {
            Object result = getJedis(conn, lockKey).eval(
                UNLOCK_SCRIPT_BYTES, singletonList(lockKey), singletonList(lockValue)
            );
            LOCK_VALUE.remove();
            return UNLOCK_SUCCESS.equals(result);
        });
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
        return value != null && Arrays.equals(value, redisTemplate.opsForValue().get(lockKey));
    }

    /**
     * 是否已锁（任何线程）
     * @return
     */
    public boolean isLocked() {
        return redisTemplate.opsForValue().get(lockKey) != null;
    }

    public void forceUnlock(int seconds) {
        redisTemplate.delete(lockKey);
    }

    // ----------------------------------------------------------------------------------private methods
    private Jedis getJedis(RedisConnection conn, byte[] key) {
        Object object = conn.getNativeConnection();
        Objects.requireNonNull(object);
        if (object instanceof Jedis) {
            return (Jedis) object;
        } else if (object instanceof ShardedJedis) {
            return ((ShardedJedis) object).getShard(key);
        } else {
            throw new UnsupportedOperationException("Unsupported " + object.getClass());
        }
    }

}
