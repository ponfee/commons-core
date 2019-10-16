package code.ponfee.commons.cache;


import static code.ponfee.commons.concurrent.ThreadPoolExecutors.DISCARD_POLICY_SCHEDULER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.Destroyable;

import com.google.common.base.Preconditions;

import code.ponfee.commons.cache.RemovalNotification.RemovalReason;
import code.ponfee.commons.io.Closeables;
import code.ponfee.commons.jce.digest.DigestUtils;
import code.ponfee.commons.util.Base64UrlSafe;

/**
 * 缓存类
 * 
 * @author Ponfee
 * @param <K>
 * @param <V>
 */
public class Cache<K, V> {

    public static final long KEEPALIVE_FOREVER = 0; // 为0表示不失效

    private final boolean caseSensitiveKey; // 是否忽略大小写（只针对String）
    private final boolean compressKey; // 是否压缩key（只针对String）
    private final long keepAliveInMillis; // 默认的数据保存的时间
    private final Map<K, CacheValue<V>> container = new ConcurrentHashMap<>(); // 缓存容器

    private volatile boolean isDestroy = false; // 是否被销毁
    private final Lock lock = new ReentrantLock(); // 定时清理加锁
    private final ScheduledExecutorService scheduler;
    private final RemovalListener<K, V> removalListener;
    private TimestampProvider timestampProvider = TimestampProvider.CURRENT;

