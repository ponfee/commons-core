package code.ponfee.commons.serial;

import code.ponfee.commons.io.GzipProcessor;

/**
 * Protostuff Serializer
 * 
 * @author fupf
 */
public class ProtostuffSerializer extends Serializer {

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        byte[] bytes = ProtostuffUtils.serialize(obj);
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }
        return ProtostuffUtils.deserialize(bytes, clazz);
    }

}
