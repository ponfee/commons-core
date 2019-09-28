package code.ponfee.commons.cache;

/**
 * Removal listener
 * 
 * @author Ponfee
 * @param <K> cache key
 * @param <V> cache value
 */
@FunctionalInterface
public interface RemovalListener<K, V> {
    void onRemoval(RemovalNotification<K, V> notification);
}