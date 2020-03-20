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
@SuppressWarnings({"rawtypes", "unchecked"})
public class RedisCacheTemplate {

    private final  RedisTemplate redis;

    public <K, V> RedisCacheTemplate(@Nonnull RedisTemplate<K, V> redis) {
        this.redis = redis;
    }

    public <K, V> V execute(K redisKey, Supplier<V> supplier) {
        return execute(redisKey, 12, TimeUnit.HOURS, supplier);
    }

    public <K, V> V execute(K redisKey, long timeout, TimeUnit unit, Supplier<V> supplier) {
        BoundValueOperations<K, V> ops = redis.boundValueOps(redisKey);

        V value;

        if ((value = ops.get()) != null) {
            return value;
        }

        if ((value = supplier.get()) != null) {
            ops.set(value, timeout, unit);
        }

        return value;
    }

    public <K, V> RedisTemplate<K, V> getRedis() {
        return redis;
    }

}
