package code.ponfee.commons.jedis;

import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import redis.clients.util.SafeEncoder;

/**
 * redis sorted set（有序集合操作类）
 * 
 * @author Ponfee
 */
public class ZSetOperations extends JedisOperations {

    ZSetOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * <pre>
     *  将一个或多个 member 元素及其 score 值加入到有序集 key 当中。
     *  如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。
     *  score 值可以是整数值或双精度浮点数。
     *  如果 key 不存在，则创建一个空的有序集并执行 ZADD 操作。
     * </pre>
     * 
     * @param key
     * @param score
     * @param member
     * @param seconds
     * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员
     */
    public long zadd(String key, double score, String member, Integer seconds) {
        return call(shardedJedis -> {
            long rtn = shardedJedis.zadd(key, score, member);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, 0L, key, score, member, seconds);
    }

    public long zadd(String key, double score, String member) {
        return this.zadd(key, score, member, null);
    }

    /**
     * 批量添加
     * @param key
     * @param scoreMembers
     * @param seconds
     * @return
     */
    public long zadd(String key, Map<String, Double> scoreMembers, Integer seconds) {
        return call(shardedJedis -> {
            long rtn = shardedJedis.zadd(key, scoreMembers);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, 0L, key, scoreMembers, seconds);
    }

    public long zadd(String key, Map<String, Double> scoreMembers) {
        return this.zadd(key, scoreMembers, null);
    }

    public long zaddBinary(String key, Map<byte[], Double> scoreMembers, Integer seconds) {
        return call(shardedJedis -> {
            Jedis j = shardedJedis.getShard(key);
            long rtn = j.zadd(SafeEncoder.encode(key), scoreMembers);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, 0L, key, scoreMembers, seconds);
    }

    /**
     * 批量添加
     * @param key           byte array of key
     * @param scoreMembers  byte array member
     * @param seconds       expire in spec seconds
     * @return
     */
    public long zadd(byte[] key, Map<byte[], Double> scoreMembers, Integer seconds) {
        return call(shardedJedis -> {
            long rtn = shardedJedis.zadd(key, scoreMembers);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, 0L, key, scoreMembers, seconds);
    }

    public long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        return this.zadd(key, scoreMembers, null);
    }

    /**
     * <pre>
     *  返回有序集 key 中，成员 member 的 score 值。
     *  如果 member 元素不是有序集 key 的成员，或 key 不存在，返回 nil 。
     * </pre>
     * 
     * @param key
     * @param member
     * @param seconds
     * @return member 成员的 score 值，以字符串形式表示。
     */
    public Double zscore(String key, String member, Integer seconds) {
        return call(shardedJedis -> {
            Double score = shardedJedis.zscore(key, member);
            expire(shardedJedis, key, seconds);
            return score;
        }, null, key, member, seconds);
    }

    public Double zscore(String key, String member) {
        return this.zscore(key, member, null);
    }

    /**
     * 返回有序集 key 的基数。
     * @param key
     * @param seconds
     * @return 当 key 存在且是有序集类型时，返回有序集的基数。当 key 不存在时，返回 0 。
     */
    public Long zcard(String key, Integer seconds) {
        return call(shardedJedis -> {
            Long count = shardedJedis.zcard(key);
            if (count != null && count > 0) {
                expire(shardedJedis, key, seconds);
            }
            return count;
        }, null, key, seconds);
    }

    public Long zcard(String key) {
        return zcard(key, null);
    }

    /**
     * <pre>
     *  移除有序集 key 中，指定排名(rank)区间内的所有成员。
     *  区间分别以下标参数 start 和 stop 指出，包含 start 和 stop 在内。
     *  下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。
     *  你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。
     * </pre>
     * 
     * @param key
     * @param start
     * @param end
     * @param seconds
     * @return 被移除成员的数量
     */
    public Long zremrangeByRank(String key, long start, long end, Integer seconds) {
        return call(shardedJedis -> {
            Long count = shardedJedis.zremrangeByRank(key, start, end);
            expire(shardedJedis, key, seconds);
            return count;
        }, null, key, start, end, seconds);
    }

    /**
     * <pre>
     *  返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。
     *  有序集成员按 score 值递减(从大到小)的次序排列。
     * </pre>
     * 
     * @param key
     * @param max
     * @param min
     * @param offset
     * @param count
     * @param seconds
     * @return 指定区间内，带有 score 值(可选)的有序集成员的列表
     */
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, 
                                                 int offset, int count, Integer seconds) {
        return call(shardedJedis -> {
            Set<Tuple> set = shardedJedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
            expire(shardedJedis, key, seconds);
            return set;
        }, null, key, max, min, offset, count, seconds);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, 
                                                 int offset, int count) {
        return this.zrevrangeByScoreWithScores(key, max, min, offset, count, null);
    }

    /**
     * @param key
     * @param max
     * @param min
     * @param seconds
     * @return
     */
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, Integer seconds) {
        return call(shardedJedis -> {
            Set<Tuple> set = shardedJedis.zrevrangeByScoreWithScores(key, max, min);
            expire(shardedJedis, key, seconds);
            return set;
        }, null, key, max, min, seconds);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return this.zrevrangeByScoreWithScores(key, max, min, null);
    }

    /**
     * <pre>
     *  返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。
     *  有序集成员按 score 值递减(从大到小)的次序排列。
     * </pre>
     * 
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * @param seconds
     * @return 指定区间内，带有 score 值(可选)的有序集成员的列表
     */
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, 
                                              int offset, int count, Integer seconds) {
        return call(shardedJedis -> {
            Set<Tuple> set = shardedJedis.zrangeByScoreWithScores(key, min, max, offset, count);
            expire(shardedJedis, key, seconds);
            return set;
        }, null, key, min, max, offset, count, seconds);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, 
                                              int offset, int count) {
        return this.zrangeByScoreWithScores(key, min, max, offset, count, null);
    }

