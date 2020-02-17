package code.ponfee.commons.jedis.spring;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Spring redis cache template(template method pattern)
 * 
 * @author Ponfee
 */
public class RedisCacheTemplate {

    private final RedisTemplate<String, Object> redis;

    public RedisCacheTemplate(@Nonnull RedisTemplate<String, Object> redis) {
        this.redis = redis;
    }

    public <T> T execute(String redisKey, Supplier<T> supplier) {
        return execute(redisKey, 24, TimeUnit.HOURS, supplier);
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(String redisKey, long timeout, TimeUnit unit, Supplier<T> supplier) {
        BoundValueOperations<String, T> ops = (BoundValueOperations<String, T>) redis.boundValueOps(redisKey);

        T value;

        if ((value = ops.get()) != null) {
            return value;
        }

        if ((value = supplier.get()) != null) {
            ops.set(value, timeout, unit);
        }

        return value;
    }

    public RedisTemplate<String, Object> getRedis() {
        return redis;
    }

}
