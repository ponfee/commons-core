/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.serial;

import cn.ponfee.commons.io.GzipProcessor;
import cn.ponfee.commons.reflect.ClassUtils;

/**
 * 字段串序例化
 * 
 * @author Ponfee
 */
public class ByteArraySerializer extends Serializer {

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        if (!(obj instanceof byte[])) {
            throw new SerializationException(
                "Object must be byte[].class type, but it's " + ClassUtils.getClassName(obj.getClass()) + " type."
            );
        }

        byte[] bytes = (byte[]) obj;
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        if (clazz != byte[].class) {
            throw new SerializationException(
                "clazz must be byte[].class, but it's " + ClassUtils.getClassName(clazz) + ".class"
            );
        }

        return (T) (compress ? GzipProcessor.decompress(bytes) : bytes);
    }

    // -------------------------------------------------------------------
    public byte[] serialize(byte[] bytes, boolean compress) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    public byte[] deserialize(byte[] bytes, boolean compress) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }
        return compress ? GzipProcessor.decompress(bytes) : bytes;
    }

}
