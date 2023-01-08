package test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import cn.ponfee.commons.base.tuple.Tuple2;
import cn.ponfee.commons.collect.ArrayHashKey;
import cn.ponfee.commons.concurrent.ThreadPoolTestUtils;
import cn.ponfee.commons.util.CurrencyEnum;
import cn.ponfee.commons.util.Money;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import cn.ponfee.commons.jce.CryptoProvider;
import cn.ponfee.commons.resource.ResourceLoaderFacade;
import cn.ponfee.commons.util.Bytes;
import cn.ponfee.commons.date.Dates;
import cn.ponfee.commons.util.MavenProjects;

/**
 * 
 * @author Ponfee
 */
public class Test1 {
    
    public static void main(String[] args) throws InterruptedException, ExecutionException, JsonProcessingException {
        Money money = Money.of(CurrencyEnum.CNY.currency(), 100);
        String json = JSON.toJSONString(money);
        System.out.println(json);
        System.out.println(new ObjectMapper().writeValueAsString(money));
        Money money1 = JSON.parseObject(json, Money.class);
        Money money2 = new ObjectMapper().readValue(json, Money.class);
        System.out.println(money1.equals(money2));
        System.out.println(new ObjectMapper().writeValueAsString(money1));
        


        String nids = Arrays.stream(new Integer[]{1,2,3}).map(e -> String.valueOf(e)).collect(Collectors.joining(","));
        System.out.println(nids);
        String[] arr = {"a", "b"};
        System.out.println(Arrays.toString(Arrays.copyOfRange(arr, 1, 1)));
        System.out.println(null==null);
        Tuple2 a = Tuple2.of(String.class, ArrayHashKey.of(new Class[]{IntStream.class, Object.class}));
        Tuple2 b = Tuple2.of(String.class, ArrayHashKey.of(new Class[]{IntStream.class, Object.class}));
        System.out.println(a.equals(b));
        System.out.println(a.hashCode() == b.hashCode());
        System.out.println((-1L ^ (-1L << 10)) == (~(-1L << 10)));
        System.out.println(String.format("%02d", 1));
        Date d1 = Dates.toDate("2019-05-10 10:23:34");
        Date d2 = Dates.toDate("2019-05-11 08:23:34");
        
        System.out.println(Dates.daysBetween(Dates.startOfDay(d1), Dates.endOfDay(d2)));
        
        System.out.println(List.class.isInstance(null));
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
        System.out.println(ThreadPoolTestUtils.INFINITY_QUEUE_EXECUTOR);
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
    @Test
    public void test5() throws Exception {
        byte[] data = MavenProjects.getTestJavaFileAsByteArray(this.getClass());
        System.out.println((data.length + 15) / 16);
        System.out.println(Integer.toHexString((data.length + 15) / 16));
        String lineNumberFormat = "%0" + Integer.toHexString((data.length + 15) / 16).length() + "x: ";
        System.out.println(lineNumberFormat);
        System.out.println(Bytes.hexDump(data));
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        /*org.apache.commons.io.HexDump.dump(MavenProjects.getTestJavaFileAsByteArray(this.getClass()), 0, out, 0);
        System.out.println(new String(out.toByteArray()));*/
        
        /*new sun.misc.HexDumpEncoder().encode(MavenProjects.getTestJavaFileAsByteArray(this.getClass()), out);
        System.out.println(new String(out.toByteArray()));*/
        
        
        //System.out.println(Bytes.hexDump(Bytes.fromChar(Numbers.CHAR_ZERO)));
    }
    
    @Test
    public void test6() throws Exception {
        //System.out.println(IOUtils.toString(ResourceLoaderFacade.getResource("/gbkxxx.txt", CryptoProvider.class).getStream(), "GBK"));
        System.out.println(IOUtils.toString(ResourceLoaderFacade.getResource("cert/gbkyyy.txt", CryptoProvider.class).getStream(), "GBK"));
    }
    
}
