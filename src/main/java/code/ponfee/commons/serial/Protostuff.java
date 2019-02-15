package code.ponfee.commons.serial;

import java.io.Serializable;
import java.lang.ref.SoftReference;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.pool2.impl.PooledSoftReference;

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

    private final ProtostuffWrapper wrapper;
    private final Schema<ProtostuffWrapper> schema;
    private final LinkedBuffer buffer;

    public Protostuff() {
        this.wrapper = new ProtostuffWrapper();
        this.schema = RuntimeSchema.getSchema(ProtostuffWrapper.class);
        this.buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    }

    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }

        wrapper.data = obj;
        byte[] bytes = ProtostuffIOUtil.toByteArray(wrapper, schema, buffer);
        buffer.clear();
        return bytes;
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return null;
        }
        ProtostuffWrapper wrapper = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, wrapper, schema);
        return (T) wrapper.data;
    }

    private static final class ProtostuffWrapper implements Serializable {
        private static final long serialVersionUID = -7170699878063967720L;
        private Object data;
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

}