    /**
     * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。
     * @param key
     * @param member
     * @param seconds
     * @return 被成功移除的成员的数量，不包括被忽略的成员。
     */
    public Long zrem(String key, String member, Integer seconds) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.zrem(key, member);
            expire(shardedJedis, key, seconds);
            return rtn;
        }, null, key, member, seconds);
    }

    public Long zrem(String key, String member) {
        return this.zrem(key, member, null);
    }

    /**
     * <pre>
     *  返回有序集 key 中，指定区间内的成员。
     *  其中成员的位置按 score 值递减(从大到小)来排列。
     *  具有相同 score 值的成员按字典序的逆序(reverse lexicographical order)排列。
     * </pre>
     * 
     * @param key
     * @param start
     * @param end
     * @param seconds
     * @return 指定区间内，带有 score 值(可选)的有序集成员的列表。
     */
    public Set<String> zrevrange(String key, long start,
                                 long end, Integer seconds) {
        return call(shardedJedis -> {
            Set<String> result = shardedJedis.zrevrange(key, start, end);
            expire(shardedJedis, key, seconds);
            return result;
        }, null, key, start, end, seconds);
    }

    public Set<String> zrevrange(String key, long start, long end) {
        return this.zrevrange(key, start, end, null);
    }

    /**
     * <pre>
     *  返回有序集 key 中，指定区间内的成员。
     *  其中成员的位置按 score 值递增(从小到大)来排序。
     *  具有相同 score 值的成员按字典序(lexicographical order )来排列。
     * </pre>
     * 
     * @param key
     * @param start
     * @param end
     * @param seconds
     * @return
     */
    public Set<String> zrange(String key, long start, long end, Integer seconds) {
        return call(shardedJedis -> {
            Set<String> result = shardedJedis.zrange(key, start, end);
            expire(shardedJedis, key, seconds);
            return result;
        }, null, key, start, end, seconds);
    }

    public Set<String> zrange(String key, long start, long end) {
        return this.zrange(key, start, end, null);
    }

    /**
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。
     * 有序集成员按 score 值递增(从小到大)次序排列。
     * WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。
     * @param key
     * @param start
     * @param end
     * @param seconds
     * @return
     */
    public Set<Tuple> zrangeWithScores(String key, long start, long end, Integer seconds) {
        return call(shardedJedis -> {
            Set<Tuple> result = shardedJedis.zrangeWithScores(key, start, end);
            expire(shardedJedis, key, seconds);
            return result;
        }, null, key, start, end, seconds);
    }

    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        return this.zrangeWithScores(key, start, end, null);
    }

    /**
     * 返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。
     * @param key
     * @param min
     * @param max
     * @param seconds
     * @return score 值在 min 和 max 之间的成员的数量。
     */
    public Long zcount(String key, double min, double max, Integer seconds) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.zcount(key, min, max);
            expire(shardedJedis, key, seconds);
            return rtn;
        }, null, key, min, max, seconds);
    }

    public Long zcount(String key, double min, double max) {
        return this.zcount(key, min, max, null);
    }

    /**
     * <pre>
     *  为有序集 key 的成员 member 的 score 值加上增量 increment 。
     *  可以通过传递一个负数值 increment ，让 score 减去相应的值，比如 ZINCRBY key -5 member ，就是让 member 的 score 值减去 5 。
     *  当 key 不存在，或 member 不是 key 的成员时， ZINCRBY key increment member 等同于 ZADD key increment member 。
     * </pre>
     * 
     * @param key
     * @param member
     * @param score
     * @param seconds
     * @return member 成员的新 score 值，以字符串形式表示。
     */
    public Double zincrby(String key, String member, double score, Integer seconds) {
        return call(shardedJedis -> {
            Double rtn = shardedJedis.zincrby(key, score, member);
            expireForce(shardedJedis, key, seconds);
            return rtn;
        }, null, key, member, score, seconds);
    }

    public Double zincrby(String key, String member, double score) {
        return this.zincrby(key, member, score, null);
    }

    /**
     * <pre>
     *  返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列。
     *  排名以 0 为底，也就是说， score 值最小的成员排名为 0 。
     * </pre>
     * 
     * @param key
     * @param member
     * @param seconds
     * @return 如果 member 是有序集 key 的成员，返回 member 的排名。如果 member 不是有序集 key 的成员，返回 nil 。
     */
    public Long zrank(String key, String member, Integer seconds) {
        return call(shardedJedis -> {
            Long rank = shardedJedis.zrank(key, member);
            expire(shardedJedis, key, seconds);
            return rank;
        }, null, key, member, seconds);
    }

    public Long zrank(String key, String member) {
        return this.zrank(key, member, null);
    }

    /**
     * <pre>
     *  返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递减(从大到小)排序。
     *  排名以 0 为底，也就是说， score 值最大的成员排名为 0 。
     * </pre>
     * 
     * @param key
     * @param member
     * @param seconds
     * @return 如果 member 是有序集 key 的成员，返回 member 的排名。如果 member 不是有序集 key 的成员，返回 nil 。
     */
    public Long zrevrank(String key, String member, Integer seconds) {
        return call(shardedJedis -> {
            Long revrank = shardedJedis.zrevrank(key, member);
            expire(shardedJedis, key, seconds);
            return revrank;
        }, null, key, member, seconds);
    }

    /**
     * <pre>
     *  移除有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。
     *  score 值等于 min 或 max 的成员也可以不包括在内，详情请参见 ZRANGEBYSCORE 命令。
     * </pre>
     * 
     * @param key
     * @param start
     * @param end
     * @param seconds 被移除成员的数量
     */
    public Long zremrangeByScore(String key, long start, long end, Integer seconds) {
        return call(shardedJedis -> {
            Long rtn = shardedJedis.zremrangeByScore(key, start, end);
            expire(shardedJedis, key, seconds);
            return rtn;
        }, null, key, start, end, seconds);
    }

    public Long zremrangeByScore(String key, long start, long end) {
        return this.zremrangeByScore(key, start, end, null);
    }

    /**
     * 判断是否是有序集的成员
     * @param key
     * @param member
     * @return
     */
    public boolean zismember(String key, String member) {
        return this.zrank(key, member) != null;
    }

}
