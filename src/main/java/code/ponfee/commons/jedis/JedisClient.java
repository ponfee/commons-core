package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

/**
 * jedis客户端
 * @author fupf
 */
public class JedisClient implements DisposableBean {

    private final static String SEPARATOR = ";";
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

    // -----------------------------------ShardedJedisPool（分片模式）-----------------------------------
    public JedisClient(GenericObjectPoolConfig poolCfg, String hosts) {
        this(poolCfg, hosts, DEFAULT_TIMEOUT_MILLIS, null);
    }

    public JedisClient(GenericObjectPoolConfig poolCfg, String hosts, int timeout) {
        this(poolCfg, hosts, timeout, null);
    }

    public JedisClient(GenericObjectPoolConfig poolCfg, String hosts, Serializer serializer) {
        this(poolCfg, hosts, DEFAULT_TIMEOUT_MILLIS, serializer);
    }

    /**
     * <pre>
     *  ShardedJedis注入格式：
     *   host1:port1;host2:port2;host3:port3
     *   name1:host1:port1;name2:host2:port2;name3:host3:port3
     *   name1:host1:port1:password1;name2:host2:port2:password2;name3:host3:port3:password3
     * </pre>
     * @param poolCfg
     * @param hosts
     * @param timeout
     * @param serializer
     */
    public JedisClient(GenericObjectPoolConfig poolCfg, String hosts, 
                       int timeout, Serializer serializer) {
        List<JedisShardInfo> infos = new ArrayList<>();
        for (String str : hosts.split(SEPARATOR)) {
            if (StringUtils.isBlank(str)) {
                continue;
            }

            String name, host, port, password = null;
            String[] array = str.split(":");
            if (array.length == 2) {
                host = array[0].trim();
                port = array[1].trim();
                name = host + ":" + port;
            } else if (array.length == 3 || array.length == 4) {
                name = array[0].trim();
                host = array[1].trim();
                port = array[2].trim();
                if (array.length == 4) {
                    password = array[3].trim();
                }
            } else {
                logger.error("invalid hosts config[" + hosts + "]");
                continue;
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
            throw new IllegalArgumentException("invalid hosts config[" + hosts + "]");
        }

        initClient(new ShardedJedisPool(poolCfg, infos), serializer);
    }

    // -----------------------------------ShardedJedisSentinelPool（哨兵+分片）-----------------------------------
    public JedisClient(GenericObjectPoolConfig poolCfg, 
                       String masters, String sentinels) {
        this(poolCfg, masters, sentinels, null, DEFAULT_TIMEOUT_MILLIS, null);
    }

    public JedisClient(GenericObjectPoolConfig poolCfg, String masters, 
                       String sentinels, int timeout) {
        this(poolCfg, masters, sentinels, null, timeout, null);
    }

    public JedisClient(GenericObjectPoolConfig poolCfg, String masters, 
                       String sentinels, Serializer serializer) {
        this(poolCfg, masters, sentinels, null, DEFAULT_TIMEOUT_MILLIS, serializer);
    }

    public JedisClient(GenericObjectPoolConfig poolCfg, String masters, 
                       String sentinels, String password) {
        this(poolCfg, masters, sentinels, password, DEFAULT_TIMEOUT_MILLIS, null);
    }

    public JedisClient(GenericObjectPoolConfig poolCfg, String masters,
                       String sentinels, String password, Serializer serializer) {
        this(poolCfg, masters, sentinels, password, DEFAULT_TIMEOUT_MILLIS, serializer);
    }

    /**
     * @param poolCfg    连接池
     * @param masters    哨兵mastername名称，多个以“;”分隔，如：sen_redis_master1;sen_redis_master2
     * @param sentinels  哨兵服务器ip及端口，多个以“;”分隔，如：127.0.0.1:16379;127.0.0.1:16380;
     * @param password   密码
     * @param timeout    超时时间
     * @param serializer 序列化对象
     */
    public JedisClient(GenericObjectPoolConfig poolCfg, String masters, String sentinels, 
                       String password, int timeout, Serializer serializer) {
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
     * @param args              参数
     * @return a result
     */
    public final <T> T call(JedisCallback<T> call, T occurErrorRtnVal, Object... args) {
        return call.call(this, occurErrorRtnVal, args);
    }

    /**
     * 勾子函数：无返回值
     * @param hook 调用勾子函数
     * @param args 参数列表
     */
    public final void hook(JedisHook hook, Object... args) {
        hook.hook(this, args);
    }

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
                        jedis.close();
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
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
        List<String> list = new ArrayList<>(array.length);
        for (String str : array) {
            if (StringUtils.isNotBlank(str)) {
                list.add(str.trim());
            }
        }
        return list;
    }

    ShardedJedis getShardedJedis() throws JedisException {
        return this.shardedJedisPool.getResource();
    }

    /*void closeShardedJedis(ShardedJedis shardedJedis) {
        if (shardedJedis != null) try {
            shardedJedis.close();
            //shardedJedis.disconnect();
        } catch (Throwable e) {
            logger.error("redis close occur error", e);
        }
    }
    
    Jedis getJedis(String key) {
        return this.getShardedJedis().getShard(key);
    }
    
    void closeJedis(Jedis jedis) {
        jedis.close();
        //jedis.disconnect();
    }*/

    final <T> byte[] serialize(T t, boolean isCompress) {
        return serializer.serialize(t, isCompress);
    }

    final <T> byte[] serialize(T t) {
        return this.serialize(t, false);
    }

    final <T> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        return serializer.deserialize(data, clazz, isCompress);
    }

    final <T> T deserialize(byte[] data, Class<T> clazz) {
        return this.deserialize(data, clazz, false);
    }

}
