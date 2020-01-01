package code.ponfee.commons.serial;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.annotation.Nonnull;

import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.util.ObjectUtils;

/**
 * Object toString Serializer
 * 
 * @author Ponfee
 */
public class ToStringSerializer extends Serializer {

    private final Charset charset;

    public ToStringSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public ToStringSerializer(@Nonnull Charset charset) {
        this.charset = Objects.requireNonNull(charset);
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

        Constructor<T> constructor = ClassUtils.getConstructor(type, byte[].class);
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

}
