package code.ponfee.commons.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;

/**
 * Extended {@link LinkedHashMap} with pre-defined get methods
 * 
 * @author Ponfee
 */
public class ExtendedLinkedMultiValueMap<K, V> extends LinkedMultiValueMap<K, V> implements PairTrait<K, V> {

    private static final long serialVersionUID = 4369022038293264189L;

    public ExtendedLinkedMultiValueMap() {
        super();
    }

    public ExtendedLinkedMultiValueMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ExtendedLinkedMultiValueMap(Map<K, List<V>> otherMap) {
        super(otherMap);
    }

    @Override
    public V getValue(K key) {
        return getFirst(key);
    }

    @Override
    public V removeValue(K key) {
        List<V> values = remove(key);
        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }

}
