package cn.ponfee.commons.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ponfee.commons.concurrent.ThreadPoolTestUtils;
import org.junit.Test;

import cn.ponfee.commons.concurrent.MultithreadExecutors;
import cn.ponfee.commons.model.Result;
import cn.ponfee.commons.reflect.ClassUtils;

public class ObjectUtilsTest {

    static int round = 100000000;

    private static final Map<String, Object> map = new HashMap<>();

    @Test
    public void test0() {
        System.out.println(ObjectUtils.newInstance(int.class));
        System.out.println(ObjectUtils.newInstance(Integer.class));
        System.out.println(ObjectUtils.newInstance(String.class));
        System.out.println(ObjectUtils.newInstance(Map.class));
        System.out.println(ObjectUtils.newInstance(List.class));
    }

    @Test
    public void test1() {
        for (int i = 0; i < round; i++) {
            ObjectUtils.newInstance(Result.class);
        }
    }

    @Test
    public void test2() {
        for (int i = 0; i < round; i++) {
            ClassUtils.newInstance(Result.class);
        }
    }

    @Test
    public void test3() throws Exception {
        for (int i = 0; i < round; i++) {
            Result.class.getConstructor().newInstance();
        }
    }

    @Test
    public void test4() {
        Byte b1 = 127;
        Byte b2 = 127;
        Byte b3 = new Byte((byte) 127);
        System.out.println(b1 == b2);
        System.out.println(b3 == b2);
    }

    @Test
    public void test5() {
        Byte b1 = 127;
        Byte b2 = 127;
        Byte b3 = new Byte((byte) 127);
        System.out.println(b1 == b2);
        System.out.println(b3 == b2);
    }

    @Test
    public void test6() {
        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutors.execute(32, () -> {
            get("123");
        }, 5, ThreadPoolTestUtils.CALLER_RUN_SCHEDULER);
    }

    @Test
    public void test7() {
        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutors.execute(32, () -> {
            Singleton.getInstance();
        }, 5, ThreadPoolTestUtils.CALLER_RUN_SCHEDULER);
    }

    public static Object get(String key) {
        Object val = map.get(key);
        if (val == null) {
            synchronized (map) {
                if ((val = map.get(key)) == null) {
                    map.put(key, val = new Object());
                    System.err.println("================");
                }
            }
        }
        return val;
    }

    public static class Singleton {
        private static Singleton instance = null;

        private Singleton() {}

        public static Singleton getInstance() {
            if (instance == null) {
                synchronized (Singleton.class) {
                    if (instance == null) {
                        System.err.println("================");
                        instance = new Singleton();
                    }
                }
            }
            return instance;
        }
    }

    class Helper {
    }

    class Foo {
        /** 
         * If perThreadInstance.get() returns a non-null value, this thread
         * has done synchronization needed to see initialization
         * of helper 
         */
        private final ThreadLocal perThreadInstance = new ThreadLocal();
        private Helper helper = null;

        public Helper getHelper() {
            if (perThreadInstance.get() == null) createHelper();
            return helper;
        }

        private final void createHelper() {
            synchronized (this) {
                if (helper == null) helper = new Helper();
            }
            // Any non-null value would do as the argument here
            perThreadInstance.set(perThreadInstance);
        }
    }
}
