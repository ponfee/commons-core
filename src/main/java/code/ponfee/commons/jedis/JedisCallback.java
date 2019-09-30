package code.ponfee.commons.jedis;

import redis.clients.jedis.ShardedJedis;

/**
 * 回调函数（有返回值时使用）
 * 
 * @author Ponfee
 * @param <T>
 */
@FunctionalInterface
public interface JedisCallback<T> {

    T call(ShardedJedis shardedJedis);

    /**
     * 回调
     * @param jedisClient      JedisClient
     * @param occurErrorRtnVal 出现异常时的返回值
     * @param args             参数列表
     * @return a result
     */
    default T call(JedisClient jedisClient, T occurErrorRtnVal, Object... args) {
        try (ShardedJedis shardedJedis = jedisClient.getShardedJedis()) {
            return this.call(shardedJedis);
        } catch (Exception e) {
            JedisClient.exception(e, args);
            return occurErrorRtnVal;
        }
    }
}
