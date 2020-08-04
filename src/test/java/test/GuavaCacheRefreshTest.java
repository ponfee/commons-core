/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2020, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Ponfee
 */
public class GuavaCacheRefreshTest {
    public class SkuCache {
        private String skuId;
        private String skuCode;
        private Long realQuantity;

        public String getSkuId() {
            return skuId;
        }

        public String getSkuCode() {
            return skuCode;
        }

        public Long getRealQuantity() {
            return realQuantity;
        }

        public void setSkuId(String skuId) {
            this.skuId = skuId;
        }

        public void setSkuCode(String skuCode) {
            this.skuCode = skuCode;
        }

        public void setRealQuantity(Long realQuantity) {
            this.realQuantity = realQuantity;
        }

    }

    AtomicInteger loadTimes = new AtomicInteger(0);
    AtomicInteger count = new AtomicInteger(0);

    @Test
    public void testCacheUse() throws Exception {
        LoadingCache<String, SkuCache> loadingCache = CacheBuilder.newBuilder().refreshAfterWrite(1000, TimeUnit.MILLISECONDS)
            //Prevent data reloading from failing, but the value of memory remains the same
            .expireAfterWrite(1500, TimeUnit.MILLISECONDS).build(new CacheLoader<String, SkuCache>() {
                @Override
                public SkuCache load(String key) {
                    System.out.println("============================load " + key);
                    return load0(key);
                }

                @Override
                public ListenableFuture<SkuCache> reload(String key, SkuCache oldValue) throws Exception {
                    checkNotNull(key);
                    checkNotNull(oldValue);
                    System.out.println("============================reload " + key);
                    return Futures.immediateFuture(load0(key));
                }

                private SkuCache load0(String key) {
                    SkuCache skuCache = new SkuCache();
                    skuCache.setSkuCode(key + "---" + (loadTimes.incrementAndGet()));
                    skuCache.setSkuId(key);
                    skuCache.setRealQuantity(100L);
                    return skuCache;
                }
            });

        int count = 5;
        Thread[] threads = new Thread[count];
        for (int i = 0; i < count; i++) {
            threads[i] = new Thread(() -> {
                try {
                    getValue(loadingCache);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        System.out.println("finish");
    }

    private void getValue(LoadingCache<String, SkuCache> loadingCache) throws Exception {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(300l);
            System.out.println(loadingCache.get("sku").toString() + " - " + count.incrementAndGet());
        }
    }
}
