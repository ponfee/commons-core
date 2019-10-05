package code.ponfee.commons.cache;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 缓存构建类
 * 
 * @author Ponfee
 */
public final class CacheBuilder<K, V> {
    private CacheBuilder() {}

    private boolean caseSensitiveKey = true; // （默认）区分大小写
    private boolean compressKey = false; // （默认）不压缩key
    private int autoReleaseInSeconds = 0; // （默认0为不清除）清除无效key的的定时时间间隔
    private long keepaliveInMillis = 0; // key保留时间，0表示无限制
    private ScheduledExecutorService executor; // 定时执行器
    private RemovalListener<K, V> removalListener; // 删除监听器

    public CacheBuilder<K, V>  caseSensitiveKey(boolean caseSensitiveKey) {
        this.caseSensitiveKey = caseSensitiveKey;
        return this;
    }

    public CacheBuilder<K, V>  compressKey(boolean compressKey) {
        this.compressKey = compressKey;
        return this;
    }

    public CacheBuilder<K, V>  autoReleaseInSeconds(int autoReleaseInSeconds) {
        this.autoReleaseInSeconds = autoReleaseInSeconds;
        return this;
    }

    public CacheBuilder<K, V>  keepaliveInMillis(long keepaliveInMillis) {
        this.keepaliveInMillis = keepaliveInMillis;
        return this;
    }

    public CacheBuilder<K, V>  scheduledExecutor(ScheduledExecutorService executor) {
        this.executor = executor;
        return this;
    }

    public CacheBuilder<K, V>  removalListener(RemovalListener<K, V> removalListener) {
        this.removalListener = removalListener;
        return this;
    }

    public Cache<K, V> build() {
        return new Cache<>(caseSensitiveKey, compressKey, keepaliveInMillis, 
                           autoReleaseInSeconds, executor, removalListener);
    }

    public static <K, V> CacheBuilder<K, V>  newBuilder() {
        return new CacheBuilder<>();
    }
}
