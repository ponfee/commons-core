package test.jedis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.io.WrappedBufferedReader;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.JedisLock;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.ObjectUtils;
import io.netty.util.internal.ThreadLocalRandom;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jedis-cfg.xml" })
public class JedisLockTester {
    private static final String NAME = ObjectUtils.shortid(3);
    private @Resource JedisClient jedisClient;

    @Before
    public void setup() {
        /*JedisPoolConfig poolCfg = new JedisPoolConfig();
        poolCfg.setMaxTotal(100);
        poolCfg.setMaxIdle(200);
        poolCfg.setMinIdle(100);
        poolCfg.setMaxWaitMillis(1000);
        poolCfg.setTestOnBorrow(false);
        poolCfg.setTestOnReturn(false);
        poolCfg.setTestWhileIdle(false);
        poolCfg.setNumTestsPerEvictionRun(-1);
        poolCfg.setMinEvictableIdleTimeMillis(60000);
        poolCfg.setTimeBetweenEvictionRunsMillis(30000);
        jedisClient = new JedisClient(poolCfg, "local1:127.0.0.1:6379", new KryoSerializer());*/
        //jedisClient = new JedisClient(poolCfg, "127.0.0.1:6379;127.0.0.1:6380;", new JdkSerializer());
    }

    @After
    public void teardown() {
        jedisClient.destroy();
    }

    @Test
    public void test0() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            System.out.println(jedisClient.valueOps().setnx("abcde", "123", 1));
            Thread.sleep(200 + ThreadLocalRandom.current().nextInt(500));
        }
    }

    @Test
    public void test1() throws IOException, InterruptedException {
        WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(this.getClass()));
        final Printer printer = new Printer(new JedisLock(jedisClient, "testLock1", 5));
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        List<Thread> threads = new ArrayList<>();
        System.out.println("\n=========================START========================");
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            threads.add(new Thread(() -> {
                printer.output(NAME + "-" + num.getAndIncrement() + "\t" + _line + "\n");
            }));
        }
        reader.close();
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================END========================\n");
    }

    @Test
    public void test2() throws IOException, InterruptedException {
        WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(this.getClass()));
        final Lock lock = new JedisLock(jedisClient, "testLock2", 5);
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        List<Thread> threads = new ArrayList<>();
        System.out.println("\n=========================START========================");
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            threads.add(new Thread(() -> {
                new Printer(lock).output(NAME + "-" + num.getAndIncrement() + "\t" + _line + "\n");
            }));
        }
        reader.close();
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================END========================\n");
    }

    @Test
    public void test3() throws IOException, InterruptedException {
        WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(this.getClass()));
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        List<Thread> threads = new ArrayList<>();
        System.out.println("\n=========================START========================");
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            threads.add(new Thread(() -> {
                new Printer(new JedisLock(jedisClient, "testLock3", 5)).output(NAME + "-" + num.getAndIncrement() + "\t" + _line + "\n");
            }));
        }
        reader.close();
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================END========================\n");
    }

    @Test
    public void test4() throws IOException, InterruptedException {
        Printer printer = new Printer(new JedisLock(jedisClient, "testLock4", 5));
        AtomicInteger num = new AtomicInteger(0);
        System.out.println("\n=========================START========================");
        List<Map<Integer, String>> lines = Files.readLines(
            MavenProjects.getTestJavaFile(this.getClass()), StandardCharsets.UTF_8
        ).stream().map(
           line -> ImmutableMap.of((Integer) num.getAndIncrement(), line)
       ).collect(Collectors.toList());

       MultithreadExecutor.runAsync(lines, map -> {
           Entry<Integer, String> line = map.entrySet().iterator().next();
           printer.output(NAME + "-" + line.getKey() + "\t" + line.getValue() + "\n");
       });
        System.out.println("=========================END========================\n");
    }

    @Test
    public void test5() throws IOException, InterruptedException {
        JedisLock lock = new JedisLock(jedisClient, "testLock5", 30);
        System.out.println(lock.tryLock());
        System.out.println(lock.isLocked());
        System.out.println(lock.tryLock());
        System.out.println(lock.isLocked());
        lock.unlock();
        System.out.println(lock.isLocked());
    }

    private static class Printer {
        private final Lock lock;

        Printer(Lock lock) {
            this.lock = lock;
        }

        private void output(final String name) {
            lock.lock();
            try {
                for (int i = 0; i < name.length(); i++) {
                    System.out.print(name.charAt(i));
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
