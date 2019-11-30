package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import code.ponfee.commons.math.Numbers;

import java.util.Set;

/**
 * redis hash（哈希表）操作类
 * 
 * @author Ponfee
 */
public class HashOperations extends JedisOperations {

    HashOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * <pre>
     *  将哈希表 key 中的域 field 的值设为 value 。
     *  如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
     *  如果域 field 已经存在于哈希表中，旧值将被覆盖。
     * </pre>
     * 
     * @param key
     * @param field
     * @param value
     * @param seconds
     * @return 返回值：true设置一个新域；false覆盖一个旧域；
     */
    public boolean hset(String key, String field, 
                        String value, Integer seconds) {
        if (value == null) {
            return false;
        }

        return call(shardedJedis -> {
            boolean flag = Numbers.equals(shardedJedis.hset(key, field, value), 1);
            expireForce(shardedJedis, key, seconds);
            return flag;
        }, false, key, field, value, seconds);
    }

    public boolean hset(String key, String field, String value) {
        return this.hset(key, field, value, null);
    }

    /**
     * 将 key的值设为value，当且仅当key不存在；若给定的 key已经存在，则 SETNX不做任何动作。
     * 返回：1成功；0失败；
     *
     * @param key
     * @param field
     * @param value
     * @param seconds
     * @return {@code true} then hset success
     */
    public boolean hsetnx(String key, String field,
                          String value, Integer seconds) {
        if (value == null) {
            return false;
        }

        return call(shardedJedis -> {
            boolean flag = Numbers.equals(shardedJedis.hsetnx(key, field, value), 1);
            if (flag) {
                expireForce(shardedJedis, key, seconds);
            }
            return flag;
        }, false, key, field, value, seconds);
    }

    public boolean hsetnx(String key, String field, String value) {
        return this.hsetnx(key, field, value, null);
    }

