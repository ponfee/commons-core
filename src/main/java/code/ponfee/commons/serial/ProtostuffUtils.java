package code.ponfee.commons.serial;

import java.util.HashMap;
import java.util.Map;

import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Protostuff utility
 * 
 * @author Ponfee
 */
public final class ProtostuffUtils {

    private static final Map<Class<?>, Schema<?>> SCHEMA_CACHE = new HashMap<>();
    private static final Objenesis OBJENESIS = new ObjenesisStd(true);

    private ProtostuffUtils() {}

    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Class<T> type = (Class<T>) obj.getClass();
        try {
            return ProtostuffIOUtil.toByteArray(obj, getSchema(type), buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            buffer.clear();
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> type) {
        try {
            T message = OBJENESIS.newInstance(type);
            ProtostuffIOUtil.mergeFrom(data, message, getSchema(type));
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> type) {
        Schema<T> schema = (Schema<T>) SCHEMA_CACHE.get(type);
        if (schema == null) {
            synchronized (SCHEMA_CACHE) {
                schema = (Schema<T>) SCHEMA_CACHE.get(type);
                if (schema == null) {
                    SCHEMA_CACHE.put(type, schema = RuntimeSchema.createFrom(type));
                }
            }
        }
        return schema;
    }

}
