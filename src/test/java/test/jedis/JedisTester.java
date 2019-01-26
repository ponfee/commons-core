package test.jedis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

public class JedisTester {

    private Jedis jedis;

    @Before
    public void setup() {
        jedis = new Jedis("127.0.0.1", 6379);
        // jedis.auth("abcdefg"); // 密码验证-如果你没有设置redis密码可不验证即可使用相关命令
    }

    @After
    public void teardown() {
        jedis.close();
    }

    @Test
    public void testAppend() {
        jedis.set("redis", "myredis"); // 简单的key-value 存储

        // 在原有值得基础上添加,如若之前没有该key，则导入该key
        // 之前已经设定了redis对应"myredis",此句执行便会使redis对应"myredisyourredis"
        jedis.append("redis", "yourredis");
        jedis.append("content", "rabbit");
        System.out.println(jedis.get("redis"));
        System.out.println(jedis.get("content"));
    }

    @Test
    public void testMset() {
        // mset 是设置多个key-value值 参数（key1,value1,key2,value2,...,keyn,valuen）
        // mget 是获取多个key所对应的value值 参数（key1,key2,key3,...,keyn） 返回的是个list
        jedis.mset("name1", "yangw", "name2", "demon", "name3", "elena");
        System.out.println(jedis.mget("name1", "name2", "name3"));
    }

    @Test
    public void testList() {
        jedis.lpush("listDemo", "A");
        jedis.lpush("listDemo", "B");
        jedis.lpush("listDemo", "C");
        System.out.println(jedis.lrange("listDemo", 0, -1));
        System.out.println(jedis.lrange("listDemo", 0, 1));

        jedis.del("listDemo");
        System.out.println(jedis.lrange("listDemo", 0, -1));
    }

    @Test
    public void testMap() {
        Map<String, String> user = new HashMap<String, String>();
        user.put("name", "cd");
        user.put("password", "123456");

        jedis.hmset("user", user); // map存入redis
        System.out.println(String.format("len:%d", jedis.hlen("user"))); // mapkey个数
        System.out.println(String.format("keys: %s", jedis.hkeys("user"))); // map中的所有键值
        System.out.println(String.format("values: %s", jedis.hvals("user"))); // map中的所有value

        List<String> rsmap = jedis.hmget("user", "name", "password"); // 取出map中的name、password字段值
        System.out.println(rsmap);

        jedis.hdel("user", "password"); // 删除map中的某一个键值 password
        System.out.println(jedis.hmget("user", "name", "password"));
    }

    @Test
    public void testSet() {
        jedis.sadd("sname", "wobby");
        jedis.sadd("sname", "kings");
        jedis.sadd("sname", "demon");
        System.out.println(String.format("set num: %d", jedis.scard("sname"))); // 总数
        System.out.println(String.format("all members: %s", jedis.smembers("sname"))); // 所有的值
        System.out.println(String.format("is member: %B", jedis.sismember("sname", "wobby"))); // 判断是否存在
        System.out.println(String.format("rand member: %s", jedis.srandmember("sname"))); // 随机获取数据

        jedis.srem("sname", "demon"); // 删除一个对象
        System.out.println(String.format("all members: %s", jedis.smembers("sname")));
    }

    @Test
    public void testLua() {
        byte[] lua = read(JedisTester.class.getResourceAsStream("/redis-script-node.lua"));
        String sha = jedis.scriptLoad(new String(lua));
        System.out.println(sha);
    }

    public static byte[] read(InputStream in) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int count = -1;
            while ((count = in.read(data, 0, 1024)) != -1)
                baos.write(data, 0, count);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
