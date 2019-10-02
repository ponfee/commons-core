package code.ponfee.commons.cache;

/**
 * Removal notification
 * 
 * @author Ponfee
 * @param <K>
 * @param <V>
 */
public class RemovalNotification<K, V> {

    public enum RemovalReason {
        REPLACED, // value was replaced by the user
        EVICTED, // manually removed by the user
        EXPIRED // expiration timestamp has passed
    }

    private final K key;
    private final V value;
    private final RemovalReason removalReason;

    public RemovalNotification(K key, V value, RemovalReason removalReason) {
        this.key = key;
        this.value = value;
        this.removalReason = removalReason;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public RemovalReason getRemovalReason() {
        return removalReason;
    }
}
