package code.ponfee.commons.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Extended {@link HashMap} with pre-defined get methods
 * 
 * @author Ponfee
 */
public class ExtendedHashMap<K, V> extends HashMap<K, V> implements MapTrait<K, V> {

    private static final long serialVersionUID = -4207327688392334942L;

    public ExtendedHashMap() {
        super();
    }

    public ExtendedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ExtendedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

}
