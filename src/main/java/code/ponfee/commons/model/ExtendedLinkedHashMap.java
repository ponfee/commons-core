package code.ponfee.commons.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extended {@link LinkedHashMap} with pre-defined get methods
 * 
 * @author Ponfee
 */
public class ExtendedLinkedHashMap<K, V> extends LinkedHashMap<K, V> implements MapTrait<K, V> {

    private static final long serialVersionUID = -4207327688392334942L;

    public ExtendedLinkedHashMap() {
        super();
    }

    public ExtendedLinkedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ExtendedLinkedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    @Override
    public V getValue(K key) {
        return get(key);
    }

}
