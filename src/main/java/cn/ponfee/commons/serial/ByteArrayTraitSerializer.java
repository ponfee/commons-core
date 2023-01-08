/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.serial;

import cn.ponfee.commons.collect.ByteArrayTrait;
import cn.ponfee.commons.io.GzipProcessor;
import cn.ponfee.commons.reflect.ClassUtils;

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

        return serialize((ByteArrayTrait) obj, compress);
    }

    @SuppressWarnings("unchecked")
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
        return (T) ofBytes(bytes, (Class<? extends ByteArrayTrait>) clazz);
    }

    public static <T extends ByteArrayTrait> T ofBytes(byte[] bytes, Class<T> type) {
        //return clazz.getDeclaredMethod("fromByteArray", byte[].class).invoke(null, bytes);
        return ClassUtils.newInstance(type, new Class<?>[]{byte[].class}, new Object[]{bytes});
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
