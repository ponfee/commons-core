package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import code.ponfee.commons.collect.Collects;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Hashing;
import redis.clients.util.Pool;

/**
 * 分片Sentinel连接池
 * http://blog.csdn.net/dc_726/article/details/48084373
 * 参考：https://github.com/warmbreeze/sharded-jedis-sentinel-pool并修复其连接池泄露的bug
 * 
 * @author Ponfee
 */
public class ShardedJedisSentinelPool extends Pool<ShardedJedis> {

    private static Logger logger = LoggerFactory.getLogger(ShardedJedisSentinelPool.class);
    private static final int MAX_RETRY_SENTINEL = 10;

    protected final GenericObjectPoolConfig poolConfig;
    protected final int timeout;
    protected final String password;
    protected final int database;

    private int sentinelRetry = 0;
    protected final Set<MasterListener> masterListeners = new HashSet<>();
    private List<HostAndPort> currentHostMasters;

    public ShardedJedisSentinelPool(List<String> masters, Set<String> sentinels) {
        this(new GenericObjectPoolConfig(), masters, sentinels, 
             null, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_DATABASE);
    }

    public ShardedJedisSentinelPool(GenericObjectPoolConfig poolConfig, List<String> masters, 
                                    Set<String> sentinels, String password) {
        this(poolConfig, masters, sentinels, password, 
             Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_DATABASE);
    }

    public ShardedJedisSentinelPool(GenericObjectPoolConfig poolConfig, List<String> masters,
                                    Set<String> sentinels, String password, int timeout) {
        this(poolConfig, masters, sentinels, password, timeout, Protocol.DEFAULT_DATABASE);
    }

    public ShardedJedisSentinelPool(GenericObjectPoolConfig poolConfig, List<String> masters, 
                                    Set<String> sentinels, String password, int timeout, int database) {
        this.poolConfig = poolConfig;
        this.timeout = timeout;
        this.password = password;
        this.database = database;

        initPool(initSentinels(sentinels, masters));
    }

    @Override
    public ShardedJedis getResource() {
        ShardedJedis jedis = super.getResource();
        jedis.setDataSource(this);
        return jedis;
    }

    /**
     * @deprecated starting from Jedis 3.0 this method will not be exposed. 
     *             Resource cleanup should be done using 
     * @see {@link redis.clients.jedis.Jedis#close()}
     */
    @Override
    public void returnBrokenResource(final ShardedJedis resource) {
        if (resource != null) {
            super.returnBrokenResourceObject(resource);
        }
    }

    /**
     * @deprecated starting from Jedis 3.0 this method will not be exposed. 
     *             Resource cleanup should be done using 
     * @see {@link redis.clients.jedis.Jedis#close()}
     */
    @Override
    public void returnResource(final ShardedJedis resource) {
        if (resource != null) {
            resource.resetState();
            super.returnResourceObject(resource);
        }
    }

    @Override
    public void destroy() {
        for (MasterListener m : masterListeners) {
            m.shutdown();
        }

        super.destroy();
    }

    public List<HostAndPort> getCurrentHostMaster() {
        return currentHostMasters;
    }

    private void initPool(List<HostAndPort> masters) {
        if (!equals(currentHostMasters, masters)) {
            StringBuilder builder = new StringBuilder();
            for (HostAndPort master : masters) {
                builder.append(master.toString());
                builder.append(" ");
            }
            logger.info("Created ShardedJedisPool to master at [{}]", builder.toString());
            List<JedisShardInfo> shardMasters = makeShardInfoList(masters);
            super.initPool(poolConfig, new ShardedJedisFactory(shardMasters, Hashing.MURMUR_HASH, null));
            currentHostMasters = masters;
        }
    }

    private boolean equals(List<HostAndPort> currentShardMasters, List<HostAndPort> shardMasters) {
        if (currentShardMasters == null || shardMasters == null
            || currentShardMasters.size() != shardMasters.size()) {
            return false;
        }

        for (int i = 0; i < currentShardMasters.size(); i++) {
            if (!Objects.equal(currentShardMasters.get(i), shardMasters.get(i))) {
                return false;
            }
        }
        return true;
    }

