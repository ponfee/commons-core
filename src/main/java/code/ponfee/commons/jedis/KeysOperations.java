package code.ponfee.commons.jedis;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;

import code.ponfee.commons.math.Numbers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedisPipeline;

/**
 * redis key（键）操作类
 * 
 * @author Ponfee
 */
public class KeysOperations extends JedisOperations {

    KeysOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * 当key不存在时，返回 -2
     * 当key存在但没有设置剩余生存时间时，返回 -1
     * 否则以秒为单位，返回 key的剩余生存时间
     * @param key
     * @return
     */
    public Long ttl(String key) {
        return ((JedisCallback<Long>) sjedis -> sjedis.ttl(key)).call(jedisClient, null, key);
    }

    /**
     * 获取有效期
     * @param key
     * @return
     */
    public Long pttl(String key) {
        return call(sjedis -> sjedis.pttl(key), null, key);
    }

    /**
     * 获取key列表
     * @param keyWildcard
     * @return
     */
    public Set<String> keys(String keyWildcard) {
        Stream<Set<String>> stream = jedisClient.executeSharded(jedis -> jedis.keys(keyWildcard));

        return stream.filter(CollectionUtils::isNotEmpty)
                     .collect(HashSet::new, HashSet::addAll, HashSet::addAll);
    }

    /**
     * 设置失效时间
     * @param key
     * @param seconds
     * @return 是否设置成功
     */
    public boolean expire(String key, int seconds) {
        return call(shardedJedis -> {
            return JedisOperations.expire(shardedJedis, key, seconds);
        }, false, key, seconds);
    }

    public boolean expireAt(String key, long unixTime) {
        return call(shardedJedis -> {
            return Numbers.equals(shardedJedis.expireAt(key, unixTime), 1L);
        }, false, key, unixTime);
    }

    /**
     * 设置失效时间
     * @param key
     * @param milliseconds
     * @return 是否设置成功
     */
    public boolean pexpire(String key, int milliseconds) {
        return call(shardedJedis -> {
            return JedisOperations.pexpire(shardedJedis, key, milliseconds);
        }, false, key, milliseconds);
    }

    public boolean pexpireAt(String key, long millisTimestamp) {
        return call(shardedJedis -> {
            return Numbers.equals(shardedJedis.pexpireAt(key, millisTimestamp), 1L);
        }, false, key, millisTimestamp);
    }

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return call(shardedJedis -> {
            return shardedJedis.exists(key);
        }, false, key);
    }

    public Set<String> exists(String... keys) {
        Set<String> existsKeys = new HashSet<>();
        jedisClient.executePipelined(
            ShardedJedisPipeline::exists, 
            (k, v) -> {
                Boolean val = (Boolean) v;
                if (val != null && val) {
                    existsKeys.add(k);
                }
            }, 
            keys
        );
        return existsKeys;
    }

    /**
     * 删除
     * @param key
     * @return 被删除 key 的数量
     */
    public Long del(String key) {
        return call(shardedJedis -> {
            return shardedJedis.del(key);
        }, null, key);
    }

    /**
     * 删除
     * @param key
     * @return 被删除 key 的数量
     */
    public Long del(byte[] key) {
        return call(shardedJedis -> {
            return shardedJedis.del(key);
        }, null, (Object) key);
    }

    /**
     * 删除多个key值
     * @param keys
     * @return
     */
    public Long del(String... keys) {
        return call(shardedJedis -> {
            if (keys == null || keys.length == 0) {
                return 0L;
            }

            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (CollectionUtils.isEmpty(jedisList)) {
                return 0L;
            }

            if (jedisList.size() < keys.length / BATCH_MULTIPLE) { // key数量大于分片数量的BATCH_MULTIPLE倍
                Stream<Long> stream = jedisClient.executeSharded(jedis -> jedis.del(keys));
                //return stream.mapToLong(c -> ObjectUtils.orElse(c.join(), 0L)).sum()
                return stream.filter(Objects::nonNull).reduce(0L, Long::sum);
            } else {
                LongAdder adder = new LongAdder();
                jedisClient.executePipelined(
                    shardedJedis, 
                    ShardedJedisPipeline::del, 
                    (k, v) -> adder.add(v == null ? 0 : (long) v), 
                    keys
                );
                return adder.longValue();
            }
        }, null, (Object[]) keys);
    }

    /**
     * 删除key（匹配通配符）
     * @param keyWildcard
     * @return 被删除 key 的数量
     */
    public long delWithWildcard(String keyWildcard) {
        return call(shardedJedis -> {
            long delCounts = 0L;
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (CollectionUtils.isEmpty(jedisList)) {
                return delCounts;
            }

            List<CompletableFuture<Long>> list = jedisList.stream().map(
                jedis -> CompletableFuture.supplyAsync( // 获取key list
                    () -> jedis.keys(keyWildcard), EXECUTOR
                ).thenCompose( // 根据key list删除
                    keys -> CompletableFuture.supplyAsync(
                        () -> CollectionUtils.isEmpty(keys) ? null : 
                            jedis.del(keys.toArray(new String[keys.size()])), 
                        EXECUTOR
                    )
                )/*.thenApply(
                    keys -> jedis.del(keys.toArray(new String[keys.size()]))
                )*/
            ).collect(Collectors.toList());
            return list.stream()
                       .map(CompletableFuture::join)
                       .filter(Objects::nonNull)
                       .reduce(0L, Long::sum);
        }, 0L, keyWildcard);
    }

    /**
     * 返回 key 所储存的值的类型。
     * @param key
     * @return none (key不存在)；string (字符串)；list (列表)；set (集合)；zset (有序集)；hash (哈希表)；
     */
    public String type(String key) {
        return call(shardedJedis -> {
            return shardedJedis.type(key);
        }, null, key);
    }

}
