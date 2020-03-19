package code.ponfee.commons.model;

import java.util.Map;

/**
 * Map trait for get the value with typed
 * 
 * @author Ponfee
 * @param <K>
 * @param <V>
 */
public interface MapTrait<K, V> extends Map<K, V>, PairTrait<K, V> {

    default V getValue(K key) {
        return this.get(key);
    }

    default V removeValue(K key) {
        return this.remove(key);
    }

}
