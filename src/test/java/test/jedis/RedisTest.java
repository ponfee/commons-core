package test.jedis;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.ShardedSentinelJedisClientBuilder;
import code.ponfee.commons.serial.JdkSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class RedisTest {
    private static final String masters = "DDT_CORE_CNSZ22_REDIS_CACHE";
    private static final String servers = "10.202.40.105:8001 10.202.40.105:8002 10.202.40.107:8001";
    private static final String password = "admin.123";
    private static JedisClient client;

    @BeforeClass
    public static void setUp() {
        client = ShardedSentinelJedisClientBuilder
            .newBuilder(buildDefaultPool(), masters, servers)
            .password(password)
            .serializer(new JdkSerializer())
            .build();
    }

    @AfterClass
    public static void tearDown() {
        client.destroy();
    }

    @Test
    public void testExists1() {
        String prefix = "test1:str:";
        int n = 100;
        String[] keys = new String[n];
        for (int i = 0; i < n; i++) {
            if (i % 2 == 0) {
                System.out.println(client.valueOps().set(prefix + i, "", 86400));
            }
            keys[i] = prefix + i;
        }
        client.hook(shardedJedis -> {
            shardedJedis.getAllShards().iterator().next().exists(keys);
        });

        //System.out.println(client.keysOps().exists(keys));
    }

    @Test
    public void testExists2() {
        System.out.println(client.keysOps().exists("s:w:r:p:152149819969"));
    }

    @Test
    public void testMget() {
        String prefix = "test2:str:";
        int n = 100;
        String[] keys1 = IntStream.range(0, n).filter(i -> i % 2 == 0).mapToObj(i -> prefix + i).toArray(String[]::new);

        client.executePipelined(
            (sp, elem) -> sp.setex(elem, 86400, "1"), 
            keys1
        );

        String[] keys2 = IntStream.range(0, n).mapToObj(i -> prefix + i).toArray(String[]::new);
        System.out.println(client.valueOps().mget(keys2));
    }

    @Test
    public void testGetObj() {
        String redisKeyPrefix = "waybill", newbill = "abcd1234", packageNo = "12333333333";
        client.valueOps().setObject(redisKeyPrefix + newbill, packageNo, 86400);
        System.out.println(client.valueOps().getObject(redisKeyPrefix + newbill, String.class));
    }

    @Test
    public void testMgetObj() {
        String prefix = "test5:str:";
        int n = 100;
        String[] keys = new String[n];
        for (int i = 0; i < n; i++) {
            String key = keys[i] = prefix + i;
            client.valueOps().set(key, RandomStringUtils.randomAlphanumeric(10), 86400);
        }

        System.out.println(client.valueOps().mgetObject(String.class, keys));
    }

    @Test
    public void testMgetObj2() {
        String prefix = "test5:str:";
        int n = 100;
        String[] keys = new String[n];
        for (int i = 0; i < n; i++) {
            String key = keys[i] = prefix + i;
            client.valueOps().set(key, RandomStringUtils.randomAlphanumeric(10), 86400);
        }

        System.out.println(client.valueOps().mget(keys));
    }

    @Test
    public void testsetbytes() {
        System.out.println("".getBytes().length);
        System.out.println(ArrayUtils.EMPTY_BYTE_ARRAY.length);
        byte[] key = "xxx".getBytes();
        client.valueOps().set(key, "".getBytes(), 9999);
        System.out.println(client.valueOps().get(key).length);
    }

    @Test 
    public void testScan() {
        client.hook(shardedJedis -> {
            for (Jedis jedis : shardedJedis.getAllShards()) {
                scan(jedis, 10, "o:w:r:p:*");
            }
        });
    }

    @Test
    public void getAndDel() {
        String key = "test123";
        client.valueOps().set(key, "aaaaaaaaaa");
        System.out.println(client.valueOps().get(key));
        System.out.println(client.valueOps().get(key));
        System.out.println(client.valueOps().getAndDel((key)));
        System.out.println(client.valueOps().get(key));
    }

    private void scan(Jedis jedis, int pageSize, String keyWildcard) {
        ScanParams scanParams = new ScanParams().count(pageSize).match(keyWildcard); // 设置每次scan个数
        String cursor = ScanParams.SCAN_POINTER_START;
        do {
            ScanResult<String> result = jedis.scan(cursor, scanParams);
            for (String key : result.getResult()) {
                Triple<String, String, Long> res = getAsString(jedis, key.getBytes());
                System.out.println(MessageFormat.format(
                    "key: {0}, type: {1}, value: {2}, ttl: {3}", 
                    key, res.getLeft(), res.getMiddle(), String.valueOf(res.getRight())
                ));
            }
            cursor = result.getStringCursor();
        } while (!ScanParams.SCAN_POINTER_START.equals(cursor)); // cursor is "0" -> scan end
    }

    /**
     * TTL：INFINITY=-1；EXPIRED=-2；
     * 
     * @see org.springframework.data.redis.connection.DataType
     * 
     * @param jedis
     * @param key
     * @return
     */
    private Triple<String, String, Long> getAsString(Jedis jedis, byte[] key) {
        String type = null, value = null;
        long ttl;
        try {
            type = jedis.type(key);
            if ("none".equals(type)) {
                value = "[NOT EXISTS]";
                ttl = -2; // 
            } else if ("string".equals(type)) {
                byte[] bytes = jedis.get(key);
                value = bytes != null ? new String(bytes) : null;
                ttl = jedis.ttl(key);
            } else {
                value = "[NOT STRING TYPE: " + type + "]";
                ttl = jedis.ttl(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
            type = Optional.ofNullable(type).orElse("ERROR");
            value = Optional.ofNullable(value).orElse("[ERROR: " + e.getMessage() + "]");
            ttl = -9; // mark ERROR
        }
        return Triple.of(type, value, ttl);
    }

    private static JedisPoolConfig buildDefaultPool() {
        JedisPoolConfig poolCfg = new JedisPoolConfig();
        poolCfg.setMaxTotal(20);
        poolCfg.setMaxIdle(5);
        poolCfg.setMinIdle(1);
        poolCfg.setMaxWaitMillis(15000);
        poolCfg.setTestOnBorrow(false);
        poolCfg.setTestOnReturn(false);
        poolCfg.setTestWhileIdle(false);
        poolCfg.setNumTestsPerEvictionRun(-1);
        poolCfg.setTimeBetweenEvictionRunsMillis(-1);
        poolCfg.setMinEvictableIdleTimeMillis(300000);
        return poolCfg;
    }
    
}
