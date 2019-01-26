package test.jedis;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-redis.xml" })
public class SpringJedisTester {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Before
    public void setup() {}

    @After
    public void teardown() {}

    @Test
    public void test() {
        redisTemplate.opsForValue().set("abc", "aaa");
        System.out.println(redisTemplate.opsForValue().get("abc"));
    }

}
