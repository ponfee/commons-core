package code.ponfee.commons.limit.current;

import static code.ponfee.commons.concurrent.ThreadPoolExecutors.CALLER_RUN_SCHEDULER;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import code.ponfee.commons.cache.Cache;
import code.ponfee.commons.cache.CacheBuilder;
import code.ponfee.commons.concurrent.AsyncBatchTransmitter;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.JedisLock;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.IdWorker;

/**
 * Redis限流器
 *
 * @author Ponfee
 */
public class RedisCurrentLimiter implements CurrentLimiter {

    private static Logger logger = LoggerFactory.getLogger(RedisCurrentLimiter.class);

    private static final int EXPIRE_SECONDS = (int) TimeUnit.DAYS.toSeconds(30) + 1; // key的失效日期
    private static final String TRACE_KEY_PREFIX = "cir:bre:"; // 频率缓存key前缀
    private static final byte[] TRACE_KEY_BYTES = TRACE_KEY_PREFIX.getBytes(); // 频率缓存key前缀
    private static final String THRESHOLD_KEY_PREFIX = "freq:thrd:"; // 限制次数缓存key前缀
    private static final Map<String, Object> LOCK_MAP = new HashMap<>(); // the map for store lock object
    private final JedisClient jedisClient;
    private final JedisLock lock;
    private final AsyncBatchTransmitter<Trace> transmitter;
    private final int clearBeforeMillis;

    private final Cache<String, Long> confCache = CacheBuilder.<String, Long>newBuilder()
        .keepaliveInMillis(120000L) // 2 minutes of cache alive
        .autoReleaseInSeconds(1800).build(); // 30 minutes to release expire cache

    private final Cache<String, Long> countCache = CacheBuilder.<String, Long>newBuilder()
        .keepaliveInMillis(500L) // 500 millis of cache alive
        .autoReleaseInSeconds(1800).build(); // 30 minutes to release expire cache

