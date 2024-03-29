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

import java.nio.charset.Charset;

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

        return serialize((String) obj, compress);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        if (clazz != String.class) {
            throw new SerializationException(
                "clazz must be java.lang.String.class, but it's " + ClassUtils.getClassName(clazz) + ".class"
            );
        }

        return (T) deserialize(bytes, compress);
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
