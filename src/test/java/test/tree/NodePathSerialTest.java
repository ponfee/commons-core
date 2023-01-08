package test.tree;

import cn.ponfee.commons.collect.ImmutableArrayList;
import cn.ponfee.commons.serial.JdkSerializer;
import cn.ponfee.commons.serial.KryoSerializer;
import cn.ponfee.commons.tree.NodePath;
import com.google.common.collect.ImmutableList;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Ponfee
 */
public class NodePathSerialTest {

    /**
     * add method UnsupportedOperationException
     */
    @Test @Ignore
    public void test1() {
        byte[] serialized1 = KryoSerializer.INSTANCE.serialize(new ImmutableArrayList<>(new Integer[]{1, 2, 3, 4}));
        System.out.println(KryoSerializer.INSTANCE.deserialize(serialized1, ImmutableArrayList.class));

        byte[] serialized2 = KryoSerializer.INSTANCE.serialize(new NodePath<>(new Integer[]{1, 2, 3, 4}));
        System.out.println(KryoSerializer.INSTANCE.deserialize(serialized2, NodePath.class));
    }

    @Test
    public void test2() {
        JdkSerializer jdkSerializer = new JdkSerializer();
        byte[] serialized1 = jdkSerializer.serialize(new ImmutableArrayList<>(new Integer[]{1, 2, 3, 4}));
        System.out.println(jdkSerializer.deserialize(serialized1, ImmutableArrayList.class).toArray().getClass());

        byte[] serialized2 = jdkSerializer.serialize(new NodePath<>(new Integer[]{1, 2, 3, 4}));
        System.out.println(jdkSerializer.deserialize(serialized2, NodePath.class).toArray().getClass());
    }

    @Test
    public void test3() {
        JdkSerializer jdkSerializer = new JdkSerializer();
        byte[] serialized1 = jdkSerializer.serialize(ImmutableList.of(1,2,3));
        System.out.println(jdkSerializer.deserialize(serialized1, ImmutableList.class).getClass());
    }
}
