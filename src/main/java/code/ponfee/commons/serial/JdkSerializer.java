package code.ponfee.commons.serial;

import code.ponfee.commons.io.Closeables;
import code.ponfee.commons.io.ExtendedGZIPOutputStream;
import code.ponfee.commons.reflect.ClassUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * java序例化
 * @author fupf
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
            Closeables.closeLog(oos, "close ObjectOutputStream exception");
            Closeables.closeLog(gzout, "close GZIPOutputStream exception");
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
            if (!clazz.isInstance(t)) {
                throw new ClassCastException(ClassUtils.getClassName(t.getClass())
                         + " can't be cast to " + ClassUtils.getClassName(clazz));
            }
            return t;
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException(e);
        } finally {
            Closeables.closeLog(ois, "close ObjectInputStream exception");
            Closeables.closeLog(gzin, "close GZIPInputStream exception");
        }
    }

}
