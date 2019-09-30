package code.ponfee.commons.jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import code.ponfee.commons.concurrent.ThreadPoolExecutors;
import code.ponfee.commons.math.Numbers;
import redis.clients.jedis.ShardedJedis;

/**
 * jedis操作抽象类
 * 
 * @author Ponfee
 */
public abstract class JedisOperations {

    private static final int MIN_EXPIRE_SECONDS = 1; // minimum 1 seconds
    static final int DEFAULT_EXPIRE_SECONDS = 86400; // default 1 days
    private static final int MAX_EXPIRE_SECONDS = 30 * DEFAULT_EXPIRE_SECONDS; // maximum 30 days
    public static final String SUCCESS_MSG = "OK"; // 返回成功信息
    //static final int FUTURE_TIMEOUT = 1500; // future task timeout milliseconds
    static final int BATCH_MULTIPLE = 1; // the multiple jedis server number to use batch

    static final ExecutorService EXECUTOR = ThreadPoolExecutors.INFINITY_QUEUE_EXECUTOR;

    final JedisClient jedisClient;

    JedisOperations(JedisClient jedisClient) {
        this.jedisClient = jedisClient;
    }

    /**
     * 回调函数：有返回值
     * @param call             回调函数
     * @param occurErrorRtnVal 出现异常时的返回值
     * @param args             参数
     * @return
     */
    final <T> T call(JedisCallback<T> call, T occurErrorRtnVal, Object... args) {
        return call.call(jedisClient, occurErrorRtnVal, args);
    }

    /**
     * 调用勾子函数：无返回值
     * @param hook 勾子对象
     * @param args 参数列表
     */
    final void hook(JedisHook hook, Object... args) {
        hook.hook(jedisClient, args);
    }

    // --------------------------------------------------------------expire
    /**
     * 设置过期时间，若seconds为null则不做处理
     * @param shardedJedis
     * @param key
     * @param seconds
     * @return
     */
    public static boolean expire(ShardedJedis shardedJedis, String key, Integer seconds) {
        if (seconds == null) {
            return false;
        }

        // reply：设置成功返回1，未设置成功返回（key不存在）0；
        return Numbers.equals(shardedJedis.expire(key, getActualExpire(seconds)), 1);
    }

    public static boolean expire(ShardedJedis shardedJedis, byte[] key, Integer seconds) {
        return seconds != null
            && Numbers.equals(shardedJedis.expire(key, getActualExpire(seconds)), 1);
    }

    /**
     * 设置过期时间，若seconds为null且无失效期限则设置默认失效时间
     * @param shardedJedis
     * @param key
     * @param seconds
     * @return
     */
    public static boolean expireForce(ShardedJedis shardedJedis, String key, Integer seconds) {
        if (seconds != null) {
            return Numbers.equals(shardedJedis.expire(key, getActualExpire(seconds)), 1);
        } else {
            return expireDefaultIfInfinite(shardedJedis, key);
        }
    }

    public static boolean expireForce(ShardedJedis shardedJedis, byte[] key, Integer seconds) {
        if (seconds != null) {
            return Numbers.equals(shardedJedis.expire(key, getActualExpire(seconds)), 1);
        } else {
            return expireDefaultIfInfinite(shardedJedis, key);
        }
    }

    /**
     * 防止内存泄露：如果无失效期，则设置默认失效时间
     * @param shardedJedis
     * @param key
     */
    public static boolean expireDefaultIfInfinite(ShardedJedis shardedJedis, String key) {
        return shardedJedis.ttl(key) == -1
            && Numbers.equals(shardedJedis.expire(key, DEFAULT_EXPIRE_SECONDS), 1);
    }

    public static boolean expireDefaultIfInfinite(ShardedJedis shardedJedis, byte[] key) {
        return shardedJedis.ttl(key) == -1
            && Numbers.equals(shardedJedis.expire(key, DEFAULT_EXPIRE_SECONDS), 1);
    }

    public static int getActualExpire(int seconds) {
        return (seconds > MAX_EXPIRE_SECONDS)
               ? MAX_EXPIRE_SECONDS
               : (seconds < MIN_EXPIRE_SECONDS)
               ? MIN_EXPIRE_SECONDS 
               : seconds;
    }

    // ------------------------------------------------------------------pexpire
    /**
     * 设置过期时间，若milliseconds为null则不做处理
     * @param shardedJedis
     * @param key
     * @param milliseconds
     * @return
     */
    public static boolean pexpire(ShardedJedis shardedJedis, String key, Integer milliseconds) {
        return milliseconds != null
            && expire(shardedJedis, key, (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds));

    }

    public static boolean pexpire(ShardedJedis shardedJedis, byte[] key, Integer milliseconds) {
        return milliseconds != null
            && expire(shardedJedis, key, (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds));

    }

    /**
     * 设置过期时间，若milliseconds为null且无失效期限则设置默认失效时间
     * @param shardedJedis
     * @param key
     * @param milliseconds
     * @return
     */
    public static boolean pexpireForce(ShardedJedis shardedJedis, 
                                       String key, Integer milliseconds) {
        if (milliseconds != null) {
            return pexpire(shardedJedis, key, milliseconds);
        } else {
            return expireDefaultIfInfinite(shardedJedis, key);
        }
    }

    public static boolean pexpireForce(ShardedJedis shardedJedis, 
                                       byte[] key, Integer milliseconds) {
        if (milliseconds != null) {
            return pexpire(shardedJedis, key, milliseconds);
        } else {
            return expireDefaultIfInfinite(shardedJedis, key);
        }
    }

}
