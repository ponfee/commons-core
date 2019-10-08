package code.ponfee.commons.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import code.ponfee.commons.serial.Serializer;

public class ShardedJedisClientBuilder {

    private final GenericObjectPoolConfig poolConfig;
    private final String nodes;

    private Integer timeout;
    private Serializer serializer;

    private ShardedJedisClientBuilder(GenericObjectPoolConfig poolConfig, String nodes) {
        this.poolConfig = poolConfig;
        this.nodes = nodes;
    }

    public static ShardedJedisClientBuilder newBuilder(GenericObjectPoolConfig poolConfig, String nodes) {
        return new ShardedJedisClientBuilder(poolConfig, nodes);
    }

    public ShardedJedisClientBuilder timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ShardedJedisClientBuilder serializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    public JedisClient build() {
        return new JedisClient(poolConfig, nodes, timeout, serializer);
    }

}
