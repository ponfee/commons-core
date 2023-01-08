/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.serial;

import cn.ponfee.commons.io.Closeables;
import cn.ponfee.commons.io.ExtendedGZIPOutputStream;
import org.apache.commons.lang3.ClassUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * <pre>
 * JDK序例化
 *
 * 1、Default serialization process：仅实现了Serializable接口，则JDK会使用默认的序列化进程序列化和反序列化对象
 * Note：针对所有non-transient和non-static成员变量
 * {@code java.io.ObjectOutputStream#defaultWriteObject() }
 * {@code java.io.ObjectInputStream#defaultReadObject() }
 *
 * 2、Customizing the serialization process：不仅实现了Serializable接口还定义了两个方法，则JDK会使用这两个方法定制化的进行序列化和反序列化对象
 * Note: must private access modifier, Subclasses will be inherit this method
 * {@code private void readObject(ObjectInputStream input) }
 * {@code private void writeObject(ObjectOutputSteam out) }
 *
 * 3、java.io.Externalizable：该接口是继承于Serializable，也是自定义实现序列化和反序列化方式的一种方式
 * {@code void writeExternal(ObjectOutput out) }
 * {@code void readExternal(ObjectInput in) }
 * </pre>
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
