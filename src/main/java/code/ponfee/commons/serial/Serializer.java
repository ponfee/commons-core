package code.ponfee.commons.serial;

/**
 * 序例化抽象类
 * 
 * Method template pattern
 * 
 * @author fupf
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
    protected abstract byte[] serialize0(Object obj, boolean compress);

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
        return serialize0(obj, compress);
    }

    public final byte[] serialize(Object obj) {
        return serialize(obj, false);
    }

    public final <T> T deserialize(byte[] bytes, Class<T> clazz, boolean compress) {
        if (bytes == null) {
            return null;
        }
        return deserialize0(bytes, clazz, compress);
    }

    public final <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return this.deserialize(bytes, clazz, false);
    }

}
