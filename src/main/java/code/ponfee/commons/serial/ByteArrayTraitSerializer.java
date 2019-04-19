package code.ponfee.commons.serial;

import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * 
 * Object toString Serializer
 * 
 * @author Ponfee
 */
public class ByteArrayTraitSerializer extends Serializer {

    public static abstract class ByteArrayTrait {

        public ByteArrayTrait(byte[] array) {}

        protected abstract byte[] toByteArray();

        /*public static ByteArrayTrait fromByteArray(byte[] arg) {
            throw new UnimplementedException();
        }*/
    }

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        if (obj instanceof ByteArrayTrait) {
            byte[] bytes = ((ByteArrayTrait) obj).toByteArray();
            return compress ? GzipProcessor.compress(bytes) : bytes;
        }

        throw new SerializationException("object must be ByteArrayTrait type, but it's "
                                    + ClassUtils.getClassName(obj.getClass()) + " type");
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        if (ByteArrayTrait.class.isAssignableFrom(clazz)) {
            if (compress) {
                bytes = GzipProcessor.decompress(bytes);
            }
            try {
                //return clazz.getDeclaredMethod("fromByteArray", byte[].class).invoke(null, bytes);
                return (T) clazz.getConstructor(byte[].class).newInstance(bytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        throw new SerializationException("clazz must be ByteArrayTrait.class, but it's "
                                        + ClassUtils.getClassName(clazz) + ".class");
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
