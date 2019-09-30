package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * redis set 集合操作
 * 
 * @author Ponfee
 */
public class SetOpertions extends JedisOperations {

    SetOpertions(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * <pre>
     *  将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
     *  假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
     *  当 key 不是集合类型时，返回一个错误。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @param members
     * @return 被添加到集合中的新元素的数量，不包括被忽略的元素。
     */
    public Long sadd(String key, Integer seconds, String... members) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.sadd(key, members);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, seconds, members);
    }

    public Long sadd(String key, String... members) {
        return this.sadd(key, null, members);
    }

    /**
     * <pre>
     *  将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
     *  假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
     *  当 key 不是集合类型时，返回一个错误。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @param members
     * @return 被添加到集合中的新元素的数量，不包括被忽略的元素。
     */
    public <T> Long sadd(byte[] key, boolean isCompress,
                         Integer seconds, T[] members) {
        return call(shardedJedis -> {
            byte[][] data = new byte[members.length][];
            for (int i = 0; i < members.length; i++) {
                data[i] = jedisClient.serialize(members[i], isCompress);
            }
            Long rtn = shardedJedis.sadd(key, data);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, isCompress, seconds, members);
    }

    public <T> Long sadd(byte[] key, boolean isCompress, T[] members) {
        return this.sadd(key, isCompress, null, members);
    }

    public <T> Long sadd(byte[] key, Integer seconds, T[] members) {
        return this.sadd(key, false, seconds, members);
    }

    public <T> Long sadd(byte[] key, T[] members) {
        return this.sadd(key, false, null, members);
    }

    /**
     * <pre>
     *  移除并返回集合中的一个随机元素。
     *  如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @return 被移除的随机元素。当 key 不存在或 key 是空集时，返回 nil 。
     */
    public String spop(String key, Integer seconds) {
        return call(shardedJedis -> {
            String result = shardedJedis.spop(key);
            if (result != null) {
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, seconds);
    }

    public String spop(String key) {
        return this.spop(key, null);
    }

    /**
     * <pre>
     *  移除并返回集合中的一个随机元素。
     *  如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @return 被移除的随机元素。当 key 不存在或 key 是空集时，返回 nil 。
     */
    public <T> T spop(byte[] key, Class<T> clazz,
                      boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            byte[] data = shardedJedis.spop(key);
            T t = jedisClient.deserialize(data, clazz, isCompress);
            if (t != null) {
                expire(shardedJedis, key, seconds);
            }
            return t;
        }, null, key, clazz, isCompress, seconds);
    }

    public <T> T spop(byte[] key, Class<T> clazz, boolean isCompress) {
        return this.spop(key, clazz, isCompress, null);
    }

    public <T> T spop(byte[] key, Class<T> clazz, Integer seconds) {
        return this.spop(key, clazz, false, seconds);
    }

    public <T> T spop(byte[] key, Class<T> clazz) {
        return this.spop(key, clazz, false, null);
    }

    /**
     * 判断 member 元素是否集合 key 的成员。
     * @param key
     * @param member
     * @param seconds
     * @return 如果 member 元素是集合的成员，返回 true 。如果 member 元素不是集合的成员，或 key 不存在，返回 false 。
     */
    public boolean sismember(String key, String member, Integer seconds) {
        return call(shardedJedis -> {
            boolean result = shardedJedis.sismember(key, member);
            expire(shardedJedis, key, seconds);
            return result;
        }, false, key, member, seconds);
    }

    public boolean sismember(String key, String member) {
        return this.sismember(key, member, null);
    }

    /**
     * <pre>
     *  返回集合 key 中的所有成员。
     *  不存在的 key 被视为空集合。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @return 集合中的所有成员。
     */
    public Set<String> smembers(String key, Integer seconds) {
        return call(shardedJedis -> {
            Set<String> result = shardedJedis.smembers(key);
            if (result != null && !result.isEmpty()) {
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, seconds);
    }

    public Set<String> smembers(String key) {
        return this.smembers(key, null);
    }

    /**
     * <pre>
     *  返回集合 key 中的所有成员。
     *  不存在的 key 被视为空集合。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @return 集合中的所有成员。
     */
    public <T> Set<T> smembers(byte[] key, Class<T> clazz,
                               boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            Set<byte[]> datas = shardedJedis.smembers(key);
            Set<T> result = new HashSet<>();
            if (datas != null && !datas.isEmpty()) {
                for (byte[] data : datas) {
                    T t = jedisClient.deserialize(data, clazz, isCompress);
                    if (t != null) {
                        result.add(t);
                    }
                }
            }
            if (result != null && !result.isEmpty()) {
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, clazz, isCompress, seconds);
    }

    public <T> Set<T> smembers(byte[] key, Class<T> clazz, boolean isCompress) {
        return this.smembers(key, clazz, isCompress, null);
    }

    public <T> Set<T> smembers(byte[] key, Class<T> clazz, Integer seconds) {
        return this.smembers(key, clazz, false, seconds);
    }

    public <T> Set<T> smembers(byte[] key, Class<T> clazz) {
        return this.smembers(key, clazz, false, null);
    }

    /**
     * 返回集合 key 的基数(集合中元素的数量)。
     * @param key
     * @param seconds
     * @return 集合的基数。当 key 不存在时，返回 0 。
     */
    public Long scard(String key, Integer seconds) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.scard(key);
            if (rtn != null && rtn > 0) {
                expire(shardedJedis, key, seconds);
            }
            return rtn;
        }, null, key, seconds);
    }

    public Long scard(String key) {
        return this.scard(key, null);
    }

    /**
     * <pre>
     *  如果命令执行时，只提供了 key 参数，那么返回集合中的一个随机元素。
     *    如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合。
     *    如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。
     *  该操作和 SPOP 相似，但 SPOP 将随机元素从集合中移除并返回，而 SRANDMEMBER 则仅仅返回随机元素，而不对集合进行任何改动
     * </pre>
     * 
     * @param key
     * @param count
     * @param seconds
     * @return 只提供 key 参数时，返回一个元素；如果集合为空，返回 nil；如果提供了 count 参数，那么返回一个数组；如果集合为空，返回空数组。
     */
    public List<String> srandmember(String key, int count, Integer seconds) {
        return call(shardedJedis -> {
            List<String> result = shardedJedis.srandmember(key, count);
            if (result != null && !result.isEmpty()) {
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, count, seconds);
    }

    public List<String> srandmember(String key, int count) {
        return this.srandmember(key, count, null);
    }

    public String srandmember(String key, Integer seconds) {
        List<String> list = this.srandmember(key, 1, seconds);
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public String srandmember(String key) {
        return srandmember(key, null);
    }

    /**
     * <pre>
     *  如果命令执行时，只提供了 key 参数，那么返回集合中的一个随机元素。
     *    如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合。
     *    如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。
     *  该操作和 SPOP 相似，但 SPOP 将随机元素从集合中移除并返回，而 SRANDMEMBER 则仅仅返回随机元素，而不对集合进行任何改动
     * </pre>
     * 
     * @param key
     * @param count
     * @param seconds
     * @return 只提供 key 参数时，返回一个元素；如果集合为空，返回 nil；如果提供了 count 参数，那么返回一个数组；如果集合为空，返回空数组。
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> srandmember(byte[] key, Class<T> clazz,
                                   boolean isCompress, int count, Integer seconds) {
        return call(shardedJedis -> {
            List<byte[]> datas = shardedJedis.srandmember(key, count);
            List<T> result = new ArrayList<>();
            if (datas != null && !datas.isEmpty()) {
                for (byte[] data : datas) {
                    T t = jedisClient.deserialize(data, clazz, isCompress);
                    if (t != null) {
                        result.add(t);
                    }
                }
            }
            if (result != null && !result.isEmpty()) {
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, clazz, isCompress, count, seconds);
    }

    public <T> List<T> srandmember(byte[] key, Class<T> clazz, boolean isCompress, int count) {
        return this.srandmember(key, clazz, isCompress, count, null);
    }

    public <T> List<T> srandmember(byte[] key, Class<T> clazz, int count, Integer seconds) {
        return this.srandmember(key, clazz, false, count, seconds);
    }

    public <T> List<T> srandmember(byte[] key, Class<T> clazz, int count) {
        return this.srandmember(key, clazz, false, count, null);
    }

    /**
     * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
     * @param key
     * @param member
     * @param seconds
     * @return 被成功移除的元素的数量，不包括被忽略的元素。
     */
    public Long srem(String key, String member, Integer seconds) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.srem(key, member);
            expire(shardedJedis, key, seconds);
            return rtn;
        }, null, key, member, seconds);
    }

    public Long srem(String key, String member) {
        return this.srem(key, member, null);
    }

}
