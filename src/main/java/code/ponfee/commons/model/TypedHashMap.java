package code.ponfee.commons.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Typed {@link HashMap} with pre-defined get methods
 * 
 * @author Ponfee
 */
public class TypedHashMap<K, V> extends HashMap<K, V> implements TypedMap<K, V> {

    private static final long serialVersionUID = -4207327688392334942L;

    public TypedHashMap() {
        super();
    }

    public TypedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public TypedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

}
