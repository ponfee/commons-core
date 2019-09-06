package test.jedis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import code.ponfee.commons.jedis.ShardedJedisSentinelPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.ShardedJedis;

public class JedisTest {

    public static void main(String[] args) {
        String key = "auth:wl:screen:key", wildcard = "auth*";
        //String key = "ddt_dim_city_division_code_M023YK", wildcard = "ddt_dim*";
        //String key = "ex:rt:trend:107", wildcard = "ext*";
        int pageSize = 10;
        ShardedJedis shardedJedis = null;
        ShardedJedisSentinelPool sentinelPool = shardedJedisSentinelPool();
        try {
            shardedJedis = sentinelPool.getResource();
            for (Jedis jedis : shardedJedis.getAllShards()) {
                System.out.println("GET-------------------"+jedis.get(key));
                scan(jedis, pageSize, wildcard);
            }
            //shardedJedis.close();
            sentinelPool.returnResource(shardedJedis);
        } catch (Exception e) {
            sentinelPool.returnBrokenResource(shardedJedis);
        }
        
        
        sentinelPool.destroy();
    }

    private static void scan(Jedis jedis, int pageSize, String keyWildcard) {
        ScanParams scanParams = new ScanParams().count(pageSize).match(keyWildcard);
        ScanResult<String> result = jedis.scan(ScanParams.SCAN_POINTER_START, scanParams);
        for (String key : result.getResult()) {
            String type = jedis.type(key);
            System.out.println("SCAN-------------------"+key+ type+ jedis.ttl(key));
        }
        //ScanParams.SCAN_POINTER_START.equals(result.getStringCursor()) -> scan end
    }


    public static ShardedJedisSentinelPool shardedJedisSentinelPool() {
        List<String> masters0 = Arrays.asList("DDT_CORE_CNSZ22_REDIS_CACHE".split(","));
        Set<String> nodes0 = new HashSet<>(Arrays.asList("10.202.40.105:8001 10.202.40.105:8002 10.202.40.107:8001".split(" ")));
        int timeout = 30000;
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(20);
        cfg.setMaxIdle(5);
        cfg.setMinIdle(0);
        cfg.setMaxWaitMillis(timeout);
        cfg.setTestOnBorrow(false);
        cfg.setTestOnReturn(false);
        cfg.setTestWhileIdle(false);
        cfg.setNumTestsPerEvictionRun(-1);
        cfg.setMinEvictableIdleTimeMillis(timeout);
        cfg.setTimeBetweenEvictionRunsMillis(timeout);

        return new ShardedJedisSentinelPool(cfg, masters0, nodes0, "admin.123", timeout);
    }
}
