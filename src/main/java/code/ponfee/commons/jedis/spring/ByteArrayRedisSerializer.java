package code.ponfee.commons.jedis.spring;

import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Byte array redis serialize
 * 
 * @author Ponfee
 */
public class ByteArrayRedisSerializer implements RedisSerializer<byte[]> {

    @Override
    public byte[] serialize(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] deserialize(byte[] bytes) {
        return bytes;
    }

}
