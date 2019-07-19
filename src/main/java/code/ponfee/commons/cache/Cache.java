package code.ponfee.commons.cache;


import static code.ponfee.commons.concurrent.ThreadPoolExecutors.DISCARD_POLICY_SCHEDULER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.Destroyable;

import com.google.common.base.Preconditions;

import code.ponfee.commons.jce.digest.DigestUtils;
import code.ponfee.commons.util.Base64UrlSafe;

/**
 * 缓存类
 * @author fupf
 * @param <T>
 */
public class Cache<T> {

    public static final long KEEPALIVE_FOREVER = 0; // 为0表示不失效

    private final boolean caseSensitiveKey; // 是否忽略大小写（只针对String）
    private final boolean compressKey; // 是否压缩key（只针对String）
    private final long keepAliveInMillis; // 默认的数据保存的时间
    private final Map<Comparable<?>, CacheValue<T>> container = new ConcurrentHashMap<>(); // 缓存容器

    private volatile boolean isDestroy = false; // 是否被销毁
    private final Lock lock = new ReentrantLock(); // 定时清理加锁
    private ScheduledExecutorService scheduler;
    private TimestampProvider timestampProvider = TimestampProvider.CURRENT;

    Cache(boolean caseSensitiveKey, boolean compressKey, long keepAliveInMillis, 
          int autoReleaseInSeconds, ScheduledExecutorService scheduler) {
        Preconditions.checkArgument(keepAliveInMillis >= 0);
        Preconditions.checkArgument(autoReleaseInSeconds >= 0);

        this.caseSensitiveKey = caseSensitiveKey;
        this.compressKey = compressKey;
        this.keepAliveInMillis = keepAliveInMillis;

        if (autoReleaseInSeconds > 0) {

            if (scheduler != null) {
                this.scheduler = scheduler;
            } else {
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
                    for (Iterator<Entry<Comparable<?>, CacheValue<T>>> iter = container.entrySet().iterator(); iter.hasNext();) {
                        CacheValue<T> cacheValue = iter.next().getValue();
                        if (cacheValue.isExpire(now)) {
                            iter.remove();
                            closeDaemon(cacheValue);
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

    public TimestampProvider getTimestampProvider() {
        return timestampProvider;
    }

    public void setTimestampProvider(TimestampProvider timestampProvider) {
        this.timestampProvider = timestampProvider;
    }

    private long now() {
        return timestampProvider.get();
    }

    // --------------------------------cache value-------------------------------
    public void set(Comparable<?> key) {
        set(key, null);
    }

    public void set(Comparable<?> key, T value) {
        long expireTimeMillis;
        if (keepAliveInMillis > 0) {
            expireTimeMillis = now() + keepAliveInMillis;
        } else {
            expireTimeMillis = KEEPALIVE_FOREVER;
        }

        this.set(key, value, expireTimeMillis);
    }

    public void setWithAliveInMillis(Comparable<?> key, T value, int aliveInMillis) {
        Preconditions.checkArgument(aliveInMillis > 0);

        this.set(key, value, now() + aliveInMillis);
    }

    public void setWithNull(Comparable<?> key, long expireTimeMillis) {
        set(key, null, expireTimeMillis);
    }

    public void set(Comparable<?> key, T value, long expireTimeMillis) {
        Preconditions.checkState(!isDestroy);

        if (expireTimeMillis < KEEPALIVE_FOREVER) {
            expireTimeMillis = KEEPALIVE_FOREVER;
        }

        if (expireTimeMillis == KEEPALIVE_FOREVER || expireTimeMillis > now()) {
            container.put(getEffectiveKey(key), new CacheValue<>(value, expireTimeMillis));
        }
    }

    /**
     * 获取
     * @param key
     * @return
     */
    public T get(Comparable<?> key) {
        if (isDestroy) {
            return null;
        }

        key = getEffectiveKey(key);
        CacheValue<T> cacheValue = container.get(key);
        if (cacheValue == null) {
            return null;
        } else if (cacheValue.isExpire(now())) {
            container.remove(key);
            closeDaemon(cacheValue);
            return null;
        } else {
            return cacheValue.getValue();
        }
    }

    /**
     * Gets value by key and remove it
     * 
     * @param key the key
     */
    public T getAndRemove(Comparable<?> key) {
        if (isDestroy) {
            return null;
        }

        return Optional.ofNullable(
            container.remove(getEffectiveKey(key))
        ).filter(
            v -> v.isAlive(now())
        ).map(
            CacheValue::getValue
        ).orElse(null);
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(Comparable<?> key) {
        if (isDestroy) {
            return false;
        }

        key = getEffectiveKey(key);
        CacheValue<T> cacheValue = container.get(key);
        if (cacheValue == null) {
            return false;
        } else if (cacheValue.isExpire(now())) {
            container.remove(key);
            closeDaemon(cacheValue);
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
    public boolean containsValue(T value) {
        if (isDestroy) {
            return false;
        }

        CacheValue<T> cacheValue;
        for (Iterator<Entry<Comparable<?>, CacheValue<T>>> i = container.entrySet().iterator(); i.hasNext();) {
            cacheValue = i.next().getValue();
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
                closeDaemon(cacheValue);
            }
        }
        return false;
    }

    /**
     * get for value collection
     * @return  the collection of values
     */
    public Collection<T> values() {
        if (isDestroy) {
            return Collections.emptyList();
        }

        Collection<T> values = new ArrayList<>();
        CacheValue<T> value;
        for (Iterator<Entry<Comparable<?>, CacheValue<T>>> i = container.entrySet().iterator(); i.hasNext();) {
            value = i.next().getValue();
            if (value.isAlive(now())) {
                values.add(value.getValue());
            } else {
                i.remove();
                closeDaemon(value);
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
     * @param key
     * @return
     */
    private Comparable<?> getEffectiveKey(Comparable<?> key) {
        if (key instanceof CharSequence) {
            if (!caseSensitiveKey) {
                key = key.toString().toLowerCase(); // 不区分大小写（转小写）
            }
            if (compressKey) {
                key = Base64UrlSafe.encode(
                    DigestUtils.sha1(key.toString())
                ); // 压缩key
            }
        }
        return key;
    }

    /**
     * Deamon close the expire value
     * 
     * @param cacheValue the cache value
     */
    private void closeDaemon(CacheValue<T> cacheValue) {
        T value = cacheValue.getValue();
        if (value instanceof AutoCloseable) {
            try {
                ((AutoCloseable) value).close();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        if (value instanceof Destroyable) {
            try {
                ((Destroyable) value).destroy();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

}
