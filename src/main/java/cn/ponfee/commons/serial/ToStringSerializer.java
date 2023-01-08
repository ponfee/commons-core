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
import cn.ponfee.commons.util.ObjectUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Object toString Serializer
 * 
 * @author Ponfee
 */
public class ToStringSerializer extends Serializer {

    private final Charset charset;

    public ToStringSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public ToStringSerializer(@Nonnull Charset charset) {
        this.charset = Objects.requireNonNull(charset);
    }

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        byte[] bytes = obj.toString().getBytes(charset);
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> type, boolean compress) {
        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }

        Constructor<T> constructor = ClassUtils.getConstructor(type, byte[].class);
        if (constructor != null) {
            try {
                return constructor.newInstance(bytes);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            return ObjectUtils.cast(new String(bytes, charset), type);
        }
    }

}
