package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import code.ponfee.commons.serial.FstSerializer;
import code.ponfee.commons.serial.Serializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

/**
 * jedis客户端
 * 
 * @author Ponfee
 */
public class JedisClient implements DisposableBean {

    private final static String SEPARATOR = "[ ,]"; // 空格或逗号分隔
    private final static int DEFAULT_TIMEOUT_MILLIS = 2000; // default 2000 millis timeout
    private static final int MAX_BYTE_LEN = 30; // max bytes length
    private static final int MAX_LEN = 40; // max str length
    private static Logger logger = LoggerFactory.getLogger(JedisClient.class);

    private Pool<ShardedJedis> shardedJedisPool;
    private Serializer serializer;
    private KeysOperations keysOps;
    private ValueOperations valueOps;
    private HashOperations hashOps;
    private ListOperations listOps;
    private SetOpertions setOps;
    private ZSetOperations zsetOps;
    private ScriptOperations scriptOps;
    private MQOperations mqOps;

    // ----------------------------------------------------------------------ShardedJedisPool（分片模式）
    /**
     * <pre>
     *  ShardedJedis注入格式：
     *   host1:port1,host2:port2,host3:port3
     *   host1:port1:password1,host2:port2:password2,host3:port3:password3
     * </pre>
     * @param poolCfg    连接池
     * @param nodes      redis各分片节点信息，多个逗号分隔
     * @param timeout    超时时间 
     * @param serializer 序列化方式
     */
    public JedisClient(GenericObjectPoolConfig poolCfg, String nodes, 
                       Integer timeout, Serializer serializer) {
        if (timeout == null || timeout < 1) {
            timeout = DEFAULT_TIMEOUT_MILLIS;
        }

        List<JedisShardInfo> infos = new ArrayList<>();
        for (String str : nodes.split(SEPARATOR)) {
            if (StringUtils.isBlank(str)) {
                continue;
            }

            String[] array = str.split(":", 3);
            String host = array[0].trim(), port = array[1].trim(), 
                   name = host + ":" + port, password = null;
            if (array.length == 3) {
                password = array[2].trim();
            }

            JedisShardInfo info = new JedisShardInfo(host, Integer.parseInt(port), timeout, name);
            if (StringUtils.isNotBlank(password)) {
                info.setPassword(password);
            }
            if (testConnectJedis(info, 3)) {
                infos.add(info);
            }
        }
        if (infos.isEmpty()) {
            throw new IllegalArgumentException("invalid nodes config[" + nodes + "]");
        }

        initClient(new ShardedJedisPool(poolCfg, infos), serializer);
    }

    // ----------------------------------------------------------------------ShardedJedisSentinelPool（哨兵+分片）
    /**
     * @param poolCfg    连接池
     * @param masters    哨兵mastername名称，多个以“,”分隔，如：sen_redis_master1,sen_redis_master2
     * @param sentinels  哨兵服务器ip及端口，多个以“,”分隔，如：127.0.0.1:16379,127.0.0.1:16380
     * @param password   密码
     * @param timeout    超时时间
     * @param serializer 序列化对象
     */
    public JedisClient(GenericObjectPoolConfig poolCfg, String masters, String sentinels, 
                       String password, Integer timeout, Serializer serializer) {
        if (timeout == null || timeout < 1) {
            timeout = DEFAULT_TIMEOUT_MILLIS;
        }

        List<String> master = asList(masters.split(SEPARATOR));
        Set<String> sentinel = new HashSet<>(asList(sentinels.split(SEPARATOR)));

        initClient(new ShardedJedisSentinelPool(poolCfg, master, sentinel, password, timeout), serializer);
    }

    /**
     * init sharded jedis
     * @param shardedJedisPool
     * @param serializer
     */
    private void initClient(Pool<ShardedJedis> shardedJedisPool, Serializer serializer) {
        this.serializer = (serializer != null) 
                          ? serializer 
                          : new FstSerializer();

        this.shardedJedisPool = shardedJedisPool;
        this.keysOps   = new KeysOperations(this);
        this.valueOps  = new ValueOperations(this);
        this.hashOps   = new HashOperations(this);
        this.listOps   = new ListOperations(this);
        this.setOps    = new SetOpertions(this);
        this.zsetOps   = new ZSetOperations(this);
        this.scriptOps = new ScriptOperations(this);
        this.mqOps     = new MQOperations(this);
    }

