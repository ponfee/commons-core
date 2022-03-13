/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.reflect;

import code.ponfee.commons.base.PrimitiveTypes;
import code.ponfee.commons.base.tuple.Tuple;
import code.ponfee.commons.base.tuple.Tuple0;
import code.ponfee.commons.base.tuple.Tuple1;
import code.ponfee.commons.base.tuple.Tuple2;
import code.ponfee.commons.base.tuple.Tuple3;
import code.ponfee.commons.cache.Cache;
import code.ponfee.commons.cache.CacheBuilder;
import code.ponfee.commons.collect.ByteArrayWrapper;
import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.model.Predicates;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.reflect.GenericUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jol.vm.VM;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ponfee
 */
public class ClassUtilsTest {

    static int n = 10000000;

    @Test
    public void test0() {
        for (int i = 0; i < n; i++) {
            ClassUtils.getConstructor(ByteArrayWrapper.class, byte[].class);
        }
    }

    @Test
    public void test1() throws Exception {
        for (int i = 0; i < n; i++) {
            ByteArrayWrapper.class.getConstructor(byte[].class);
        }
    }

    @Test
    public void test20() {
        for (int i = 0; i < n; i++) {
            ClassUtils.getConstructor(ClassUtilsTest.class);
        }
    }

    @Test
    public void test21() throws NoSuchMethodException {
        for (int i = 0; i < n; i++) {
            ClassUtilsTest.class.getConstructor();
        }
    }

    @Test
    public void test3() throws Exception {
        int n = 1000;
        Method method = ClassUtils.class.getMethod("newInstance", Class.class, Class[].class, Object[].class);
        System.out.println(Arrays.toString(ClassUtils.getMethodParamNames(method)));
        for (int i = 0; i < n; i++) {
            ClassUtils.getMethodParamNames(method);
        }
    }

    @Test
    public void test4() throws Exception {
        int n = 1000000;
        Cache<Method, String[]> METHOD_ARGSNAME = CacheBuilder.<Method, String[]>newBuilder().build();
        Method method = ClassUtils.class.getMethod("newInstance", Class.class, Class[].class, Object[].class);
        for (int i = 0; i < n; i++) {
            String[] argsName = METHOD_ARGSNAME.get(method);
            if (argsName == null) {
                argsName = ClassUtils.getMethodParamNames(method);
                METHOD_ARGSNAME.put(method, argsName);
                System.out.println(Arrays.toString(argsName));
            }
        }
    }

    @Test
    public void test5() throws Exception {
        Assert.assertEquals(ClassUtils.class.getMethod("newInstance", Class.class, Class[].class, Object[].class), ClassUtils.class.getMethod("newInstance", Class.class, Class[].class, Object[].class));
        Assert.assertEquals(Result.class.getDeclaredField("code"), Result.class.getDeclaredField("code"));
    }

