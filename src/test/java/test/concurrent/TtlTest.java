package test.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;

/**
 */
public class TtlTest {

    static ExecutorService executorService = Executors.newFixedThreadPool(1);

    public static void main(String[] args) {
        //子线程每次new 所以会复制线程的InheritableThreadLocal,结果正确
        //withoutThreadPool(10);

        //因线程池复用线程,不会每次new 所以不会更新父线程InheritableThreadLocal 的值,导致结果错误
        withThreadPool(10);
    }

    public static void withoutThreadPool(int c) {
        for (int i = 0; i < c; i++) {
            Integer var1 = (int) (Math.random() * 100);
            Integer var2 = (int) (Math.random() * 100);
            MyContextHolder.set(var1);
            threadRun(var1, var2);
        }
    }

    public static void withThreadPool(int c) {
        for (int i = 0; i < c; i++) {
            Integer var1 = (int) (Math.random() * 100);
            Integer var2 = (int) (Math.random() * 100);
            MyContextHolder.set(var1);
            threadPoolExecute(var1, var2);
        }
    }

    public static void threadRun(Integer var1, Integer var2) {
        new Thread(() -> assert1(var1, var2)).start();
    }

    public static void threadPoolExecute(Integer var1, Integer var2) {
        executorService.execute(TtlRunnable.get(() -> assert1(var1, var2)));  // XXX TtlRunnable
    }

    public static void assert1(Integer var1, Integer var2) {
        System.out.println(MyContextHolder.get() * var2 == var1 * var2);
    }

    public static class MyContextHolder {

        //private static ThreadLocal<Integer> stringThreadLocal = new InheritableThreadLocal<>();
        private static ThreadLocal<Integer> stringThreadLocal = new TransmittableThreadLocal<>(); // XXX TransmittableThreadLocal

        public static void set(Integer data) {
            stringThreadLocal.set(data);
        }

        public static Integer get() {
            return stringThreadLocal.get();
        }
    }

}