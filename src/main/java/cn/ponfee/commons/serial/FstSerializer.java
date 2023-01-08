/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.serial;

import cn.ponfee.commons.io.GzipProcessor;
import org.apache.commons.lang3.ClassUtils;
import org.nustaq.serialization.FSTConfiguration;

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
        if (obj != null && !ClassUtils.isAssignable(obj.getClass(), clazz)) {
            throw new ClassCastException(
                ClassUtils.getName(obj.getClass()) + " can't be cast to " + ClassUtils.getName(clazz)
            );
        }
        return obj;
    }

}
