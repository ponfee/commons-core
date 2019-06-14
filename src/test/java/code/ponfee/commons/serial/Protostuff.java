package code.ponfee.commons.serial;

import java.lang.ref.SoftReference;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.pool2.impl.PooledSoftReference;

import code.ponfee.commons.jedis.spring.ProtostuffRedisSerializer.ProtostuffWrapper;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Non thread safe, should be use with pool
 * 
 * @see ProtostuffPool
 * 
 * @author Ponfee
 */
public class Protostuff {

    private static final Schema<ProtostuffWrapper> SCHEMA =
        RuntimeSchema.getSchema(ProtostuffWrapper.class);

    private final ProtostuffWrapper wrapper;
    private final LinkedBuffer buffer;

    public Protostuff() {
        this.wrapper = new ProtostuffWrapper();
        this.buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    }

    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }

        wrapper.setValue(obj);
        try {
            return ProtostuffIOUtil.toByteArray(wrapper, SCHEMA, buffer);
        } finally {
            buffer.clear();
            wrapper.setValue(null);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return null;
        }
        //ProtostuffWrapper wrapper = SCHEMA.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, wrapper, SCHEMA);
        try {
            return (T) wrapper.getValue();
        } finally {
            wrapper.setValue(null);
        }
    }

    public static class ProtostuffFactory extends BasePooledObjectFactory<Protostuff> {
        @Override
        public Protostuff create() {
            return new Protostuff();
        }

        @Override
        public PooledObject<Protostuff> wrap(Protostuff obj) {
            //return new DefaultPooledObject<>(obj);
            return new PooledSoftReference<>(new SoftReference<>(obj));
        }
    }

    public static class ProtostuffPool extends GenericObjectPool<Protostuff> {
        public ProtostuffPool() {
            super(new ProtostuffFactory());
        }

        public ProtostuffPool(GenericObjectPoolConfig config,
                              AbandonedConfig abandonedConfig) {
            super(new ProtostuffFactory(), config, abandonedConfig);
        }
    }

    public static class ProtostuffWithPool {
        private static final ProtostuffPool PROTOSTUFF_POOL = new ProtostuffPool(
            new GenericObjectPoolConfig(), new AbandonedConfig()
        );

        public <T> byte[] serialize(T obj) throws SerializationException {
            Protostuff protostuff = null;
            try {
                protostuff = PROTOSTUFF_POOL.borrowObject();
                return protostuff.serialize(obj);
            } catch (Exception e) {
                throw new SerializationException(e.getMessage(), e);
            } finally {
                if (protostuff != null) {
                    PROTOSTUFF_POOL.returnObject(protostuff);
                }
            }
        }

        public <T> T deserialize(byte[] bytes) throws SerializationException {
            Protostuff protostuff = null;
            try {
                protostuff = PROTOSTUFF_POOL.borrowObject();
                return protostuff.deserialize(bytes);
            } catch (Exception e) {
                throw new SerializationException(e.getMessage(), e);
            } finally {
                if (protostuff != null) {
                    PROTOSTUFF_POOL.returnObject(protostuff);
                }
            }
        }
    }

}
