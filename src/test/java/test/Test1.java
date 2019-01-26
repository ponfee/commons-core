/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.sound.midi.Soundbank;

import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import code.ponfee.commons.concurrent.ThreadPoolExecutors;
import io.netty.util.internal.ThreadLocalRandom;

/**
 * 
 * @author Ponfee
 */
public class Test1 {
    
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Stopwatch watch = Stopwatch.createStarted();
        CompletableFuture<String> future1 = new CompletableFuture<>();
        new Thread(()->{
            try {
                Thread.sleep(2000);
                future1.complete("test1"); // 完成，会通知CompletableFuture.get()
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println("===================");
        System.out.println(future1.get() + " cost: " + watch.stop().toString());
        System.out.println("===================");

        System.out.println();
        watch.reset().start();
        CompletableFuture<String> future2 = CompletableFuture.completedFuture("test2");
        System.out.println("===================");
        System.out.println(future2.get() + " cost: " + watch.stop().toString());
        System.out.println("===================");
    }

    @Test
    public void test() throws InterruptedException, ExecutionException {
        System.out.println("开始...");

        CompletableFuture<Object> fu = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("返回 zhang");
                return "zhang";
            }
        }).thenCombine(CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("返回 phil");
                return "phil";
            }
        }), new BiFunction<String, String, Object>() {
            @Override
            public Object apply(String s1, String s2) {
                String s = s1 + " , " + s2;
                System.out.println("apply:" + s);
                return s;
            }
        }).whenCompleteAsync(new BiConsumer<Object, Throwable>() {
            @Override
            public void accept(Object o, Throwable throwable) {
                System.out.println("accept:" + o.toString());
            }
        });

        
        System.out.println(fu.get());
    }
    
    @Test
    public void test2() throws InterruptedException {
        Stream.of(1,2,3,4,5).map(i -> CompletableFuture.runAsync(()-> {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println(i);
        }))
        .count();
        Thread.sleep(5000);
    }
    @Test
    public void test3() throws InterruptedException {
        System.out.println(ThreadPoolExecutors.INFINITY_QUEUE_EXECUTOR);
        Lists.newArrayList(1,2,3)
            .parallelStream().parallel().sequential() // 并行或串行由最后一个设置决定
        ;
        // ClassName::staticMethodName
        // ClassName::new
        // TypeName[]::new           int[]::new <=> (x -> new int[x])
        // ClassName::instanceMethodName
        // instance::instanceMethodName
        // super::instanceMethodName
        // this::instanceMethodName
        Lists.newArrayList(1,2,3).stream().filter(this::test).forEach(System.out::println);
    }
    
    
    public boolean test(Integer i) {
        return i > 2;
    }
}