    private List<JedisShardInfo> makeShardInfoList(List<HostAndPort> masters) {
        List<JedisShardInfo> shardMasters = new ArrayList<>();
        for (HostAndPort master : masters) {
            JedisShardInfo jedisShardInfo = new JedisShardInfo(master.getHost(), master.getPort(), timeout);
            jedisShardInfo.setPassword(password);

            shardMasters.add(jedisShardInfo);
        }
        return shardMasters;
    }

    private List<HostAndPort> initSentinels(Set<String> sentinels, final List<String> masters) {

        Map<String, HostAndPort> masterMap = new HashMap<>();
        List<HostAndPort> shardMasters = new ArrayList<>();

        logger.info("Trying to find all master from available Sentinels...");

        for (String masterName : masters) {
            HostAndPort master = null;
            boolean fetched = false;

            String invalid = null;
            while (!fetched && sentinelRetry < MAX_RETRY_SENTINEL) {
                for (String sentinel : sentinels) {
                    final HostAndPort hap = toHostAndPort(sentinel.split(":", 2));

                    logger.info("Connecting to Sentinel {}", hap);

                    Jedis jedis = null;
                    try {
                        jedis = new Jedis(hap.getHost(), hap.getPort());
                        master = masterMap.get(masterName);
                        if (master == null) {
                            List<String> hostAndPort = jedis.sentinelGetMasterAddrByName(masterName);
                            if (hostAndPort != null && hostAndPort.size() > 0) {
                                master = toHostAndPort(hostAndPort.toArray(new String[hostAndPort.size()]));
                                logger.info("Found Redis master at {}", master);
                                shardMasters.add(master);
                                masterMap.put(masterName, master);
                                fetched = true;
                                break;
                            }
                        }
                    } catch (JedisConnectionException e) {
                        logger.warn("Cannot connect to sentinel running @ {}. Trying next one.", hap);
                    } finally {
                        if (jedis != null) {
                            try {
                                jedis.disconnect();
                            } catch (Exception ignored) {
                                ignored.printStackTrace();
                            }
                            try {
                                jedis.close();
                            } catch (Exception ignored) {
                                ignored.printStackTrace();
                            }
                            jedis = null;
                        }
                    }
                }

                if (null == master) {
                    invalid = "{" + masterName + " " + sentinels.toString() + "}";
                    try {
                        logger.warn("retry connect {}", invalid);
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                        ignored.printStackTrace();
                    }
                    fetched = false;
                    sentinelRetry++;
                }
            }

            // Try MAX_RETRY_SENTINEL times.
            if (!fetched && sentinelRetry >= MAX_RETRY_SENTINEL) {
                logger.error("{} down and try {} times, Abort.", invalid, MAX_RETRY_SENTINEL);
                throw new JedisConnectionException("invalid " + invalid + " cannot connect all sentinels, Abort.");
            }
        }

        // All shards master must been accessed.
        if (masters.size() != 0 && masters.size() == shardMasters.size()) {
            logger.info("Starting Sentinel listeners...");
            for (String sentinel : sentinels) {
                final HostAndPort hap = toHostAndPort(sentinel.split(":", 2));
                MasterListener masterListener = new MasterListener(masters, hap.getHost(), hap.getPort());
                masterListeners.add(masterListener);
                masterListener.start();
            }
        }

        return shardMasters;
    }

    private HostAndPort toHostAndPort(String[] array) {
        return new HostAndPort(array[0], Integer.parseInt(array[1]));
    }

    /**
     * PoolableObjectFactory custom impl.
     */
    protected static class ShardedJedisFactory implements PooledObjectFactory<ShardedJedis> {
        private final List<JedisShardInfo> shards;
        private final Hashing algo;
        private final Pattern keyTagPattern;

        public ShardedJedisFactory(List<JedisShardInfo> shards, Hashing algo, Pattern keyTagPattern) {
            this.shards = shards;
            this.algo = algo;
            this.keyTagPattern = keyTagPattern;
        }

        @Override
        public PooledObject<ShardedJedis> makeObject() {
            ShardedJedis jedis = new ShardedJedis(shards, algo, keyTagPattern);
            return new DefaultPooledObject<>(jedis);
        }

