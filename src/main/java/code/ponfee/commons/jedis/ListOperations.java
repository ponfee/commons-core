package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * redis list（列表）操作类
 * 
 * @author Ponfee
 */
public class ListOperations extends JedisOperations {

    ListOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表头
     * @param key
     * @param seconds
     * @param fields
     * @return 执行 LPUSH 命令后，列表的长度。
     */
    public Long lpush(String key, Integer seconds, String... fields) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.lpush(key, fields);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, seconds, fields);
    }

    public Long lpush(String key, String... fields) {
        return this.lpush(key, null, fields);
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表头
     * @param key
     * @param isCompress
     * @param seconds
     * @param objs
     * @return 执行 LPUSH 命令后，列表的长度。
     */
    public <T> Long lpushObject(byte[] key, boolean isCompress, Integer seconds, T[] objs) {
        return call(shardedJedis -> {
            byte[][] data = new byte[objs.length][];
            for (int i = 0; i < objs.length; i++) {
                data[i] = jedisClient.serialize(objs[i], isCompress);
            }
            Long rtn = shardedJedis.lpush(key, data);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, isCompress, seconds, objs);
    }

    public <T> Long lpushObject(byte[] key, boolean isCompress, T[] objs) {
        return this.lpushObject(key, isCompress, null, objs);
    }

    public <T> Long lpushObject(byte[] key, Integer seconds, T[] objs) {
        return this.lpushObject(key, false, seconds, objs);
    }

    public <T> Long lpushObject(byte[] key, T[] objs) {
        return this.lpushObject(key, false, null, objs);
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表尾(最右边)。
     * @param key
     * @param seconds
     * @param fields
     * @return 执行 RPUSH 操作后，表的长度。
     */
    public Long rpush(String key, Integer seconds, String... fields) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.rpush(key, fields);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, seconds, fields);
    }

    public Long rpush(String key, String... fields) {
        return this.rpush(key, null, fields);
    }

    /**
     * 移除并返回列表 key 的头元素。
     * @param key
     * @param seconds
     * @return 列表的头元素。当 key 不存在时，返回 nil 。
     */
    public String lpop(String key, Integer seconds) {
        return call(shardedJedis -> {
            String result = shardedJedis.lpop(key);
            if (result != null) {
                // 存在才设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, seconds);
    }

    public String lpop(String key) {
        return this.lpop(key, null);
    }

    /**
     * 移除并返回列表 key 的尾元素。
     * @param key
     * @param seconds
     * @return 列表的尾元素。当 key 不存在时，返回 nil 。
     */
    public <T> T lpopObject(byte[] key, Class<T> clazz,
                            boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            T result = jedisClient.deserialize(shardedJedis.lpop(key), clazz, isCompress);
            if (result != null) {
                // 存在才设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, clazz, isCompress, seconds);
    }

    public <T> T lpopObject(byte[] key, Class<T> clazz, boolean isCompress) {
        return this.lpopObject(key, clazz, isCompress, null);
    }

    public <T> T lpopObject(byte[] key, Class<T> clazz, Integer seconds) {
        return this.lpopObject(key, clazz, false, seconds);
    }

    public <T> T lpopObject(byte[] key, Class<T> clazz) {
        return this.lpopObject(key, clazz, false, null);
    }

    /**
     * 移除并返回列表 key 的尾元素。
     * @param key
     * @param seconds
     * @return 列表的尾元素。当 key 不存在时，返回 nil 。
     */
    public String rpop(String key, Integer seconds) {
        return call(shardedJedis -> {
            String result = shardedJedis.rpop(key);
            if (result != null) {
                // 存在才设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, seconds);
    }

    public String rpop(String key) {
        return this.rpop(key, null);
    }

    /**
     * 移除并返回列表 key 的尾元素。
     * @param key
     * @param seconds
     * @return 列表的尾元素。当 key 不存在时，返回 nil 。
     */
    public <T> T rpopObject(byte[] key, Class<T> clazz,
                            boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            T result = jedisClient.deserialize(shardedJedis.rpop(key), clazz, isCompress);
            if (result != null) {
                // 存在才设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, clazz, isCompress, seconds);
    }

    public <T> T rpopObject(byte[] key, Class<T> clazz, boolean isCompress) {
        return this.rpopObject(key, clazz, isCompress, null);
    }

    public <T> T rpopObject(byte[] key, Class<T> clazz, Integer seconds) {
        return this.rpopObject(key, clazz, false, seconds);
    }

    public <T> T rpopObject(byte[] key, Class<T> clazz) {
        return this.rpopObject(key, clazz, false, null);
    }

    /**
     * <pre>
     *  根据参数 count 的值，移除列表中与参数 value 相等的元素。
     *  count 的值可以是以下几种：
     *    count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
     *    count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
     *    count = 0 : 移除表中所有与 value 相等的值。
     * </pre>
     * 
     * @param key
     * @param count
     * @param filed
     * @param seconds
     * @return 被移除元素的数量。因为不存在的 key 被视作空表(empty list)，所以当 key 不存在时， LREM 命令总是返回 0
     */
    public long lrem(String key, int count, String filed, Integer seconds) {
        return call(shardedJedis -> {
            long rtn = shardedJedis.lrem(key, count, filed);
            if (rtn > 0) {
                // 存在才设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return rtn;
        }, 0L, key, count, filed, seconds);
    }

    public long lrem(String key, int count, String filed) {
        return this.lrem(key, count, filed, null);
    }

    /**
     * <pre>
     *  根据参数 count 的值，移除列表中与参数 value 相等的元素。
     *  count 的值可以是以下几种：
     *    count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
     *    count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
     *    count = 0 : 移除表中所有与 value 相等的值。
     * </pre>
     * 
     * @param key
     * @param count
     * @param filed
     * @param seconds
     * @return 被移除元素的数量。因为不存在的 key 被视作空表(empty list)，所以当 key 不存在时， LREM 命令总是返回 0
     */
    public long lrem(byte[] key, int count, byte[] filed, Integer seconds) {
        return call(shardedJedis -> {
            long rtn = shardedJedis.lrem(key, count, filed);
            if (rtn > 0) {
                // 存在才设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return rtn;
        }, 0L, key, count, filed, seconds);
    }

    public long lrem(byte[] key, int count, byte[] filed) {
        return this.lrem(key, count, filed, null);
    }

    /**
     * <pre>
     *  返回列表 key 的长度。
     *  如果 key 不存在，则 key 被解释为一个空列表，返回 0 .
     * </pre>
     * 
     * @param key
     * @param seconds
     * @return 列表 key 的长度。
     */
    public long llen(String key, Integer seconds) {
        return call(shardedJedis -> {
            long result = shardedJedis.llen(key);
            if (result > 0) {
                // 存在才设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, 0L, key, seconds);
    }

    public long llen(String key) {
        return this.llen(key, null);
    }

    /**
     * <pre>
     *  返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定。
     *  下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
     *  你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     *  如果 start 下标比列表的最大下标 end ( LLEN list 减去 1 )还要大，那么 LRANGE 返回一个空列表。
     * </pre>
     * 
     * @param key
     * @param start
     * @param end
     * @param seconds
     * @return 一个列表，包含指定区间内的元素。
     */
    public List<String> lrange(String key, long start, long end, Integer seconds) {
        return call(shardedJedis -> {
            List<String> result = shardedJedis.lrange(key, start, end);
            expire(shardedJedis, key, seconds);
            return result;
        }, null, key, start, end, seconds);
    }

    public List<String> lrange(String key, long start, long end) {
        return this.lrange(key, start, end, null);
    }

    /**
     * <pre>
     *  返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定。
     *  下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
     *  你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     *  如果 start 下标比列表的最大下标 end ( LLEN list 减去 1 )还要大，那么 LRANGE 返回一个空列表。
     * </pre>
     * 
     * @param key
     * @param start
     * @param end
     * @param seconds
     * @return 一个列表，包含指定区间内的元素。
     */
    public <T> List<T> lrange(byte[] key, Class<T> clazz, boolean isCompress, 
                              long start, long end, Integer seconds) {
        return call(shardedJedis -> {
            List<T> result = new ArrayList<>();
            List<byte[]> datas = shardedJedis.lrange(key, start, end);
            if (datas != null && !datas.isEmpty()) {
                for (byte[] data : datas) {
                    T t = jedisClient.deserialize(data, clazz, isCompress);
                    if (t != null) {
                        result.add(t);
                    }
                }
            }
            expire(shardedJedis, key, seconds);
            return result;
        }, null, key, clazz, isCompress, start, end, seconds);
    }

    public <T> List<T> lrange(byte[] key, Class<T> clazz, boolean isCompress,
                                             long start, long end) {
        return this.lrange(key, clazz, isCompress, start, end, null);
    }

    public <T> List<T> lrange(byte[] key, Class<T> clazz, long start, 
                                             long end, Integer seconds) {
        return this.lrange(key, clazz, false, start, end, seconds);
    }

    public <T> List<T> lrange(byte[] key, Class<T> clazz, long start, long end) {
        return this.lrange(key, clazz, false, start, end, null);
    }

}
