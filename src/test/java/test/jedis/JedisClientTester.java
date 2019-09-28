package test.jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

import bean.TestBean;
import code.ponfee.commons.collect.ByteArrayWrapper;
import code.ponfee.commons.io.Files;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.ShardedJedisSentinelPool;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.ObjectUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jedis-cfg.xml" })
public class JedisClientTester {

    @Resource
    private JedisClient jedisClient;

    @Before
    public void setup() {
        /*JedisPoolConfig poolCfg = new JedisPoolConfig();
        poolCfg.setMaxTotal(100);
        poolCfg.setMaxIdle(200);
        poolCfg.setMinIdle(100);
        poolCfg.setMaxWaitMillis(1000);
        poolCfg.setTestOnBorrow(false);
        poolCfg.setTestOnReturn(false);
        poolCfg.setTestWhileIdle(false);
        poolCfg.setNumTestsPerEvictionRun(-1);
        poolCfg.setMinEvictableIdleTimeMillis(60000);
        poolCfg.setTimeBetweenEvictionRunsMillis(30000);
        //jedisClient = new JedisClient(poolCfg, "local1:127.0.0.1:6379", new KryoSerializer());
        //jedisClient = new JedisClient(poolCfg, "127.0.0.1:6379;127.0.0.1:6380;127.0.0.1:6381");
        jedisClient = new JedisClient(poolCfg, "127.0.0.1:6379;127.0.0.1:6380;127.0.0.1:6381", new KryoSerializer());*/
    }

    @After
    public void teardown() {}

    @Test
    public void test() {
        TestBean bean = new TestBean(123, 4324L, "5435fds");
        System.out.println(jedisClient.valueOps().setObject("abcdefg".getBytes(), bean));
        System.out.println(jedisClient.valueOps().getObject("abcdefg".getBytes(), TestBean.class));
    }
    
    @Test
    public void testScript1() {
        String key = "abc:xxxxx";
        long ttl = 60;
        String script = "local val=redis.call('INCR', KEYS[1]); if val==1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end; return val;";
        System.out.println((Long)jedisClient.scriptOps().eval(script, Arrays.asList(key), Arrays.asList(String.valueOf(ttl))));
        System.out.println(jedisClient.keysOps().ttl(key));
    }
    
    @Test
    public void testScript2() {
        String key = "abc:xxxxx";
        long ttl = 60;
        String script = "local val=redis.call('INCR', KEYS[1]); if val==1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end; return val;";
        System.out.println((Long)jedisClient.scriptOps().eval(key, script, Arrays.asList(String.valueOf(ttl))));
        System.out.println(jedisClient.keysOps().ttl(key));
    }
    
    @Test
    public void testScript3() {
        String key = "abc:xxxxx";
        int ttl = 30;
        System.out.println(jedisClient.valueOps().incrByEX(key, 1, ttl));
        System.out.println(jedisClient.keysOps().ttl(key));
    }

    @Test
    public void test2() {
        System.out.println(jedisClient.valueOps().set("123456".getBytes(), "11".getBytes(), 99999));
        System.out.println(jedisClient.valueOps().incrBy("123456"));
        
        
        System.out.println(jedisClient.valueOps().set("654321".getBytes(), Bytes.toBytes(789), 99999));
        System.out.println(jedisClient.valueOps().incrBy("654321"));
        
        
        System.out.println(jedisClient.valueOps().set("1111111".getBytes(), Bytes.toBytes(52), 99999));
        System.out.println(jedisClient.valueOps().incrBy("1111111"));
    }

    @Test
    public void testSet() {
        int n = 1;
        TestBean[] breans = new TestBean[n];
        for (int i = 0; i < n; i++) {
            breans[i] = new TestBean(i, new Random().nextLong(), UUID.randomUUID().toString());
        }
        long start = System.currentTimeMillis();
        System.out.println(jedisClient.setOps().sadd("abcdeaf".getBytes(), true, breans));
        //System.out.println(jedisClient.setOps().smembers("abcdeaf".getBytes(), TestBean.class));
        //System.out.println(jedisClient.setOps().srandmember("abcdeaf".getBytes(), TestBean.class, 2));
        System.out.println(System.currentTimeMillis() - start);
    }

    // ----------------------------------------------------------------------------batch start
    @Test
    public void testMget1() {
        int n = 2000;
        String[] keys =new String[n];
        for (int i = 0; i < n; i++) {
            String key = "test_mget_str_" + i;
            jedisClient.valueOps().setObject(key, key);
            keys[i] = key;
        }
        long start = System.currentTimeMillis();
        System.out.println(jedisClient.valueOps().mget(keys).size());
        System.out.println(System.currentTimeMillis() - start);
    }
    
