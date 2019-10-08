package code.ponfee.commons.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import code.ponfee.commons.serial.Serializer;

public class ShardedSentinelJedisClientBuilder {

    private final GenericObjectPoolConfig poolConfig;
    private final String masters;
    private final String sentinels;

    private String password;
    private Integer timeout;
    private Serializer serializer;

    private ShardedSentinelJedisClientBuilder(GenericObjectPoolConfig poolConfig, String masters, String sentinels) {
        this.poolConfig = poolConfig;
        this.masters = masters;
        this.sentinels = sentinels;
    }

    public static ShardedSentinelJedisClientBuilder newBuilder(GenericObjectPoolConfig poolConfig, String masters, String sentinels) {
        return new ShardedSentinelJedisClientBuilder(poolConfig, masters, sentinels);
    }

    public ShardedSentinelJedisClientBuilder password(String password) {
        this.password = password;
        return this;
    }

    public ShardedSentinelJedisClientBuilder timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ShardedSentinelJedisClientBuilder serializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    public JedisClient build() {
        return new JedisClient(poolConfig, masters, sentinels, password, timeout, serializer);
    }

}
