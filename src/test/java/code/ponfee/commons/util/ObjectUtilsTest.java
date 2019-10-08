package code.ponfee.commons.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.log.LogInfo;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;

public class ObjectUtilsTest {

    static int round = 100000000;
    
    private static final Map<String, Object> map = new HashMap<>();

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
    public void test3() {
        Byte b1 = 127;
        Byte b2 = 127;
        Byte b3 = new Byte((byte) 127);
        System.out.println(b1 == b2);
        System.out.println(b3 == b2);
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
        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(32, () -> {
            get("123");
        }, 5);
    }
    
    @Test
    public void test6() {
        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(32, () -> {
            Singleton.getInstance();
        }, 5);
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
}