        @Override
        public void destroyObject(PooledObject<ShardedJedis> pooledShardedJedis) {
            final ShardedJedis shardedJedis = pooledShardedJedis.getObject();
            for (Jedis jedis : shardedJedis.getAllShards()) {
                try {
                    jedis.quit();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                try {
                    jedis.disconnect();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }

        @Override
        public boolean validateObject(PooledObject<ShardedJedis> pooledShardedJedis) {
            try {
                ShardedJedis jedis = pooledShardedJedis.getObject();
                for (Jedis shard : jedis.getAllShards()) {
                    if (!"PONG".equals(shard.ping())) {
                        return false;
                    }
                }
                return true;
            } catch (Exception ex) {
                return false;
            }
        }

        @Override
        public void activateObject(PooledObject<ShardedJedis> p) {
            // do-non
        }

        @Override
        public void passivateObject(PooledObject<ShardedJedis> p) {
            // do-non
        }
    }

    protected class JedisPubSubAdapter extends JedisPubSub {
        @Override
        public void onMessage(String channel, String message) {}

        @Override
        public void onPMessage(String pattern, String channel, String message) {}

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {}

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {}

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {}

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {}
    }

    protected class MasterListener extends Thread {

        protected final List<String> masters;
        protected final String host;
        protected final int port;
        protected long retryIntervalMillis = 5000;
        protected Jedis jedis;
        protected final AtomicBoolean running = new AtomicBoolean(false);

        public MasterListener(List<String> masters, String host, int port) {
            Preconditions.checkArgument(masters != null && !masters.isEmpty());
            Preconditions.checkArgument(StringUtils.isNotEmpty(host));

            this.masters = masters.stream().sorted().collect(Collectors.toList());
            this.host = host;
            this.port = port;
        }

        public MasterListener(List<String> masters, String host, 
                              int port, long retryIntervalMillis) {
            this(masters, host, port);
            this.retryIntervalMillis = retryIntervalMillis;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(masters.toArray())
                          .append(host).append(port).toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MasterListener)) {
                return false;
            }

            MasterListener other = (MasterListener) obj;
            return Collects.different(this.masters, other.masters).isEmpty()
                && this.host.equals(other.host) && this.port == other.port;
        }

        @Override
        public void run() {
            running.set(true);
            while (running.get()) {
                jedis = new Jedis(host, port);
                try {
                    jedis.subscribe(new JedisPubSubAdapter() {
                        @Override
                        public void onMessage(String channel, String message) {
                            logger.info("Sentinel {}:{} published: {}.", host, port, message);
                            String[] array = message.split(" ");
                            if (array.length > 3) {
                                int index = masters.indexOf(array[0]);
                                if (index >= 0) {
                                    HostAndPort newHostMaster = toHostAndPort(new String[] { array[3], array[4] });
                                    List<HostAndPort> newHostMasters = new ArrayList<>();
                                    for (int i = 0; i < masters.size(); i++) {
                                        newHostMasters.add(null);
                                    }
                                    Collections.copy(newHostMasters, currentHostMasters);
                                    newHostMasters.set(index, newHostMaster);
                                    initPool(newHostMasters);
                                } else {
                                    StringBuffer sb = new StringBuffer();
                                    for (String masterName : masters) {
                                        sb.append(masterName).append(",");
                                    }
                                    logger.info("Ignoring message on +switch-master for master name {}, "
                                              + "our monitor master name are [{}]", array[0], sb);
                                }
                            } else {
                                logger.error("Invalid message received on Sentinel {}:{} "
                                           + "on channel +switch-master: {}", host, port, message);
                            }
                        }
                    }, "+switch-master");
                } catch (JedisConnectionException e) {
                    if (running.get()) {
                        logger.error("Lost connection to Sentinel at {}:{}. "
                                   + "Sleeping {}ms and retrying.", host, port, retryIntervalMillis);
                        try {
                            Thread.sleep(retryIntervalMillis);
                        } catch (InterruptedException ignored) {
                            ignored.printStackTrace();
                        }
                    } else {
                        logger.info("Unsubscribing from Sentinel at {}:{}", host, port);
                    }
                }
            }
        }

        public void shutdown() {
            try {
                logger.info("Shutting down listener on {}:{}", host, port);
                running.set(false);
                // This isn't good, the Jedis object is not thread safe
                jedis.disconnect();
            } catch (Exception e) {
                logger.error("Caught exception while shutting down", e);
            }
        }
    }

}
