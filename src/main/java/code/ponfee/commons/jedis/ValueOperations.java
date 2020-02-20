package code.ponfee.commons.jedis;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import code.ponfee.commons.collect.ByteArrayWrapper;
import code.ponfee.commons.io.GzipProcessor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedisPipeline;

/**
 * redis string（字符串）操作类
 * 
 * @author Ponfee
 */
public class ValueOperations extends JedisOperations {

    private static final byte[] INCRBY_SCRIPT =
        "local val=redis.call('INCRBY',KEYS[1],ARGV[1]); if val==tonumber(ARGV[1]) then redis.call('EXPIRE',KEYS[1],ARGV[2]) end; return val;".getBytes();

    private static final byte[] GET_DEL_SCRIPT =
        "local val=redis.call('GET',KEYS[1]); if val~=nil then redis.call('DEL',KEYS[1]) end; return val;".getBytes();

    public static final String EX = "EX";
    public static final String PX = "PX";
    public static final String NX = "NX"; // 只有键key不存在的时候才会设置key的值
    public static final String XX = "XX"; // 只有键key存在的时候才会设置key的值

    ValueOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * SET 在设置操作成功完成时，才返回 OK 。
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, String value) {
        return set(key, value, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 将值value关联到key，并将key的生存时间设为seconds(以秒为单位)。
     * 如果 key 已经存在， SETEX 命令将覆写旧值。
     * SET('key','value')+EXPIRE('key','seconds')
     * @param key
     * @param value
     * @param seconds
     * @return 是否设置成功
     */
    public boolean set(String key, String value, int seconds) {
        return call(shardedJedis -> {
            // 使用EX：SET key-with-expire-time "hello" EX 10086
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), value);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, value, seconds);
    }

    public String get(String key) {
        return get(key, null);
    }

    /***
     * 获取值
     * @param key
     * @param seconds
     * @return
     */
    public String get(String key, Integer seconds) {
        return call(shardedJedis -> {
            String value = shardedJedis.get(key);
            if (value != null) {
                // 存在则设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return value;
        }, null, key, seconds);
    }

    /**
     * 设置并删除
     * 
     * @param key
     * @return
     */
    public String getAndDel(String key) {
        return call(shardedJedis -> {
            /*List<Object> result = jedisClient.executePipelined(
                shardedJedis, sdp -> { sdp.get(key); sdp.del(key); }
            );
            return (String) result.get(0);*/
            byte[] keyb = key.getBytes();
            byte[] val = (byte[]) shardedJedis.getShard(keyb).eval(GET_DEL_SCRIPT, 1, keyb);
            return val == null ? null:new String(val);
        }, null, key);
    }

    /**
     * 通配符获取值
     * @param keyWildcard
     * @return
     */
    public Set<String> getWithWildcard(String keyWildcard) {
        return call(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (CollectionUtils.isEmpty(jedisList)) {
                return null;
            }
            List<CompletableFuture<List<String>>> list = jedisList.stream().map(
                jedis -> CompletableFuture.supplyAsync(
                    () -> jedis.keys(keyWildcard), EXECUTOR
                ).thenCompose(
                    keys -> CompletableFuture.supplyAsync(
                        () -> CollectionUtils.isEmpty(keys) ? null : 
                            jedis.mget(keys.toArray(new String[keys.size()])), 
                        EXECUTOR
                    )
                )
            ).collect(Collectors.toList());
            return list.stream()
                       .map(CompletableFuture::join)
                       .filter(CollectionUtils::isNotEmpty)
                       .collect(HashSet::new, HashSet::addAll, HashSet::addAll);
        }, null, keyWildcard);
    }

    /**
     * SET 在设置操作成功完成时，才返回 OK 。
     * @param key
     * @param value
     * @return 是否设置成功
     */
    public boolean setLong(String key, long value) {
        return setLong(key, value, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * SET 在设置操作成功完成时，才返回 OK 。
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public boolean setLong(String key, long value, int seconds) {
        return call(shardedJedis -> {
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), String.valueOf(value));
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, value, seconds);
    }

    /**
     * 获取long值
     * @param key
     * @return
     */
    public Long getLong(String key) {
        return getLong(key, null);
    }

    public Long getLong(String key, Integer seconds) {
        return call(shardedJedis -> {
            Long number = null;
            String value = shardedJedis.get(key);
            if (value != null) {
                // 存在则设置失效时间
                number = Long.parseLong(value);
                expire(shardedJedis, key, seconds);
            }
            return number;
        }, null, key, seconds);
    }

    /**
     * 设置新值，返回旧值
     * @param key
     * @param value
     * @return
     */
    public String getSet(String key, String value) {
        return getSet(key, value, DEFAULT_EXPIRE_SECONDS);
    }

    public String getSet(String key, String value, int seconds) {
        return call(shardedJedis -> {
            String oldValue = shardedJedis.getSet(key, value);
            expireForce(shardedJedis, key, seconds);
            return oldValue;
        }, null, key, value, seconds);
    }

    /**
     * <pre>
     *  将 key的值设为value，当且仅当key不存在；若给定的 key已经存在，则 SETNX不做任何动作。
     *  返回：1成功；0失败；
     * </pre>
     * 
     * # 使用 EX 选项
     * SET key-with-expire-time "hello" EX 10086
     * 
     * # 使用 PX 选项
     * SET key-with-pexpire-time "moto" PX 80016
     * 
     * # 使用 NX 选项
     * SET not-exists-key "value" NX
     * 
     * # 使用 XX 选项
     * SET exists-key "value" XX
     * 
     * # EX 和 PX 可以同时出现，但后面给出的选项会覆盖前面给出的选项：ttl 5000
     * SET key "value" EX 1000 PX 5000000
     * 
     * # NX或XX 可以和  EX或PX 组合使用
     * SET key-with-expire-and-NX "hello" EX 10086 NX
     * 
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public boolean setnx(String key, String value, int seconds) {
        return call(shardedJedis -> {
            String result = shardedJedis.set(key, value, NX, EX, getActualExpire(seconds));
            return SUCCESS_MSG.equals(result);
        }, false, key, value, seconds);
    }

    public boolean setnx(byte[] key, byte[] value, int seconds) {
        return call(shardedJedis -> {
            String result = shardedJedis.set(
                key, value, NX.getBytes(), EX.getBytes(), getActualExpire(seconds)
            );
            return SUCCESS_MSG.equals(result);
        }, false, key, value, seconds);
    }

    /**
     * 将 key中储存的数字值增一，如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
     * @param key
     * @return 执行 INCR 命令之后 key 的值
     */
    public Long incrBy(String key) {
        return incrBy(key, 1, null);
    }

    public Long incrBy(String key, int step) {
        return incrBy(key, step, null);
    }

    public Long incrBy(String key, int step, Integer seconds) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.incrBy(key, step);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, step, seconds);
    }

    /**
     * Increment by step and set expire
     * 
     * @param key  the key
     * @param step the step
     * @param seconds the expire seconds
     * @return incry by result
     */
    public Long incrByEX(String key, int step, int seconds) {
        return call(sj -> {
            byte[] k = key.getBytes(), 
                   v = Integer.toString(step).getBytes(), 
                   s = Integer.toString(getActualExpire(seconds)).getBytes();
            return (Long) sj.getShard(k).eval(INCRBY_SCRIPT, 1, k, v, s);
        }, null, key, step, seconds);
    }

    /**
     * 为 key 中所储存的值加上浮点数增量 increment，如果 key 不存在，那么 INCRBYFLOAT 会先将 key 的值设为 0 ，再执行加法操作。
     * @param key
     * @param step
     * @return
     */
    public Double incrByFloat(String key, double step) {
        return incrByFloat(key, step, null);
    }

    public Double incrByFloat(String key, double step, Integer seconds) {
        return call(shardedJedis -> {
            Double rtn = shardedJedis.incrByFloat(key, step);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, step, seconds);
    }

    /**
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作。
     * @param key
     * @return
     */
    public Long decrBy(String key) {
        return decrBy(key, 1, null);
    }

    public Long decrBy(String key, int step) {
        return decrBy(key, step, null);
    }

    public Long decrBy(String key, int step, Integer seconds) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.decrBy(key, step);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, step, seconds);
    }

    /**
     * 对象序例化并缓存
     * @param key
     * @param value
     * @param isCompress
     * @param seconds
     * @return
     */
    public boolean setObject(byte[] key, Object value,
                             boolean isCompress, int seconds) {
        if (value == null) {
            return false;
        }

        return call(shardedJedis -> {
            byte[] data = jedisClient.serialize(value, isCompress);
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), data);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, value, isCompress, seconds);
    }

    public boolean setObject(byte[] key, Object value, boolean isCompress) {
        return setObject(key, value, isCompress, DEFAULT_EXPIRE_SECONDS);
    }

    public boolean setObject(byte[] key, Object value, int seconds) {
        return setObject(key, value, false, seconds);
    }

    public boolean setObject(byte[] key, Object value) {
        return setObject(key, value, false, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 对象序例化并缓存
     * @param key
     * @param value
     * @param isCompress
     * @param seconds
     * @return
     */
    public boolean setObject(String key, Object value,
                             boolean isCompress, int seconds) {
        return setObject(key.getBytes(StandardCharsets.UTF_8), value, isCompress, seconds);
    }

    public boolean setObject(String key, Object value, boolean isCompress) {
        return setObject(key, value, isCompress, DEFAULT_EXPIRE_SECONDS);
    }

    public boolean setObject(String key, Object value, int seconds) {
        return setObject(key, value, false, seconds);
    }

    public boolean setObject(String key, Object value) {
        return setObject(key, value, false, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 获取缓存数据并反序例化为对象
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return
     */
    public <T> T getObject(byte[] key, Class<T> clazz, 
                           boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            T value = jedisClient.deserialize(shardedJedis.get(key), clazz, isCompress);
            if (value != null) {
                // 存在则设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return value;
        }, null, key, clazz, isCompress, seconds);
    }

    public <T> T getObject(byte[] key, Class<T> clazz, boolean isCompress) {
        return getObject(key, clazz, isCompress, null);
    }

    public <T> T getObject(byte[] key, Class<T> clazz, Integer seconds) {
        return getObject(key, clazz, false, seconds);
    }

    public <T> T getObject(byte[] key, Class<T> clazz) {
        return getObject(key, clazz, false, null);
    }

    /**
     * 获取缓存数据并反序例化为对象
     * 
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return
     */
    public <T> T getObject(String key, Class<T> clazz, 
                           boolean isCompress, Integer seconds) {
        return getObject(key.getBytes(StandardCharsets.UTF_8), clazz, isCompress, seconds);
    }

    public <T> T getObject(String key, Class<T> clazz, boolean isCompress) {
        return getObject(key, clazz, isCompress, null);
    }

    public <T> T getObject(String key, Class<T> clazz, Integer seconds) {
        return getObject(key, clazz, false, seconds);
    }

    public <T> T getObject(String key, Class<T> clazz) {
        return getObject(key, clazz, false, null);
    }

    public boolean set(byte[] key, byte[] value, int seconds) {
        if (value == null || key == null) {
            return false;
        }
        return call(shardedJedis -> {
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), value);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, value, seconds);
    }

    /**
     * 缓存流数据
     * @param key
     * @param value
     * @param isCompress
     * @param seconds
     * @return
     */
    public boolean set(byte[] key, byte[] value, boolean isCompress, int seconds) {
        if (value == null || key == null) {
            return false;
        }

        byte[] value0 = isCompress ? GzipProcessor.compress(value) : value;

        return call(shardedJedis -> {
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), value0);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, value0, isCompress, seconds);
    }

    public boolean set(String key, byte[] value, int seconds) {
        return this.set(key.getBytes(), value, false, seconds);
    }

    /**
     * 获取流数据
     * @param key
     * @param isCompress
     * @param seconds
     * @return
     */
    public byte[] get(byte[] key, boolean isCompress, Integer seconds) {
        if (key == null) {
            return null;
        }

        return call(shardedJedis -> {
            byte[] result = shardedJedis.get(key);
            if (result != null) {
                if (isCompress) {
                    result = GzipProcessor.decompress(result);
                }
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, isCompress, seconds);
    }

    public byte[] get(byte[] key, boolean isCompress) {
        return this.get(key, isCompress, null);
    }

    public byte[] get(byte[] key) {
        return this.get(key, false, null);
    }

    /**
     * 批量获取值
     * @param keys
     * @return
     */
    public Map<String, String> mget(String... keys) {
        if (keys == null) {
            return null;
        }

        return call(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (CollectionUtils.isEmpty(jedisList)) {
                return null;
            }

            if (jedisList.size() < keys.length / BATCH_MULTIPLE) { // key数量大于分片数量倍数，则采用mget方式
                Map<String, String> resultMap = new ConcurrentHashMap<>();
                List<CompletableFuture<Void>> list = jedisList.stream().map(
                  jedis -> CompletableFuture.supplyAsync(() -> jedis.mget(keys), EXECUTOR)
                ).map(future -> future.thenAccept(values -> { // 同步
                    String value;
                    for (int i = 0; i < keys.length; i++) {
                        if ((value = values.get(i)) != null) {
                            resultMap.put(keys[i], value);
                        }
                    }
                })).collect(Collectors.toList());
                list.forEach(CompletableFuture::join);
                return resultMap;
            } else { // 直接获取，不用mget方式
                Map<String, String> result = new HashMap<>();
                jedisClient.executePipelined(
                    shardedJedis, ShardedJedisPipeline::get, (k, v) -> result.put(k, (String) v), keys
                );
                return result;
            }
        }, null, Arrays.toString(keys));
    }

    /**
     * 批量获取
     * @param clazz
     * @param keys
     * @return
     */
    public <T> Map<ByteArrayWrapper, T> mgetObject(Class<T> clazz, boolean isCompress, byte[]... keys) {
        Map<ByteArrayWrapper, byte[]> datas = this.mget(false, keys);
        if (datas == null || datas.isEmpty()) {
            return null;
        }
        return datas.entrySet().stream().filter(
            e -> Objects.nonNull(e.getValue())
        ).collect(Collectors.toMap(
            Entry::getKey, 
            e -> jedisClient.deserialize(e.getValue(), clazz, isCompress)
        ));
    }

    public <T> Map<ByteArrayWrapper, T> mgetObject(Class<T> clazz, byte[]... keys) {
        return this.mgetObject(clazz, false, keys);
    }

    public Map<ByteArrayWrapper, byte[]> mget(byte[]... keys) {
        return this.mget(false, keys);
    }

    /**
     * 批量获取值
     * @param isCompress
     * @param keys
     * @return
     */
    public Map<ByteArrayWrapper, byte[]> mget(boolean isCompress, byte[]... keys) {
        if (keys == null) {
            return null;
        }

        return call(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (CollectionUtils.isEmpty(jedisList)) {
                return null;
            }

            if (jedisList.size() < keys.length / BATCH_MULTIPLE) { // key数量大于分片数量倍数，则采用mget方式
                Map<ByteArrayWrapper, byte[]> resultMap = new ConcurrentHashMap<>();
                List<CompletableFuture<Void>> list = jedisList.stream().map(
                  jedis -> CompletableFuture.supplyAsync(() -> jedis.mget(keys), EXECUTOR)
                ).map(future -> future.thenAccept(values -> { // 同步
                    for (int i = 0; i < keys.length; i++) {
                        byte[] value;
                        if ((value = values.get(i)) != null) {
                            resultMap.put(ByteArrayWrapper.of(keys[i]), value);
                        }
                    }
                })).collect(Collectors.toList());

                list.forEach(CompletableFuture::join);
                return resultMap;
            } else { // 直接获取，不用mget方式
                Map<ByteArrayWrapper, byte[]> result = new HashMap<>();
                jedisClient.executePipelined(
                   shardedJedis, ShardedJedisPipeline::get, 
                   (k, v) -> result.put(ByteArrayWrapper.of(k), (byte[]) v), 
                   keys
                );
                return result;
            }
        }, null, isCompress, keys);
    }

    public <T> Map<String, T> mgetObject(Class<T> clazz, String... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return null;
        }

        Map<ByteArrayWrapper, T> result = this.mgetObject(
            clazz, Arrays.stream(keys).map(String::getBytes).toArray(byte[][]::new)
        );

        if (MapUtils.isEmpty(result)) {
            return null;
        }

        return result.entrySet().stream().collect(
            Collectors.toMap(e -> new String(e.getKey().getArray()), Entry::getValue)
        );
    }

}