    @Test
    public void testMget2() {
        int n = 2;
        byte[][] keys =new byte[n][];
        for (int i = 0; i < n; i++) {
            byte[] key = ("test_mget_str_" + i).getBytes();
            jedisClient.valueOps().setObject(key, key);
            keys[i] = key;
        }
        long start = System.currentTimeMillis();
        System.out.println(jedisClient.valueOps().mget(keys).size());
        System.out.println(System.currentTimeMillis() - start);
    }
 
    @Test
    public void testMget3() {
        int n = 2;
        List<byte[]> keys = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            byte[] key = ("test_mget_" + i).getBytes();
            keys.add(key);
            jedisClient.valueOps().setObject(key, new TestBean(i, new Random().nextLong(), UUID.randomUUID().toString()));
        }
        long start = System.currentTimeMillis();
        System.out.println(jedisClient.valueOps().mgetObject(TestBean.class, keys.toArray(new byte[keys.size()][])));
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void testMgetByte() {
        int n = 2000;
        byte[][] keys = new byte[n][];
        for (int i = 0; i < n; i++) {
            byte[] key = ("test_mget_" + i).getBytes();
            //byte[] bytes = new byte[new Random().nextInt(9997)];
            //byte[] bytes = new byte[9997];
            //new Random().nextBytes(bytes);
            byte[] bytes = Files.toByteArray(MavenProjects.getTestJavaFile(this.getClass()));
            keys[i] = key;
            jedisClient.valueOps().set(key, bytes, true, 1000);
        }
        long start = System.currentTimeMillis();
        Map<ByteArrayWrapper, byte[]> map = jedisClient.valueOps().mget(true, keys);
        System.out.println(map.size() + " mget cost: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        map = new HashMap<>();
        byte[] v;
        for (byte[] key : keys) {
            v = jedisClient.valueOps().get(key, true);
            if (v != null) map.put(ByteArrayWrapper.of(key), v);
        }
        System.out.println(map.size() + " get cost: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void testMgetObj() {
        int n = 2000;
        byte[][] keys = new byte[n][];
        for (int i = 0; i < n; i++) {
            byte[] key = ("test_mget_" + i).getBytes();
            keys[i] = key;
            jedisClient.valueOps().setObject(key, new TestBean(i, new Random().nextLong(), UUID.randomUUID().toString()), true);
        }

        long start = System.currentTimeMillis();
        jedisClient.valueOps().mgetObject(TestBean.class, true, keys);
        System.out.println("mgetObject cost: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        Map<byte[], TestBean> map = new HashMap<>();
        TestBean b;
        for (byte[] key : keys) {
            b = jedisClient.valueOps().getObject(key, TestBean.class, true);
            if (b != null) map.put(key, b);
        }
        System.out.println("getObject cost: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void testGetWithWildcard() {
        Set<String> list = jedisClient.valueOps().getWithWildcard("test_mget_str_*");
        System.out.println(list.size());
    }

    @Test
    public void testKeys() {
        Set<String> list = jedisClient.keysOps().keys("test_mget_*");
        System.out.println(list.size());
    }

    @Test
    public void testDels() {
        int n = 3;
        String[] keys = new String[n];
        for (int i = 0; i < n; i++) {
            keys[i] = ObjectUtils.uuid32();
            jedisClient.valueOps().set(keys[i], keys[i]);
        }
        System.out.println("mget: "+jedisClient.valueOps().mget(keys).size());
        System.out.println("dels: "+jedisClient.keysOps().del(keys));
        System.out.println("mget: "+jedisClient.valueOps().mget(keys).size());
    }

    @Test
    public void testDelWithWildcard() {
        System.out.println(jedisClient.keysOps().keys("test_mget_*").size());
        jedisClient.keysOps().delWithWildcard("test_mget_*");
        System.out.println(jedisClient.keysOps().keys("test_mget_*").size());
    }
    // ----------------------------------------------------------------------------batch end

    @Test
    public void testString() {
        String key = "312412";
        System.out.println(jedisClient.valueOps().set(key, "fdsafdsaf"));
        System.out.println(jedisClient.valueOps().get(key));
    }

    @Test
    public void testHset() {
        System.out.println(jedisClient.hashOps().hset("21312", "abcd", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    public void testLua() throws IOException {
        String lua = IOUtils.toString(JedisTester.class.getResourceAsStream("/redis-script-node.lua"), "UTF-8");
        String sha1 = jedisClient.scriptOps().scriptLoad(lua);
        System.out.println(sha1);
        System.out.println(jedisClient.scriptOps().evalsha(lua, sha1, Lists.newArrayList("myname", "1"), Lists.newArrayList()));
    }

    @Test
    public void testLua2() {
        String lua = "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2],'bar'}";
        String sha1 = jedisClient.scriptOps().scriptLoad(lua);
        System.out.println(sha1);
        System.out.println(jedisClient.scriptOps().evalsha(lua, sha1, Lists.newArrayList("myname", "test"), Lists.newArrayList("a", "b")));
    }

    @Test
    public void testScript() {
        System.out.println(jedisClient.scriptOps().eval("return 10", Lists.newArrayList(), Lists.newArrayList()));
        System.out.println(jedisClient.scriptOps().eval("return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}", Lists.newArrayList("myname", "test"), Lists.newArrayList("a", "b")));
        System.out.println(jedisClient.scriptOps().eval("return redis.call('set',KEYS[1],'bar11')", Lists.newArrayList("myname"), Lists.newArrayList()));
        System.out.println((String)jedisClient.call(sj ->  sj.getShard("myname").get("myname"), null));
    }

    @Test
    public void testHook() {
        jedisClient.call(shardedJedis -> {
            System.out.println(shardedJedis.set("aaa", "111"));
            System.out.println(shardedJedis.get("aaa"));
            return 1;
        }, true);

        jedisClient.hook(shardedJedis -> {
            System.out.println(shardedJedis.set("aaa", "111"));
            System.out.println(shardedJedis.get("aaa"));
        });

        jedisClient.valueOps().get("abc");
    }

    @Test
    public void testPubsub() throws InterruptedException {
        Thread thread = new Thread(() -> {
            jedisClient.mqOps().subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    System.out.println("onMessage: " + channel + "--->" + message);
                }

                @Override
                public void onPMessage(String pattern, String channel, String message) {
                    System.out.println("onPMessage: " + pattern + "--->" + channel + "--->" + message);
                }

                @Override
                public void onPSubscribe(String pattern, int subscribedChannels) {
                    System.out.println("onPSubscribe: " + pattern + "--->" + subscribedChannels);
                }

                @Override
                public void onPUnsubscribe(String pattern, int subscribedChannels) {
                    System.out.println("onPUnsubscribe: " + pattern + "--->" + subscribedChannels);
                }

                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    System.out.println("onSubscribe: " + channel + "--->" + subscribedChannels);
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    System.out.println("onSubscribe: " + channel + "--->" + subscribedChannels);
                }
            }, "testChannel", "testChannel1");
        });
        thread.start();
        Thread.sleep(1000);
        System.out.println("====================");
        jedisClient.mqOps().publish("testChannel", "a");
        jedisClient.mqOps().publish("testChannel", "b");
        jedisClient.mqOps().publish("testChannel", "c");
        jedisClient.mqOps().publish("testChannel1", "1");
        jedisClient.mqOps().publish("testChannel1", "2");
        jedisClient.mqOps().publish("testChannel1", "3");
        Thread.sleep(5000);
    }

    @Test
    public void testIncr() {
        String key = "abcde";
        System.out.println( jedisClient.valueOps().incrBy(key));
        System.out.println(jedisClient.valueOps().getLong(key));
        System.out.println(jedisClient.valueOps().getLong(key));
        System.out.println( jedisClient.valueOps().incrBy(key));
        System.out.println(jedisClient.valueOps().getLong(key));
        System.out.println(jedisClient.valueOps().getLong(key));
    }

    @Test
    public void testScan() {
        List<String> masters = Arrays.asList("DDT_CORE_CNSZ22_REDIS_CACHE");
        Set<String> sentinels = new HashSet<>(Arrays.asList("10.202.40.105:8001", "10.202.40.105:8002", "10.202.40.107:8001"));
        String password = "admin.123";
        int timeout = 300000;
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(5);
        cfg.setMaxIdle(1);
        cfg.setMinIdle(0);
        cfg.setMaxWaitMillis(timeout);
        cfg.setTestOnBorrow(false);
        cfg.setTestOnReturn(false);
        cfg.setTestWhileIdle(false);
        cfg.setNumTestsPerEvictionRun(-1);
        cfg.setMinEvictableIdleTimeMillis(timeout);
        cfg.setTimeBetweenEvictionRunsMillis(timeout);

        ShardedJedisSentinelPool pool = new ShardedJedisSentinelPool(cfg, masters, sentinels, password, timeout);

        for (Jedis jedis : pool.getResource().getAllShards()) {
            scan(jedis);
            jedis.del("task:payment:scanTest");
        }
        pool.close();
    }

    private static void scan(Jedis jedis) {
        ScanParams scanParams = new ScanParams();
        scanParams.count(2000); // 设置每次scan个数
        scanParams.match("task:payment:*");
        String ok = "0", cursor = ok;
        try {
            do {
                ScanResult<String> result = jedis.scan(cursor, scanParams);
                cursor = result.getStringCursor();
                for (String key : result.getResult()) {
                    System.out.println(key + "\t" + jedis.ttl(key));
                }
            } while (!cursor.equals(ok));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
