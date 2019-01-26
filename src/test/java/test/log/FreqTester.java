package test.log;

import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.limit.current.RedisCurrentLimiter;
import code.ponfee.commons.serial.JdkSerializer;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPoolConfig;

public class FreqTester {

    private JedisClient jedisClient;

    @Before
    public void setup() {
        JedisPoolConfig poolCfg = new JedisPoolConfig();
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
        jedisClient = new JedisClient(poolCfg, "127.0.0.1:6379;", new JdkSerializer());
    }

    // zcount "cir:bre:abc" 0 99999999999999
    @Test
    public void test1() throws InterruptedException {
        RedisCurrentLimiter f = new RedisCurrentLimiter(jedisClient, 1, 5);

        String[] s = {"a","b","c","d","e","f","g","h"};
        for (String a : s) {
            f.setRequestThreshold(a, 7000000);
        }
        int len = s.length;
        AtomicInteger count = new AtomicInteger(0);
        MultithreadExecutor.execAsync(100, ()->{
            if (!f.checkpoint(s[count.getAndIncrement()%len])) {
                System.err.println("reject req " + Thread.currentThread());
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 20);
    }
}
