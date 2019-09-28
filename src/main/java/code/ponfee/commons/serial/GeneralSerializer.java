package code.ponfee.commons.serial;

import code.ponfee.commons.io.GzipProcessor;

/**
 * 
 * General Serializer
 * 
 * @author Ponfee
 */
public class GeneralSerializer extends Serializer {

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        byte[] bytes = Serializations.serialize(obj);
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> type, boolean compress) {
        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }
        return Serializations.deserialize(bytes, type);
    }

}