    public KeysOperations keysOps() {
        return this.keysOps;
    }

    public ValueOperations valueOps() {
        return this.valueOps;
    }

    public HashOperations hashOps() {
        return this.hashOps;
    }

    public ListOperations listOps() {
        return this.listOps;
    }

    public SetOpertions setOps() {
        return this.setOps;
    }

    public ZSetOperations zsetOps() {
        return this.zsetOps;
    }

    public ScriptOperations scriptOps() {
        return this.scriptOps;
    }

    public MQOperations mqOps() {
        return this.mqOps;
    }

    @Override
    public void destroy() {
        if (shardedJedisPool != null && !shardedJedisPool.isClosed()) {
            shardedJedisPool.close();
            shardedJedisPool = null;
        }
    }

    /**
     * 回调函数：有返回值
     * @param call              回调对象
     * @param occurErrorRtnVal  出现异常时的返回值
     * @return a result
     */
    public final <T> T call(JedisCallback<T> call, T occurErrorRtnVal) {
        return call.call(this, occurErrorRtnVal);
    }

    /**
     * 勾子函数：无返回值
     * @param hook 调用勾子函数
     */
    public final void hook(JedisHook hook) {
        hook.hook(this);
    }

    // --------------------------------------------------------------------ShardedJedisPipeline.syncAndReturnAll()
    @SuppressWarnings("unchecked")
    public <T> void executePipelined(BiConsumer<ShardedJedisPipeline, T> action,
                                     BiConsumer<T, Object> collector, T... array) {
        this.executePipelined(action, collector, Arrays.asList(array));
    }

    public <T> void executePipelined(BiConsumer<ShardedJedisPipeline, T> action,
                                     BiConsumer<T, Object> collector, List<T> list) {
        valueOps.hook(sj -> executePipelined(sj, action, collector, list), list);
    }

    @SuppressWarnings("unchecked")
    public <K> void executePipelined(ShardedJedis shardedJedis, 
                                     BiConsumer<ShardedJedisPipeline, K> action, 
                                     BiConsumer<K, Object> collector, K... array) {
        this.executePipelined(shardedJedis, action, collector, Arrays.asList(array));
    }

    /**
     * Executed pipelined and return result
     * 
     * @param shardedJedis
     * @param action
     * @param collector
     * @param list
     */
    public <T> void executePipelined(ShardedJedis shardedJedis, 
                                     BiConsumer<ShardedJedisPipeline, T> action, 
                                     BiConsumer<T, Object> collector, List<T> list) {
        ShardedJedisPipeline pipeline = shardedJedis.pipelined();
        list.forEach(elem -> action.accept(pipeline, elem));
        List<Object> resp = pipeline.syncAndReturnAll();
        for (int i = 0, n = list.size(); i < n; i++) {
            // arg-elem -> redis-value
            collector.accept(list.get(i), resp.get(i));
        }
    }

    /**
     * Executed pipelined and return result
     * 
     * @param shardedJedis
     * @param action
     * @return
     */
    public List<Object> executePipelined(ShardedJedis shardedJedis, 
                                        Consumer<ShardedJedisPipeline> action) {
        ShardedJedisPipeline pipeline = shardedJedis.pipelined();
        action.accept(pipeline);
        return pipeline.syncAndReturnAll();
    }

    // --------------------------------------------------------------------ShardedJedisPipeline.sync()
    @SuppressWarnings("unchecked")
    public <T> void executePipelined(BiConsumer<ShardedJedisPipeline, T> action, T... array) {
        this.executePipelined(action, Arrays.asList(array));
    }

    public <T> void executePipelined(BiConsumer<ShardedJedisPipeline, T> action, Collection<T> coll) {
        valueOps.hook(sj -> this.executePipelined(sj, action, coll), coll);
    }

    @SuppressWarnings("unchecked")
    public <T> void executePipelined(ShardedJedis shardedJedis, 
                                     BiConsumer<ShardedJedisPipeline, T> action, 
                                     T... array) {
        this.executePipelined(shardedJedis, action, Arrays.asList(array));
    }