    @Test
    public void test6() throws Exception {
        System.out.println(Arrays.toString(ClassUtils.getMethodParamNames(ClassUtils.class.getMethod("newInstance", Class.class, Class[].class, Object[].class))));
        System.out.println(ClassUtils.getMethodSignature(ClassUtils.class.getMethod("newInstance", Class.class, Class[].class, Object[].class)));
        System.out.println(ClassUtils.getClassName(ClassUtils.class));
        System.out.println(ClassUtils.getPackagePath(ClassUtils.class));
        System.out.println(ClassUtils.getClassFilePath(ClassUtils.class));
        System.out.println(ClassUtils.getClassFilePath(org.apache.commons.lang3.StringUtils.class));
        System.out.println(ClassUtils.getClasspath(ClassUtils.class));
        System.out.println(ClassUtils.getClasspath(org.apache.commons.lang3.StringUtils.class));
        System.out.println(ClassUtils.getClasspath());
        System.out.println(ClassUtils.newInstance(Tuple3.class, new Object[]{1, 2, 3}));
        System.out.println(ClassUtils.newInstance(Tuple3.class, new Object[]{null, null, null}));
        System.out.println(ClassUtils.newInstance(Tuple2.class, new Object[]{new String[]{"a", "b"}, new Integer[]{1, 2}}));
        System.out.println("Tuple.class.isAssignableFrom(Tuple.class)=" + Tuple.class.isAssignableFrom(Tuple.class));
        System.out.println("Tuple.class.isAssignableFrom(Tuple1.class)=" + Tuple.class.isAssignableFrom(Tuple1.class));
        System.out.println("Tuple1.class.isAssignableFrom(Tuple.class)=" + Tuple1.class.isAssignableFrom(Tuple.class));
        System.out.println(PrimitiveTypes.FLOAT.isCastable(PrimitiveTypes.DOUBLE));
        System.out.println(PrimitiveTypes.FLOAT.isCastable(PrimitiveTypes.FLOAT));
        System.out.println(PrimitiveTypes.FLOAT.isCastable(PrimitiveTypes.LONG));

        System.out.println(ClassUtils.newInstance(Tuple0.class));
        System.out.println(ClassUtils.newInstance(Tuple0.class, new Object[]{}));
        System.out.println(ClassUtils.newInstance(Tuple1.class, new Object[]{null}));
        System.out.println(ClassUtils.newInstance(A.class, new Object[]{1}));

        Assert.assertTrue(new Object().getClass() == Object.class);
        Assert.assertTrue("".getClass() == String.class);
        Assert.assertEquals(String.class.getSimpleName(), "String");
        Assert.assertFalse(String.class.equals(Class.class));
        Assert.assertTrue(String.class.getClass() == Class.class);
        Assert.assertTrue(Object.class.getClass() == Class.class);
        Assert.assertTrue(Class.class.getClass() == Class.class);
        Assert.assertTrue(Class.class.getClass().getClass().getClass().getClass() == Class.class);
        Assert.assertTrue(String.class instanceof Class);
        Assert.assertTrue(Object.class instanceof Class);
        Assert.assertTrue(Class.class instanceof Class);

        Assert.assertTrue(Predicates.Y.equals(true));
        Assert.assertFalse(Predicates.Y.equals(false));
        Assert.assertTrue(Predicates.N.equals(false));
        Assert.assertFalse(Predicates.N.equals(true));
        Assert.assertEquals(Predicates.N.code(), 'N');
        Assert.assertTrue(Predicates.Y.state());
        Assert.assertFalse(Predicates.N.state());
        Assert.assertEquals(ClassUtils.invoke(A.class, "max", new Object[]{2, 4}), new Integer(4));
        Assert.assertEquals(ClassUtils.invoke(new A(5), "add", new Object[]{2}), new Integer(7));
        Assert.assertEquals(Boolean.TYPE, boolean.class);
        Assert.assertEquals((byte) 0B1_0_0_0_0_0_0_0, -128);
        Assert.assertEquals(0B1_0_0_0_0_0_0_0, 128);
        Assert.assertThrows(RuntimeException.class, () -> ClassUtils.invoke(String.class, "getName"));

        // 普通Class类实例(如String.class)：只处理其所表示类的静态方法，如String.valueOf(1)。不支持Class类中的实例方法，如String.class.getName()
        Assert.assertEquals(ClassUtils.invoke(String.class, "valueOf", new Object[]{1}), "1");
        Assert.assertEquals(ClassUtils.invoke(String.class, "join", new Object[]{"|", new String[]{"a", "b"}}), "a|b");
        Assert.assertNull(ClassUtils.getMethod(String.class, "getName"));

        // Class.class对象：只处理Class类中的实例方法，如Class.class.getName()。不支持Class类中的静态方法，如Class.forName("code.ponfee.commons.base.tuple.Tuple0");
        Assert.assertEquals(ClassUtils.invoke(Class.class, "getName"), "java.lang.Class");
        Assert.assertNull(ClassUtils.getMethod(Class.class, "forName", String.class));
        //double a = 1;
        //float a = 1;
        //long a = 1;
        int a = 1;
        //short a = 1;
        //char a = 1;
        //byte a = 1;
        //boolean a = true;
        new A(a);
        ClassUtils.newInstance(A.class, new Class[]{int.class}, new Object[]{a});
        System.out.println(0B1_0_0_0_0_0_0_0);
        System.out.println(0B1_1_1_1_1_1_1_1);
    }

    @Test
    public void test00() throws Exception {
        System.out.println(Arrays.toString(A.class.getInterfaces()));
        System.out.println(Arrays.toString(Tuple.class.getInterfaces()));

        Assert.assertEquals(31, Fields.get(Tuple.class, "HASH_FACTOR"));
        Fields.put(Tuple.class, "HASH_FACTOR", 311); // final可以修改
        Assert.assertEquals(311, Fields.get(Tuple.class, "HASH_FACTOR"));
        Assert.assertEquals(31, Tuple.HASH_FACTOR); // 编译时已经是直接引用
        System.out.println(B.INTF_1);

        Assert.assertNull(Fields.get(A.class, "static_2"));
        A.max(1, 2); // new A(1); 要初始化
        Assert.assertEquals(10, Fields.get(A.class, "static_2"));
        Fields.put(A.class, "static_2", 20);
        Assert.assertEquals(20, Fields.get(A.class, "static_2"));
        Assert.assertNotNull(null, ClassUtils.getStaticField(A.class, "static_1"));
        B.static_2 = 123;
        Assert.assertEquals(123, Fields.get(A.class, "static_2"));
        Assert.assertEquals(123, Fields.get(B.class, "static_2"));


        Assert.assertEquals(Fields.get(Tuple1.of("xx"), "a"), "xx");
        System.out.println(PrimitiveTypes.allPrimitiveTypes());

        System.out.println(GenericUtils.getFieldActualType(A.class, ClassUtils.getField(A.class, "member_1")));// java.util.List
        System.out.println(GenericUtils.getFieldActualType(A.class, ClassUtils.getField(A.class, "member_2"))); // java.util.List
        System.out.println(GenericUtils.getFieldActualType(A.class, ClassUtils.getField(A.class, "member_3"))); // java.lang.Object

        System.out.println(GenericUtils.getFieldActualType(B.class, ClassUtils.getField(B.class, "member_1"))); // java.lang.Object
        System.out.println(GenericUtils.getFieldActualType(B.class, ClassUtils.getField(B.class, "member_2"))); // java.lang.Object
        System.out.println(GenericUtils.getFieldActualType(B.class, ClassUtils.getField(B.class, "member_3"))); // java.lang.Integer

        System.out.println(GenericUtils.getFieldActualType(A.class, ClassUtils.getStaticFieldInClassChain(A.class, "static_1").b));
    }

