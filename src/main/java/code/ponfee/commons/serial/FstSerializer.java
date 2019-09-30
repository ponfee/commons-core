package code.ponfee.commons.serial;

import org.nustaq.serialization.FSTConfiguration;

import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * Fst Serializer
 * 
 * @author Ponfee
 */
public class FstSerializer extends Serializer {

    // createDefaultConfiguration
    private static final ThreadLocal<FSTConfiguration> FST_CFG =
        ThreadLocal.withInitial(FSTConfiguration::createStructConfiguration);

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        byte[] bytes = FST_CFG.get().asByteArray(obj);
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }

        T obj = (T) FST_CFG.get().asObject(bytes);
        if (!clazz.isInstance(obj)) {
            throw new ClassCastException(ClassUtils.getClassName(obj.getClass())
                       + " can't be cast to " + ClassUtils.getClassName(clazz));
        }
        return obj;
    }

}
