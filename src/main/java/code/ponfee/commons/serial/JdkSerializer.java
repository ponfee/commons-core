package code.ponfee.commons.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.ClassUtils;

import code.ponfee.commons.io.Closeables;
import code.ponfee.commons.io.ExtendedGZIPOutputStream;

/**
 * java序例化
 * 
 * @author Ponfee
 */
public class JdkSerializer extends Serializer {

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        GZIPOutputStream gzout = null;
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
            if (compress) {
                gzout = new ExtendedGZIPOutputStream(baos);
                oos = new ObjectOutputStream(gzout);
            } else {
                oos = new ObjectOutputStream(baos);
            }
            oos.writeObject(obj);
            oos.close();
            oos = null;
            if (gzout != null) {
                gzout.close();
                gzout = null;
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            // 先打开的后关闭，后打开的先关闭
            // 看依赖关系，如果流a依赖流b，应该先关闭流a，再关闭流b
            // 处理流a依赖节点流b，应该先关闭处理流a，再关闭节点流b
            Closeables.log(oos, "close ObjectOutputStream exception");
            Closeables.log(gzout, "close GZIPOutputStream exception");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        GZIPInputStream gzin = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            if (compress) {
                gzin = new GZIPInputStream(bais);
                ois = new ObjectInputStream(gzin);
            } else {
                ois = new ObjectInputStream(bais);
            }

            T t = (T) ois.readObject();
            if (t != null && !ClassUtils.isAssignable(t.getClass(), clazz)) {
                throw new ClassCastException(
                    ClassUtils.getName(t.getClass()) + " can't be cast to " + ClassUtils.getName(clazz)
                );
            }
            return t;
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException(e);
        } finally {
            Closeables.log(ois, "close ObjectInputStream exception");
            Closeables.log(gzin, "close GZIPInputStream exception");
        }
    }

}
