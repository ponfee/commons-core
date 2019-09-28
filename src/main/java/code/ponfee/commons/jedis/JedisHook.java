package code.ponfee.commons.jedis;

import redis.clients.jedis.ShardedJedis;

/**
 * 回调函数（无返回值时使用）
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface JedisHook {

    void hook(ShardedJedis shardedJedis);

    /**
     * 钩子函数，无返回值
     * @param jedisClient JedisClient
     * @param args        参数列表
     */
    default void hook(JedisClient jedisClient, Object... args) {
        try (ShardedJedis shardedJedis = jedisClient.getShardedJedis()) {
            this.hook(shardedJedis);
        } catch (Exception e) {
            JedisClient.exception(e, args);
        }
    }
}
