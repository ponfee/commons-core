package code.ponfee.commons.serial;

import code.ponfee.commons.collect.ByteArrayTrait;
import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * 
 * Byte array trait Serializer
 * 
 * @author Ponfee
 */
public class ByteArrayTraitSerializer extends Serializer {

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        if (!(obj instanceof ByteArrayTrait)) {
            throw new SerializationException(
                "object must be ByteArrayTrait type, but it's " + ClassUtils.getClassName(obj.getClass()) + " type"
            );
        }

        byte[] bytes = ((ByteArrayTrait) obj).toByteArray();
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        if (!ByteArrayTrait.class.isAssignableFrom(clazz)) {
            throw new SerializationException(
                "clazz must be ByteArrayTrait.class, but it's " + ClassUtils.getClassName(clazz) + ".class"
            );
        }

        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }
        try {
            //return clazz.getDeclaredMethod("fromByteArray", byte[].class).invoke(null, bytes);
            return clazz.getConstructor(byte[].class).newInstance(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------------
    /**
     * serialize the byte array of ByteArrayTrait
     * 
     * @param trait
     * @param compress
     * @return
     */
    public byte[] serialize(ByteArrayTrait trait, boolean compress) {
        if (trait == null) {
            return null;
        }

        byte[] bytes = trait.toByteArray();
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

}