    @Test
    public void test8() throws Exception {
        A a = new B(1);
        B b = new B(1);
        Assert.assertEquals("A#func1", a.func1());
        Assert.assertEquals("A#func2", a.func2());
        Assert.assertEquals("B#func1", b.func1());
        Assert.assertEquals("A#func2", b.func2());

        Assert.assertEquals("test-static", (A.get()).test());

        Fields.put(Double.class, "SIZE", 999);
        Assert.assertEquals(999, Fields.get(Double.class, "SIZE"));
        Assert.assertEquals(64, Double.SIZE);
        Assert.assertEquals(999, Fields.get(Double.class, "SIZE"));
    }

    @Test
    public void test9() {
        Assert.assertTrue(Collects.toArray(new Object[]{"1", "2"})[0] instanceof String);
        Assert.assertEquals(String[].class, Collects.toArray(new String[]{"1", "2"}).getClass());

        Assert.assertTrue(toArray(new Object[]{"1", "2"})[0] instanceof String);
        Assert.assertEquals(String[].class, toArray(new String[]{"1", "2"}).getClass());

        Assert.assertTrue(get(new Object[]{"1", "2"}, 0) instanceof String);
        Assert.assertTrue(get(new String[]{"1", "2"}, 0) instanceof String);
        Assert.assertTrue(get(new Integer[]{1, 2}, 0) instanceof Integer);
    }

    @Test
    public void tes10() {
        Object obj = new Object();
        Assert.assertEquals(obj.hashCode(), System.identityHashCode(obj));
        Assert.assertEquals(Fields.addressOf(obj), VM.current().addressOf(obj));
        System.out.println("identityHashCode: " + System.identityHashCode(obj));
        System.out.println("Fields.addressOf: " + Fields.addressOf(obj));
        System.out.println("VM.addressOf: " + VM.current().addressOf(obj));

        obj = ClassUtilsTest.class;
        Assert.assertEquals(obj.hashCode(), System.identityHashCode(obj));
        Assert.assertEquals(Fields.addressOf(obj), VM.current().addressOf(obj));

        Assert.assertNotEquals("123".hashCode(), System.identityHashCode(new String("123")));
        Assert.assertNotEquals(Fields.addressOf("123"), VM.current().addressOf(new String("123")));

        Assert.assertTrue(String.class == "123".getClass());


        Assert.assertFalse(new Integer(1) == new Integer(1));
        Assert.assertTrue(Integer.valueOf(1) == Integer.valueOf(1));
        Assert.assertEquals(Fields.addressOf(1), VM.current().addressOf(1));
        Assert.assertEquals(Fields.addressOf(1), VM.current().addressOf(1));
        Assert.assertEquals(Fields.addressOf(1L), VM.current().addressOf(1L));
        Assert.assertEquals(Fields.addressOf((byte) 1), VM.current().addressOf((byte) 1));


        Assert.assertEquals(Fields.addressOf(new Object[]{1, 2, 3, obj}, 3), VM.current().addressOf(obj));
        Assert.assertEquals(Fields.addressOf(new Object[]{1, 2, obj, 6, new Object(), "abc"}, 2), VM.current().addressOf(obj));

        String str = "fdsaf23";
        Assert.assertEquals(Fields.addressOf(new String[]{"abc", str, new String(), null, ""}, 1), VM.current().addressOf(str));

        Byte a = 1;
        Assert.assertEquals(Fields.addressOf(new Byte[]{127, a, 21, 4}, 1), VM.current().addressOf(a));

    }

    public static Object[] toArray(Object... args) {
        return args;
    }

    public static Object get(Object[] args, int i) {
        return args[i];
    }

    public static class A<T> {
        public List<T> member_1;
        public List<String> member_2;
        public T member_3;
        private int x;
        public static List<A> static_1;
        public static Integer static_2 = 10;

        public A(int x) {
            this.x = x;
        }

        public void a(int i) {

        }

        public int add(int i) {
            return x + i;
        }

        public static String func1() {
            return "A#func1";
        }

        public static String func2() {
            return "A#func2";
        }

        public static int max(int a, int b) {
            return a > b ? a : b;
        }

        /*public static void a(int i) {

        }*/

        public static String test() {
            return "test-static";
        }

        public static A get() {
            return null;
        }
    }

    public static interface X {
        String INTF_1 = "test1";
    }

    public static class B extends A<Integer> implements X {

        public B(int x) {
            super(x);
        }

        public static String func1() {
            return "B#func1";
        }

    }

}