    public RedisCurrentLimiter(JedisClient jedisClient, int clearBeforeMinutes, int autoClearInSeconds) {
        this.jedisClient = jedisClient;
        this.clearBeforeMillis = (int) TimeUnit.MINUTES.toMillis(clearBeforeMinutes);

        // 定时清除记录(zrem range by score)，jedis:lock:cir:bre:clear
        this.lock = new JedisLock(jedisClient, TRACE_KEY_PREFIX + "clear", autoClearInSeconds >>> 1);
        CALLER_RUN_SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                if (this.lock.tryLock()) { // 不用释放锁，让其自动超时
                    long beforeTimeMillis = System.currentTimeMillis() - clearBeforeMillis;
                    for (String key : jedisClient.keysOps().keys(TRACE_KEY_PREFIX + "*")) {
                        jedisClient.zsetOps().zremrangeByScore(key, 0, beforeTimeMillis);
                    }
                }
            } catch (Throwable t) {
                logger.error("JedisLock tryLock occur error", t);
            }
        }, autoClearInSeconds, autoClearInSeconds, TimeUnit.SECONDS);

        // 批量记录
        this.transmitter = new AsyncBatchTransmitter<>((traces, isEnd) -> () -> {
            Map<String, Map<byte[], Double>> groups = new HashMap<>();
            Map<byte[], Double> batch;
            for (Trace trace : traces) {
                batch = groups.computeIfAbsent(trace.key, k -> new HashMap<>());
                // ObjectUtils.uuid22()
                // Long.toString(IdWorker.LOCAL_WORKER.nextId(), Character.MAX_RADIX)
                batch.put(Bytes.toBytes(IdWorker.LOCAL_WORKER.nextId()), trace.timeMillis);
            }

            /*for (Entry<String, Map<byte[], Double>> entry : groups.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    continue;
                }
                // TRACE_KEY_PREFIX + trace.key
                jedisClient.zsetOps().zadd(
                    concat(TRACE_KEY_BYTES, entry.getKey().getBytes(UTF_8)),
                    entry.getValue(), EXPIRE_SECONDS
                );
                entry.getValue().clear();
            }*/
            jedisClient.executePipelined(
                (pipeline, e) -> {
                    Map<byte[], Double> map = e.getValue();
                    if (MapUtils.isNotEmpty(map)) {
                        byte[] key = concat(TRACE_KEY_BYTES, e.getKey().getBytes(UTF_8));
                        pipeline.zadd(key, map);
                        pipeline.expire(key, EXPIRE_SECONDS);
                        map.clear();
                    }
                }, 
                groups.entrySet()
            );

            groups.clear();
            traces.clear();
        }, 100, 5000); // 100毫秒间隔，5000条∕次
    }

    /**
     * 校验并记录
     * @param key
     * @return 是否频繁访问：true是；false否；
     */
    @Override
    public boolean checkpoint(String key) {
        return checkpoint(key, getRequestThreshold(key));
    }

    @Override
    public boolean checkpoint(String key, long requestThreshold) {
        if (requestThreshold < 0) {
            return true; // 小于0表示无限制
        } else if (requestThreshold == 0) {
            return false; // 禁止访问
        }

        // 超过频率
        boolean flag = requestThreshold >= countByLastTime(key, 1, TimeUnit.MINUTES);
        if (flag) {
            transmitter.put(new Trace(key, System.currentTimeMillis()));
        }
        return flag;
    }

    public long countByLastTime(String key, int time, TimeUnit unit) {
        long millis = unit.toMillis(time);
        String key0 = new StringBuilder(key).append(':').append(millis).toString();
        Long count = countCache.get(key0);
        if (count == null) {
            synchronized (getLock(key0)) {
                if ((count = countCache.get(key0)) == null) {
                    long now = System.currentTimeMillis();
                    // load the freq from cache, if not hit then calculate by redis zcount
                    count = countByRangeMillis(key, now - millis, now);
                    countCache.set(key0, count);
                }
            }
        }
        return count;
    }

    @Override
    public long countByRange(String key, Date from, Date to) {
        return countByRangeMillis(key, from.getTime(), to.getTime());
    }

    /**
     * 限制一分钟的访问频率
     * @param key
     * @param threshold
     * @return 是否设置成功：true是；false否；
     */
    @Override
    public boolean setRequestThreshold(String key, long threshold) {
        boolean flag = jedisClient.valueOps().setLong(THRESHOLD_KEY_PREFIX + key,
                                                      threshold, EXPIRE_SECONDS);
        if (flag) {
            confCache.set(key, threshold); // refresh key value
        }

        return flag;
    }

    /**
     * 获取一分钟的限制频率量
     * @param key
     * @return
     */
    @Override
    public long getRequestThreshold(String key) {
        Long threshold = confCache.get(key);
        if (threshold == null) {
            synchronized (getLock(key)) {
                if ((threshold = confCache.get(key)) == null) {
                    threshold = jedisClient.valueOps()
                                           .getLong(THRESHOLD_KEY_PREFIX + key, EXPIRE_SECONDS);
                    if (threshold == null) {
                        threshold = -1L; // -1表示无限制
                    }
                    confCache.set(key, threshold); // put into local cache
                }
            }
        }
        return threshold;
    }

    /**
     * 销毁
     */
    public void destory() {
        confCache.destroy();
        countCache.destroy();
        transmitter.end();
        synchronized (LOCK_MAP) {
            LOCK_MAP.clear();
        }
    }

    private Object getLock(String key) {
        // get the lock for access redis
        Object lock = LOCK_MAP.get(key);
        if (lock == null) {
            synchronized (LOCK_MAP) {
                lock = LOCK_MAP.computeIfAbsent(key, k -> new Object());
            }
        }
        return lock;
    }

    /**
     * 查询指定时间段的访问次数
     * @param key
     * @param fromMillis
     * @param toMillis
     * @return  the threshold of this time range
     */
    private long countByRangeMillis(String key, long fromMillis, long toMillis) {
        Preconditions.checkArgument(fromMillis < toMillis, "from time must before to time.");
        return jedisClient.zsetOps().zcount(TRACE_KEY_PREFIX + key, fromMillis, toMillis);
    }

    private static class Trace {
        final String key;
        final double timeMillis;

        Trace(String key, double timeMillis) {
            this.key = key;
            this.timeMillis = timeMillis;
        }
    }

    private static byte[] concat(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

}
