package test.reflect;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class GuavaCacheTest {

    @Test
    public void create1() throws Exception {
        LoadingCache<String, Long> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .weakValues()
            .weakKeys()
            .softValues()
            .expireAfterAccess(30L, TimeUnit.SECONDS)
            .build(new CacheLoader<String, Long>() {
                public @Override Long load(String key) {
                    return -1L; // 缓存未命中，会调用此方法加载(不能为null)
                }
            }
        );

        cache.put("abc", 123L);
        System.out.println(cache.get("abc"));
        System.out.println(cache.get("def"));
        System.out.println(cache.getUnchecked("123"));
    }
    
    @Test
    public void create2() throws Exception {
        Cache<String, Long> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(); // look Ma, no CacheLoader
        
        // If the key wasn't in the "easy to compute" group, we need to
        // do things the hard way.
        Long result = cache.get("123", new Callable<Long>() {
            public @Override Long call() {
                return -1L;  // 缓存未命中，会调用此方法加载
            }
        });
        System.out.println(result);
        System.out.println(cache.getIfPresent("456"));
    }
}
