package code.ponfee.commons.model;

import java.util.Map;

/**
 * Get the value with typed for {@link Map}
 * 
 * @author Ponfee
 * @param <K>
 * @param <V>
 */
public interface TypedMap<K, V> extends Map<K, V>, TypedDictionary<K, V> {

    default V getValue(K key) {
        return this.get(key);
    }

    default V removeKey(K key) {
        return this.remove(key);
    }

}
