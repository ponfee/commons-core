package test.jedis;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.ObjectUtils;

public class RedissonLockTester {
    private static final String NAME = ObjectUtils.shortid(3);

    public RedissonClient redisson;

    @Before
    public void setup() {
        Config config = new Config();
        config.useSingleServer().setAddress("127.0.0.1:6379");
        redisson = Redisson.create(config);
    }

    @After
    public void teardown() {
        redisson.shutdown();
    }

    @Test
    public void test() throws Exception {
        Scanner s = new Scanner(new FileInputStream(MavenProjects.getTestJavaFile(this.getClass())));
        final AtomicInteger num = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();
        while (s.hasNextLine()) {
            final String line = s.nextLine();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    new Printer(redisson.getLock("redisson:lock")).output(NAME + "-" + num.getAndIncrement() + "\t" + line + "\n");
                }
            });
            threads.add(t);
        }
        s.close();
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private static class Printer {
        private final Lock lock;

        Printer(Lock lock) {
            this.lock = lock;
        }

        private void output(String name) {
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