    /**
     * 返回哈希表 key 中给定域 field 的值。
     * @param key
     * @param field
     * @param seconds
     * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 nil 。
     */
    public String hget(String key, String field, Integer seconds) {
        return call(shardedJedis -> {
            String result = shardedJedis.hget(key, field);
            if (result != null) {
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, field, seconds);
    }

    public String hget(String key, String field) {
        return this.hget(key, field, null);
    }

    /**
     * <pre>
     * 返回哈希表 key 中，所有的域和值。 
     * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @return 以map形式返回哈希表的域和域的值
     */
    public Map<String, String> hgetAll(String key, Integer seconds) {
        return call(shardedJedis -> {
            Map<String, String> result = shardedJedis.hgetAll(key);
            if (result != null && !result.isEmpty()) {
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, seconds);
    }

    public Map<String, String> hgetAll(String key) {
        return this.hgetAll(key, null);
    }

    /**
     * 返回哈希表 key 中所有域的值
     * @param key
     * @param seconds
     * @return 一个包含哈希表中所有值的表
     */
    public List<String> hvals(String key, Integer seconds) {
        return call(shardedJedis -> {
            List<String> result = shardedJedis.hvals(key);
            if (result != null && !result.isEmpty()) {
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, seconds);
    }

    public List<String> hvals(String key) {
        return this.hvals(key, null);
    }

    /**
     * <pre>
     *  将哈希表 key 中的域 field 的值设为 value 。
     *  如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
     *  如果域 field 已经存在于哈希表中，旧值将被覆盖。
     * </pre>
     * 
     * @param key
     * @param field
     * @param t
     * @param isCompress
     * @param seconds
     * @return 返回值：true设置一个新域；false覆盖一个旧域；
     */
    public boolean hsetObject(byte[] key, byte[] field, Object t,
                              boolean isCompress, Integer seconds) {
        if (t == null) {
            return false;
        }

        return call(shardedJedis -> {
            byte[] data = jedisClient.serialize(t, isCompress);
            boolean flag = Numbers.equals(shardedJedis.hset(key, field, data), 1);
            expireForce(shardedJedis, key, seconds);
            return flag;
        }, false, key, field, t, isCompress, seconds);
    }

    public boolean hsetObject(byte[] key, byte[] field, Object t, boolean isCompress) {
        return this.hsetObject(key, field, t, isCompress, null);
    }

    public boolean hsetObject(byte[] key, byte[] field, Object t, Integer seconds) {
        return this.hsetObject(key, field, t, false, seconds);
    }

    public boolean hsetObject(byte[] key, byte[] field, Object t) {
        return this.hsetObject(key, field, t, false, null);
    }

    /**
     * 获取对象
     * @param key
     * @param field
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return
     */
    public <T> T hgetObject(byte[] key, byte[] field, Class<T> clazz, 
                            boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            byte[] data = shardedJedis.hget(key, field);
            T t = jedisClient.deserialize(data, clazz, isCompress);
            if (t != null) {
                expire(shardedJedis, key, seconds);
            }
            return t;
        }, null, key, field, clazz, isCompress, seconds);
    }

    public <T> T hgetObject(byte[] key, byte[] field, Class<T> clazz, boolean isCompress) {
        return this.hgetObject(key, field, clazz, isCompress, null);
    }

    public <T> T hgetObject(byte[] key, byte[] field, Class<T> clazz, Integer seconds) {
        return this.hgetObject(key, field, clazz, false, seconds);
    }

    public <T> T hgetObject(byte[] key, byte[] field, Class<T> clazz) {
        return this.hgetObject(key, field, clazz, false, null);
    }

    /**
     * <pre>
     * 返回哈希表 key 中，所有的域和值。 
     * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
     * </pre>
     * 
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return 以map形式返回哈希表的域和域的值
     */
    public <T> Map<byte[], T> hgetAllObject(byte[] key, Class<T> clazz, 
                                            boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            Map<byte[], byte[]> datas = shardedJedis.hgetAll(key);
            Map<byte[], T> result = new HashMap<>();
            if (datas != null && !datas.isEmpty()) {
                for (Entry<byte[], byte[]> entry : datas.entrySet()) {
                    T t = jedisClient.deserialize(entry.getValue(), clazz, isCompress);
                    result.put(entry.getKey(), t);
                }
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, clazz, isCompress, seconds);
    }

    public <T> Map<byte[], T> hgetAllObject(byte[] key, Class<T> clazz,
                                            boolean isCompress) {
        return this.hgetAllObject(key, clazz, isCompress, null);
    }

    public <T> Map<byte[], T> hgetAllObject(byte[] key, Class<T> clazz, Integer seconds) {
        return this.hgetAllObject(key, clazz, false, seconds);
    }

    public <T> Map<byte[], T> hgetAllObject(byte[] key, Class<T> clazz) {
        return this.hgetAllObject(key, clazz, false, null);
    }

    /**
     * 返回哈希表 key 中所有域的值
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return 一个包含哈希表中所有值的表
     */
    public <T> List<T> hvalsObject(byte[] key, Class<T> clazz, 
                                   boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            List<T> list = new ArrayList<>();
            for (byte[] data : shardedJedis.hvals(key)) {
                T t = jedisClient.deserialize(data, clazz, isCompress);
                if (t != null) {
                    list.add(t);
                }
            }
            expire(shardedJedis, key, seconds);
            return list;
        }, null, key, clazz, isCompress, seconds);
    }

    public <T> List<T> hvalsObject(byte[] key, Class<T> clazz, boolean isCompress) {
        return this.hvalsObject(key, clazz, isCompress, null);
    }

    public <T> List<T> hvalsObject(byte[] key, Class<T> clazz, Integer seconds) {
        return this.hvalsObject(key, clazz, false, seconds);
    }

    public <T> List<T> hvalsObject(byte[] key, Class<T> clazz) {
        return this.hvalsObject(key, clazz, false, null);

    }

    /**
     * <pre>
     *  同时将多个 field-value (域-值)对设置到哈希表 key 中。
     *  此命令会覆盖哈希表中已存在的域。
     *  如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
     * </pre>
     * 
     * @param key
     * @param map
     * @param isCompress
     * @param seconds
     * @return 返回值：true成功；false失败；
     */
    public boolean hmsetObjects(byte[] key, Map<byte[], ?> map,
                                boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            Map<byte[], byte[]> data = new HashMap<>();
            for (Entry<byte[], ?> entry : map.entrySet()) {
                data.put(entry.getKey(), jedisClient.serialize(entry.getValue(), isCompress));
            }

            String rtn = shardedJedis.hmset(key, data);
            expireForce(shardedJedis, key, seconds);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, map, isCompress, seconds);
    }

    public boolean hmsetObjects(byte[] key, Map<byte[], ?> map, boolean isCompress) {
        return this.hmsetObjects(key, map, isCompress, null);
    }

    public boolean hmsetObjects(byte[] key, Map<byte[], ?> map, Integer seconds) {
        return this.hmsetObjects(key, map, false, seconds);
    }

    public boolean hmsetObjects(byte[] key, Map<byte[], ?> map) {
        return this.hmsetObjects(key, map, false, null);
    }

    /**
     * <pre>
     *  返回哈希表 key 中，一个或多个给定域的值。
     *  如果给定的域不存在于哈希表，那么返回一个 nil 值。
     *  因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。
     * </pre>
     * 
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @param fields
     * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
     */
    public <T> List<T> hmgetObjects(byte[] key, Class<T> clazz, boolean isCompress, 
                                    Integer seconds, byte[]... fields) {
        return call(shardedJedis -> {
            List<byte[]> datas = shardedJedis.hmget(key, fields);
            if (datas == null || datas.isEmpty()) {
                return null;
            }

            List<T> list = new ArrayList<>();
            for (byte[] data : datas) {
                T t = jedisClient.deserialize(data, clazz, isCompress);
                if (t != null) {
                    list.add(t);
                }
            }
            expire(shardedJedis, key, seconds);
            return list;
        }, null, key, clazz, isCompress, seconds, fields);
    }

    public <T> List<T> hmgetObjects(byte[] key, Class<T> clazz, boolean isCompress,
                                    byte[]... fields) {
        return this.hmgetObjects(key, clazz, isCompress, null, fields);
    }

    public <T> List<T> hmgetObjects(byte[] key, Class<T> clazz, Integer seconds,
                                    byte[]... fields) {
        return this.hmgetObjects(key, clazz, false, seconds, fields);
    }

    public <T> List<T> hmgetObjects(byte[] key, Class<T> clazz, byte[]... fields) {
        return this.hmgetObjects(key, clazz, false, null, fields);
    }

    /**
     * <pre>
     *  为哈希表 key 中的域 field 的值加上增量 increment，增量也可以为负数，相当于对给定域进行减法操作。
     *  如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。
     *  如果域 field 不存在，那么在执行命令前，域的值被初始化为 0
     * </pre>
     * 
     * @param key
     * @param field
     * @param value
     * @param seconds
     * @return 执行 HINCRBY 命令之后，哈希表 key 中域 field 的值
     */
    public Long hincrBy(String key, String field, int value, Integer seconds) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.hincrBy(key, field, value);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, field, value, seconds);
    }

    public Long hincrBy(String key, String field, int value) {
        return hincrBy(key, field, value, null);
    }

    /**
     * <pre>
     *  同时将多个 field-value (域-值)对设置到哈希表 key 中。
     *  此命令会覆盖哈希表中已存在的域。
     *  如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
     * </pre>
     * 
     * @param key
     * @param map
     * @param seconds
     * @return 返回值：true成功；false失败；
     */
    public boolean hmset(String key, Map<String, String> map, Integer seconds) {
        if (map == null || map.isEmpty()) {
            return false;
        }

        return call(shardedJedis -> {
            String rtn = shardedJedis.hmset(key, map);
            expireForce(shardedJedis, key, seconds);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, map, seconds);
    }

    public boolean hmset(String key, Map<String, String> map) {
        return this.hmset(key, map, null);
    }

    /**
     * <pre>
     *  返回哈希表 key 中，一个或多个给定域的值。
     *  如果给定的域不存在于哈希表，那么返回一个 nil 值。
     *  为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @param fields
     * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
     */
    public List<String> hmget(String key, Integer seconds, String... fields) {
        return call(shardedJedis -> {
            List<String> list = shardedJedis.hmget(key, fields);
            expire(shardedJedis, key, seconds);
            return list;
        }, null, key, seconds, fields);
    }

    public List<String> hmget(String key, String... fields) {
        return this.hmget(key, null, fields);
    }

    /**
     * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略
     * @param key
     * @param seconds
     * @param fields
     * @return 被成功移除的域的数量，不包括被忽略的域
     */
    public Long hdel(String key, Integer seconds, String... fields) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.hdel(key, fields);
            expire(shardedJedis, key, seconds);
            return rtn;
        }, null, key, seconds, fields);
    }

    public Long hdel(String key, String... fields) {
        return this.hdel(key, null, fields);
    }

    /**
     * 返回哈希表 key 中域的数量。
     * @param key
     * @param seconds
     * @return 哈希表中域的数量，当 key 不存在时，返回 0 。
     */
    public Long hlen(String key, Integer seconds) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.hlen(key);
            if (rtn != null && rtn != 0) {
                // key存在时才设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return rtn;
        }, null, key, seconds);
    }

    public Long hlen(String key) {
        return this.hlen(key, null);
    }

    /**
     * 查看哈希表 key 中，给定域 field 是否存在。
     * @param key
     * @param field
     * @param seconds
     * @return 返回值：true哈希表含有给定域；false哈希表不含有给定域（或key）不存在；
     */
    public boolean hexists(String key, String field, Integer seconds) {
        return call(shardedJedis -> {
            boolean result = shardedJedis.hexists(key, field);
            expire(shardedJedis, key, seconds);
            return result;
        }, false, key, field, seconds);
    }

    /**
     * 返回哈希表 key 中的所有域。
     * @param key
     * @param seconds
     * @return 一个包含哈希表中所有域的表。当 key 不存在时，返回一个空表。
     */
    public Set<String> hkeys(String key, Integer seconds) {
        return call(shardedJedis -> {
            Set<String> keys = shardedJedis.hkeys(key);
            if (keys != null && !keys.isEmpty()) {
                // 存在时才设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return keys;
        }, null, key, seconds);
    }

    /**
     * 批量获取
     * 
     * @param queryParams
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, String>> hmget(Map<String, String[]> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return null;
        }

        Map<String, Map<String, String>> result = new HashMap<>();
        jedisClient.executePipelined(
            (sp, e) -> sp.hmget(e.getKey(), e.getValue()), 
            (k, v) -> {
                Map<String, String> map = result.computeIfAbsent(
                    k.getKey(), key -> new HashMap<>()
                );

                List<String> list = (List<String>) v;
                String[] fields = k.getValue();
                for (int i = 0, n = fields.length; i < n; i++) {
                    if (StringUtils.isNotEmpty(fields[i])) {
                        map.put(fields[i], list.get(i));
                    }
                }
            }, 
            Lists.newArrayList(queryParams.entrySet())
        );
        return result;
    }
}
