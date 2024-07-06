package cn.ponfee.commons.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import cn.ponfee.commons.concurrent.ThreadPoolTestUtils;
import com.google.common.base.Stopwatch;
import org.junit.Test;

import cn.ponfee.commons.model.Result;
import cn.ponfee.commons.reflect.ClassUtils;
import org.openjdk.jol.info.ClassLayout;

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

        System.out.println("\n-----------------------");
        System.out.println(ClassLayout.parseInstance(new Object()).toPrintable());
        System.out.println("\n-----------------------");
        System.out.println(ClassLayout.parseInstance(new Object[10]).toPrintable());
        System.out.println("\n-----------------------");
        System.out.println(ClassLayout.parseInstance(new long[10]).toPrintable());
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
        execute(32, () -> {
            get("123");
        }, 5, ThreadPoolTestUtils.CALLER_RUN_SCHEDULER);
    }

    @Test
    public void test7() {
        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        execute(32, () -> {
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


    /**
     * Exec async, usual use in test case
     *
     * @param parallelism the parallelism
     * @param command     the command
     * @param execSeconds the execSeconds
     * @param executor    the executor
     */
    public static void execute(int parallelism, Runnable command,
                               int execSeconds, Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        AtomicBoolean flag = new AtomicBoolean(true);

        // CALLER_RUNS: caller run will be dead loop
        // caller thread will be loop exec command, can't to run the after code{flag.set(false)}
        // threadNumber > 32
        CompletableFuture<?>[] futures = IntStream
            .range(0, parallelism)
            .mapToObj(i -> (Runnable) () -> {
                while (flag.get() && !Thread.currentThread().isInterrupted()) {
                    command.run();
                }
            })
            .map(runnable -> CompletableFuture.runAsync(runnable, executor))
            .toArray(CompletableFuture[]::new);

        try {
            // parent thread sleep
            Thread.sleep(execSeconds * 1000L);
            flag.set(false);
            CompletableFuture.allOf(futures).join();
        } catch (InterruptedException e) {
            flag.set(false);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            System.out.println("multi thread exec async duration: " + watch.stop());
        }
    }

}
