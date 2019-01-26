package code.ponfee.commons.cache;

/**
 * 缓存值
 * @author fupf
 * @param <T>
 */
class CacheValue<T> implements java.io.Serializable {

    private static final long serialVersionUID = 4266458031910874821L;

    private final long expireTimeMillis; // 失效时间
    private final T value; // 值

    CacheValue(T value, long expireTimeMillis) {
        this.value = value;
        this.expireTimeMillis = expireTimeMillis;
    }

    boolean isAlive(long refTimeMillis) {
        return Cache.KEEPALIVE_FOREVER == expireTimeMillis
            || expireTimeMillis > refTimeMillis;
    }

    boolean isExpire(long refTimeMillis) {
        return !isAlive(refTimeMillis);
    }

    T getValue() {
        return value;
    }

}
