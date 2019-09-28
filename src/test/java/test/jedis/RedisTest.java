package test.jedis;

import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.serial.KryoSerializer;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTest {
    private static JedisClient client;

    @BeforeClass
    public static void setUp() {
        client = new JedisClient(
            buildDefaultPool(), 
            "INC_BUPP_CORE_REDIS_C01", 
            "inc-bupp-core1.cachesit.sfdc.com.cn:8001 inc-bupp-core2.cachesit.sfdc.com.cn:8001 inc-bupp-core3.cachesit.sfdc.com.cn:8001", 
            "jpyuffv2msdtk3ep", 
            new KryoSerializer()
        );
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

        System.out.println(client.keysOps().exists(keys));
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

    @AfterClass
    public static void tearDown() {
        client.destroy();
    }

    public static JedisPoolConfig buildDefaultPool() {
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
