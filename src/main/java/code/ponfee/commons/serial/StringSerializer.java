package code.ponfee.commons.serial;

import java.nio.charset.Charset;

import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * 字段串序例化
 * 
 * @author Ponfee
 */
public class StringSerializer extends Serializer {

    private final Charset charset;

    public StringSerializer() {
        this(Charset.defaultCharset());
    }

    public StringSerializer(String charset) {
        this(Charset.forName(charset));
    }

    public StringSerializer(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset cannot be null");
        }
        this.charset = charset;
    }

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        if (!(obj instanceof String)) {
            throw new SerializationException(
                "object must be java.lang.String type, but it's " + ClassUtils.getClassName(obj.getClass()) + " type"
            );
        }

        byte[] bytes = ((String) obj).getBytes(charset);
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        if (clazz != String.class) {
            throw new SerializationException(
                "clazz must be java.lang.String.class, but it's " + ClassUtils.getClassName(clazz) + ".class"
            );
        }

        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }
        return (T) new String(bytes, charset);
    }

    // ----------------------------------------------------------------------
    /**
     * serialize the byte array of string
     * 
     * @param str
     * @param compress
     * @return
     */
    public byte[] serialize(String str, boolean compress) {
        if (str == null) {
            return null;
        }

        byte[] bytes = str.getBytes(charset);
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    /**
     * deserialize the byte array to string
     * 
     * @param bytes
     * @param compress
     * @return
     */
    public String deserialize(byte[] bytes, boolean compress) {
        if (bytes == null) {
            return null;
        }
        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }
        return new String(bytes, charset);
    }

}
