package code.ponfee.commons.jedis.spring;

import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import code.ponfee.commons.serial.Protostuff;
import code.ponfee.commons.serial.Protostuff.ProtostuffPool;

/**
 * Protostuff redis serialize utility
 * 
 * @author Ponfee
 */

public class ProtostuffRedisSerializer<T> implements RedisSerializer<T> {

    private final ProtostuffPool protostuffPool = new ProtostuffPool(
        new GenericObjectPoolConfig(), new AbandonedConfig()
    );

    @Override
    public byte[] serialize(T obj) throws SerializationException {
        Protostuff protostuff = null;
        try {
            protostuff = this.protostuffPool.borrowObject();
            return protostuff.serialize(obj);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        } finally {
            if (protostuff != null) {
                this.protostuffPool.returnObject(protostuff);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        Protostuff protostuff = null;
        try {
            protostuff = this.protostuffPool.borrowObject();
            return (T) protostuff.deserialize(bytes);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        } finally {
            if (protostuff != null) {
                this.protostuffPool.returnObject(protostuff);
            }
        }
    }

}
