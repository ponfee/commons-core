package code.ponfee.commons.jedis.spring;

import java.io.Serializable;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Protostuff redis serialize utility
 * 
 * @author Ponfee
 */
public class ProtostuffRedisSerializer<T> implements RedisSerializer<T> {

    public static final class ProtostuffWrapper implements Serializable, Cloneable {
        private static final long serialVersionUID = -7170699878063967720L;

        private Object value;

        public ProtostuffWrapper() {}

        public ProtostuffWrapper(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    private static final Schema<ProtostuffWrapper> SCHEMA =
        RuntimeSchema.getSchema(ProtostuffWrapper.class);

    public byte[] serialize(T object) throws SerializationException {
        if (object == null) {
            return null;
        }

        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtostuffIOUtil.toByteArray(new ProtostuffWrapper(object), SCHEMA, buffer);
        } finally {
            buffer.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ProtostuffWrapper wrapper = new ProtostuffWrapper();
        ProtostuffIOUtil.mergeFrom(bytes, wrapper, SCHEMA);
        return (T) wrapper.value;
    }

}
