package test.utils;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.ponfee.commons.cache.Cache;
import cn.ponfee.commons.cache.CacheBuilder;
import cn.ponfee.commons.util.ObjectUtils;
import cn.ponfee.commons.util.UuidUtils;

public class TestCache {

    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        Cache<String, Void> cache = CacheBuilder.<String, Void>newBuilder()
            .caseSensitiveKey(false).compressKey(true).autoReleaseInSeconds(2).build();
        AtomicBoolean flag = new AtomicBoolean(true);
        int n = 10;
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            threads[i] = new Thread(() -> {
                while (flag.get()) {
                    if (cache.isDestroy()) break;
                    cache.put(UuidUtils.uuid32(), null, new Date().getTime() + random.nextInt(1000));
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (int i = 0; i < 15; i++) {
            System.out.println(cache.size());
            Thread.sleep(1000);
        }
        flag.set(false);
        for (Thread thread : threads) {
            thread.join();
        }
        cache.destroy();
        System.out.println(cache.size());
    }
}
