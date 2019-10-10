package code.ponfee.commons.jedis.spring;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Jdk redis serialize utility
 * 
 * @param <T>
 * 
 * @author Ponfee
 */
public class JdkRedisSerializer<T> implements RedisSerializer<T> {

    @Override
    public byte[] serialize(T obj) throws SerializationException {
        return SerializationUtils.serialize((Serializable) obj);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        return SerializationUtils.deserialize(bytes);
    }
}
