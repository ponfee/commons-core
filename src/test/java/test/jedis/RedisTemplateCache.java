package test.jedis;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis cache
 * 
 * @author Ponfee
 */
public class RedisTemplateCache implements Cache {

    private static Logger logger = LoggerFactory.getLogger(RedisTemplateCache.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final String name;
    private final Long expireTime;

    public RedisTemplateCache(RedisTemplate<String, Object> redisTemplate,
                              String name, Long expireTime) {
        this.redisTemplate = redisTemplate;
        this.name = name;
        this.expireTime = expireTime;
    }

    public RedisTemplateCache(RedisTemplate<String, Object> redisTemplate) {
        this(redisTemplate, "", 86400L);
    }

    @Override
    public RedisTemplate<String, Object> getNativeCache() {
        return this.redisTemplate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ValueWrapper get(Object key) {
        if (key == null) {
            return null;
        }
        Object object = redisTemplate.opsForValue().get(formatKey(key));
        return (object != null ? new SimpleValueWrapper(object) : null);
    }

    @Override
    public void put(Object key, Object value) {
        if (key == null) {
            return;
        }
        final String keyf = formatKey(key);
        if (expireTime != null) {
            redisTemplate.opsForValue().set(keyf, value, this.expireTime, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(keyf, value);
        }
    }

    @Override
    public void evict(Object key) {
        if (key == null) {
            return;
        }
        redisTemplate.delete(formatKey(key));
    }

    @Override
    public void clear() {
        //Do nothing
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Class<T> clazz) {
        if (key == null) {
            return null;
        }
        return (T) redisTemplate.opsForValue().get(formatKey(key));
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        if (key == null) {
            return null;
        }
        final String keyf = formatKey(key);
        boolean success = redisTemplate.opsForValue().setIfAbsent(keyf, value);
        if (success) {
            return (value != null ? new SimpleValueWrapper(value) : null);
        } else {
            return get(key);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        if (key == null) {
            return null;
        }
        final String keyf = formatKey(key);

        Object value = redisTemplate.opsForValue().get(keyf);
        if (value != null) {
            return (T) value;
        } else {
            try {
                value = valueLoader.call();
                redisTemplate.opsForValue().set(keyf, value);
            } catch (Exception e) {
                logger.debug("value not find", e);
            }
            return (T) value;
        }
    }

    private String formatKey(Object key) {
        return this.name + ":" + key;
    }

}
