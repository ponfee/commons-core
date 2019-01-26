package code.ponfee.commons.jedis;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.JedisPubSub;

/**
 * redis message queue
 * @author fupf
 */
public class MQOperations extends JedisOperations {

    public static final String JEDIS_MQ_OPS = "jedis-mq-ops";
    private static final byte[] JEDIS_MQ_OPS_BYTES = JEDIS_MQ_OPS.getBytes();

    MQOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * 将信息 message发送到指定的频道channel
     * @param channel
     * @param message
     * @return 接收到信息message的订阅者数量
     */
    public Long publish(String channel, String message) {
        return call(shardedJedis -> {
            return getShard(shardedJedis, JEDIS_MQ_OPS_BYTES).publish(channel, message);
        }, null, channel, message);
    }

    /**
     * 发布消息
     * @param channel
     * @param message
     * @return  接收到信息message的订阅者数量
     */
    public Long publish(byte[] channel, byte[] message) {
        return call(shardedJedis -> {
            return getShard(shardedJedis, JEDIS_MQ_OPS_BYTES).publish(channel, message);
        }, null, channel, message);
    }

    /**
     * 订阅给定的一个或多个频道的信息
     * @param jedisPubSub
     * @param channels
     */
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        hook(shardedJedis -> {
            getShard(shardedJedis, JEDIS_MQ_OPS_BYTES).subscribe(jedisPubSub, channels);
        });
    }

    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        hook(shardedJedis -> {
            getShard(shardedJedis, JEDIS_MQ_OPS_BYTES).psubscribe(jedisPubSub, patterns);
        });
    }

    public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
        hook(shardedJedis -> {
            getShard(shardedJedis, JEDIS_MQ_OPS_BYTES).subscribe(jedisPubSub, channels);
        });
    }

    public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
        hook(shardedJedis -> {
            getShard(shardedJedis, JEDIS_MQ_OPS_BYTES).psubscribe(jedisPubSub, patterns);
        });
    }

    public List<String> pubsubChannels(String patterns) {
        return call(shardedJedis -> {
            return getShard(shardedJedis, JEDIS_MQ_OPS_BYTES).pubsubChannels(patterns);
        }, null, patterns);
    }

    public Map<String, String> pubsubNumSub(String... channels) {
        return call(shardedJedis -> {
            return getShard(shardedJedis, JEDIS_MQ_OPS_BYTES).pubsubNumSub(channels);
        }, null, String.valueOf(channels));
    }

    public Long pubsubNumPat() {
        return call(shardedJedis -> {
            return getShard(shardedJedis, JEDIS_MQ_OPS_BYTES).pubsubNumPat();
        }, null);
    }

}
