package code.ponfee.commons.serial;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.util.ObjectUtils;

/**
 * Object toString Serializer
 * 
 * @author Ponfee
 */
public class ToStringSerializer extends Serializer {

    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new HashMap<>();

    private final Charset charset;

    public ToStringSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public ToStringSerializer(@Nonnull Charset charset) {
        Objects.requireNonNull(charset);
        this.charset = charset;
    }

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        byte[] bytes = obj.toString().getBytes(charset);
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> type, boolean compress) {
        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }

        Constructor<T> constructor = getByteArrayArgConstructor(type);
        if (constructor != null) {
            try {
                return constructor.newInstance(bytes);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            return ObjectUtils.convert(new String(bytes, charset), type);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getByteArrayArgConstructor(Class<T> type) {
        if (CONSTRUCTOR_CACHE.containsKey(type)) {
            return (Constructor<T>) CONSTRUCTOR_CACHE.get(type);
        }

        synchronized (CONSTRUCTOR_CACHE) {
            if (CONSTRUCTOR_CACHE.containsKey(type)) {
                return (Constructor<T>) CONSTRUCTOR_CACHE.get(type);
            }

            Constructor<T> constructor = null;
            try {
                constructor = type.getConstructor(byte[].class);
            } catch (NoSuchMethodException | SecurityException ignored) {
                // Not such a single byte[] argument constructor
            }
            CONSTRUCTOR_CACHE.put(type, constructor);
            return constructor;
        }
    }

}
