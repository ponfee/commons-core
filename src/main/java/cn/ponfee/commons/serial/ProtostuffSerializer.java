/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.serial;

import cn.ponfee.commons.io.GzipProcessor;
import cn.ponfee.commons.util.ObjectUtils;
import cn.ponfee.commons.util.SynchronizedCaches;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * Protostuff Serializer
 * 
 * @author Ponfee
 */
public class ProtostuffSerializer extends Serializer {

    private static final Map<Class<?>, Schema<?>> SCHEMA_CACHE = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    protected <T> byte[] serialize0(T obj, boolean compress) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            byte[] bytes = ProtostuffIOUtil.toByteArray(
                obj, getSchema((Class<T>) obj.getClass()), buffer
            );
            return compress ? GzipProcessor.compress(bytes) : bytes;
        } finally {
            buffer.clear();
        }
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> type, boolean compress) {
        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }

        T message = ObjectUtils.newInstance(type);
        ProtostuffIOUtil.mergeFrom(bytes, message, getSchema(type));
        return message;
    }

    // ------------------------------------------------------------------------private methods
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> type) {
        return (Schema<T>) SynchronizedCaches.get(type, SCHEMA_CACHE, RuntimeSchema::createFrom);
    }

}