    Cache(boolean caseSensitiveKey, boolean compressKey, long keepAliveInMillis, 
          int autoReleaseInSeconds, ScheduledExecutorService scheduler, 
          RemovalListener<K, V> removalListener) {
        Preconditions.checkArgument(keepAliveInMillis >= 0);
        Preconditions.checkArgument(autoReleaseInSeconds >= 0);

        this.caseSensitiveKey = caseSensitiveKey;
        this.compressKey = compressKey;
        this.keepAliveInMillis = keepAliveInMillis;
        this.removalListener = removalListener;
        this.scheduler = scheduler;

        if (autoReleaseInSeconds > 0) {
            if (scheduler == null) {
                scheduler = DISCARD_POLICY_SCHEDULER;
            }

            // 定时清理
            scheduler.scheduleAtFixedRate(() -> {
                // none exception to throw, so can not wrap try catch
                if (!lock.tryLock()) {
                    return;
                }

                long now = now();
                try {
                    //container.entrySet().removeIf(x -> x.getValue().isExpire(now));
                    for (Iterator<Entry<K, CacheValue<V>>> iter = container.entrySet().iterator(); iter.hasNext();) {
                        Entry<K, CacheValue<V>> entry = iter.next();
                        CacheValue<V> cacheValue = entry.getValue();
                        if (cacheValue.isExpire(now)) {
                            iter.remove();
                            onRemoval(entry.getKey(), cacheValue, RemovalReason.EXPIRED);
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }, autoReleaseInSeconds, autoReleaseInSeconds, TimeUnit.SECONDS);

        }
    }

    public boolean isCaseSensitiveKey() {
        return caseSensitiveKey;
    }

    public boolean isCompressKey() {
        return compressKey;
    }

    public long getKeepAliveInMillis() {
        return keepAliveInMillis;
    }

    public RemovalListener<K, V> getRemovalListener() {
        return removalListener;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public TimestampProvider getTimestampProvider() {
        return timestampProvider;
    }

    public void setTimestampProvider(TimestampProvider timestampProvider) {
        this.timestampProvider = timestampProvider;
    }

    private long now() {
        return timestampProvider.get();
    }

    // ---------------------------------------------------------------cache value
    public void set(K key) {
        set(key, null);
    }

    public void set(K key, V value) {
        long expireTimeMillis;
        if (keepAliveInMillis > 0) {
            expireTimeMillis = now() + keepAliveInMillis;
        } else {
            expireTimeMillis = KEEPALIVE_FOREVER;
        }

        set(key, value, expireTimeMillis);
    }

    public void setWithAliveInMillis(K key, V value, int aliveInMillis) {
        Preconditions.checkArgument(aliveInMillis > 0);

        set(key, value, now() + aliveInMillis);
    }

    public void setWithNull(K key, long expireTimeMillis) {
        set(key, null, expireTimeMillis);
    }

    public void set(K key, V value, long expireTimeMillis) {
        Preconditions.checkState(!isDestroy);

        if (expireTimeMillis < KEEPALIVE_FOREVER) {
            expireTimeMillis = KEEPALIVE_FOREVER;
        }

        if (expireTimeMillis == KEEPALIVE_FOREVER || expireTimeMillis > now()) {
            CacheValue<V> newly = new CacheValue<>(value, expireTimeMillis);
            CacheValue<V> former = container.put(getEffectiveKey(key), newly);
            onRemoval(key, former, RemovalReason.REPLACED);
        }
    }

    /**
     * 获取
     * @param key
     * @return
     */
    public V get(K key) {
        if (isDestroy) {
            return null;
        }

        key = getEffectiveKey(key);
        CacheValue<V> cacheValue = container.get(key);
        if (cacheValue == null) {
            return null;
        } else if (cacheValue.isExpire(now())) {
            container.remove(key);
            onRemoval(key, cacheValue, RemovalReason.EXPIRED);
            return null;
        } else {
            return cacheValue.getValue();
        }
    }

    /**
     * Remove key value and return the value if exists
     * 
     * @param key the key
     */
    public V remove(K key) {
        if (isDestroy) {
            return null;
        }

        CacheValue<V> cacheValue = container.remove(getEffectiveKey(key));
        if (cacheValue == null) {
            return null;
        } else if (cacheValue.isExpire(now())) {
            onRemoval(key, cacheValue, RemovalReason.EXPIRED);
            return cacheValue.getValue();
        } else {
            onRemoval(key, cacheValue, RemovalReason.EVICTED);
            return null;
        }
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(K key) {
        if (isDestroy) {
            return false;
        }

        key = getEffectiveKey(key);
        CacheValue<V> cacheValue = container.get(key);
        if (cacheValue == null) {
            return false;
        } else if (cacheValue.isExpire(now())) {
            container.remove(key);
            onRemoval(key, cacheValue, RemovalReason.EXPIRED);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 是否包含指定的value
     * @param value
     * @return
     */
    public boolean containsValue(V value) {
        if (isDestroy) {
            return false;
        }

        CacheValue<V> cacheValue;
        for (Iterator<Entry<K, CacheValue<V>>> i = container.entrySet().iterator(); i.hasNext();) {
            Entry<K, CacheValue<V>> entry = i.next();
            cacheValue = entry.getValue();
            if (cacheValue.isAlive(now())) {
                if (value == null) {
                    if (cacheValue.getValue() == null) {
                        return true;
                    }
                } else if (value.equals(cacheValue.getValue())) {
                    return true;
                }
            } else {
                i.remove();
                onRemoval(entry.getKey(), cacheValue, RemovalReason.EXPIRED);
            }
        }
        return false;
    }

    /**
     * Gets for value collection
     * 
     * @return  the collection of values
     */
    public Collection<V> values() {
        if (isDestroy) {
            return Collections.emptyList();
        }

        Collection<V> values = new ArrayList<>();
        CacheValue<V> value;
        for (Iterator<Entry<K, CacheValue<V>>> i = container.entrySet().iterator(); i.hasNext();) {
            Entry<K, CacheValue<V>> entry = i.next();
            value = entry.getValue();
            if (value.isAlive(now())) {
                values.add(value.getValue());
            } else {
                i.remove();
                onRemoval(entry.getKey(), value, RemovalReason.EXPIRED);
            }
        }
        return values;
    }

    /**
     * get size of the cache keys
     * @return
     */
    public int size() {
        return container.size();
    }

    /**
     * check is empty
     * @return
     */
    public boolean isEmpty() {
        return container.isEmpty();
    }

    /**
     * clear all
     */
    public void clear() {
        Preconditions.checkState(!isDestroy);

        container.clear();
    }

    /**
     * destory the cache self
     */
    public void destroy() {
        isDestroy = true;
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        container.clear();
    }

    public boolean isDestroy() {
        return isDestroy;
    }

    /**
     * get effective key
     * 
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    private K getEffectiveKey(K key) {
        if (key instanceof CharSequence) {
            String k = key.toString();
            if (!caseSensitiveKey) {
                k = k.toLowerCase(); // 不区分大小写（转小写）
            }
            if (compressKey) {
                k = Base64UrlSafe.encode(DigestUtils.sha1(k)); // 压缩key
            }
            key = (K) k;
        }
        return key;
    }

    /**
     * Removing a value
     * 
     * @param key the key
     * @param cacheValue the CacheValue
     * @param removalReason the removalReason
     */
    private void onRemoval(K key, CacheValue<V> cacheValue, RemovalReason removalReason) {
        V value;
        if (cacheValue == null || (value = cacheValue.getValue()) == null) {
            return;
        }

        // Closeable, Destroyable
        if (value instanceof Destroyable) {
            Closeables.log((Destroyable) value);
        } else if (value instanceof AutoCloseable) {
            Closeables.log((AutoCloseable) value);
        }

        if (this.removalListener != null) {
            removalListener.onRemoval(new RemovalNotification<>(key, value, removalReason));
        }
    }

}
