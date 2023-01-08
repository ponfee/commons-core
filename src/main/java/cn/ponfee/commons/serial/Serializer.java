/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.serial;

/**
 * 序例化抽象类
 * 
 * Method template pattern
 * 
 * @author Ponfee
 */
public abstract class Serializer {

    static final int BYTE_SIZE = 512;

    /**
     * 对象序例化为流数据
     * 
     * @param obj 对象
     * @param compress 是否要压缩：true是；false否；
     * @return 序例化后的流数据
     */
    protected abstract <T> byte[] serialize0(T obj, boolean compress);

    /**
     * 流数据反序例化为对象
     * 
     * @param bytes 流数据
     * @param compress 是否被压缩：true是；false否；
     * @return 反序例化后的对象
     */
    protected abstract <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress);

    // ----------------------------------------------------------------------------------
    public final byte[] serialize(Object obj, boolean compress) {
        if (obj == null) {
            return null;
        }
        return Serializer.this.serialize0(obj, compress);
    }

    public final byte[] serialize(Object obj) {
        return serialize(obj, false);
    }

    public final <T> T deserialize(byte[] bytes, Class<T> clazz, boolean compress) {
        if (bytes == null) {
            return null;
        }
        return Serializer.this.deserialize0(bytes, clazz, compress);
    }

    public final <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return this.deserialize(bytes, clazz, false);
    }

}