    /**
     * Executed pipelined 
     * 
     * @param shardedJedis
     * @param action
     * @param coll
     */
    public <T> void executePipelined(ShardedJedis shardedJedis, 
                                     BiConsumer<ShardedJedisPipeline, T> action, 
                                     Collection<T> coll) {
        ShardedJedisPipeline pipeline = shardedJedis.pipelined();
        coll.forEach(elem -> action.accept(pipeline, elem));
        pipeline.sync();
    }

    // --------------------------------------------------------execute each 
    public <R> Stream<R> executeSharded(Function<Jedis,  R> action) {
        return valueOps.call(
            shardedJedis -> executeSharded(shardedJedis, action), null
        );
    }

    /**
     * 
     * @param shardedJedis the shardedJedis
     * @param action       the BiFunction for jedis exec batch action
     * @return a stream for batch result
     */
    public <R> Stream<R> executeSharded(ShardedJedis shardedJedis,
                                        Function<Jedis, R> action) {
        Stream<Jedis> stream = shardedJedis.getAllShards().stream();

        List<CompletableFuture<R>> result = stream.map(
            jedis -> CompletableFuture.supplyAsync(
                () -> action.apply(jedis), JedisOperations.EXECUTOR
            )
        ).collect(
            Collectors.toList()
        );

        return result.stream().map(CompletableFuture::join);
    }

    // --------------------------------------------------------package modify methods
    /**
     * 异常处理
     * @param e    the exception
     * @param args method arguments
     */
    static void exception(Exception e, Object... args) {
        StringBuilder builder = new StringBuilder();
        builder.append("redis operation occur error, args(");
        //builder.append(ObjectUtils.getStackTrace(5)).append("(");
        StringBuilder part = new StringBuilder();
        Object arg;
        for (int n = args.length, i = 0; i < n; i++, part.setLength(0)) {
            arg = args[i];
            if (arg == null) {
                part.append("null");
            } else if (i == 0 && (arg instanceof byte[] || arg instanceof Byte[])) {
                if (arg instanceof byte[]) {
                    part.append(toString((byte[]) arg));
                } else {
                    part.append(toString((Byte[]) arg));
                }
            } else {
                part.append(arg.toString());
            }

            if (part.length() > MAX_LEN) {
                part.setLength(MAX_LEN);
                part.append("...");
            }
            builder.append("`").append(part).append("`");
            if (i != n - 1) {
                builder.append(", ");
            }
        } // end for loop

        logger.error(builder.append(")").toString(), e);
    }

    ShardedJedis getShardedJedis() throws JedisException {
        return this.shardedJedisPool.getResource();
    }

    final byte[] serialize(Object obj, boolean isCompress) {
        return serializer.serialize(obj, isCompress);
    }

    final byte[] serialize(Object obj) {
        return this.serialize(obj, false);
    }

    final <T> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        return serializer.deserialize(data, clazz, isCompress);
    }

    final <T> T deserialize(byte[] data, Class<T> clazz) {
        return this.deserialize(data, clazz, false);
    }

    // --------------------------------------------------------private methods
    private static String toString(byte[] bytes) {
        if (bytes.length > MAX_BYTE_LEN) {
            bytes = Arrays.copyOf(bytes, MAX_BYTE_LEN);
        }
        return "b64:" + Base64.getEncoder().encodeToString(bytes);
    }

    private static String toString(Byte[] bytes) {
        int length = Math.min(bytes.length, MAX_BYTE_LEN);
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = bytes[i];
        }
        return "B64:" + Base64.getEncoder().encodeToString(data);
    }

    /**
     * 测试连接jedis是否可用
     * @param jedisInfo
     * @return
     */
    private static boolean testConnectJedis(JedisShardInfo jedisInfo, int retryTimes) {
        do {
            Jedis jedis = null;
            try {
                jedis = jedisInfo.createResource();
                jedis.connect();
                return true;
            } catch (Exception e) {
                logger.error("jedis cannot connect: " + jedisInfo + " - " + e.getMessage());
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
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                logger.error("test jedis connect sleep error", e);
                return false;
            }
        } while (--retryTimes > 0);

        return false;
    }

    private static List<String> asList(String... array) {
        if (array == null) {
            return null;
        }
        return Arrays.stream(array)
                     .filter(StringUtils::isNotBlank)
                     .map(String::trim)
                     .collect(Collectors.toList());
    }

}
