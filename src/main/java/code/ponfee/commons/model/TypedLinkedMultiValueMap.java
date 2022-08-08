package code.ponfee.commons.model;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * Typed {@link LinkedMultiValueMap} with pre-defined get methods
 * 
 * @author Ponfee
 */
public class TypedLinkedMultiValueMap<K, V> extends LinkedMultiValueMap<K, V> implements TypedKeyValue<K, V> {

    private static final long serialVersionUID = 4369022038293264189L;

    public TypedLinkedMultiValueMap() {
        super();
    }

    public TypedLinkedMultiValueMap(int initialCapacity) {
        super(initialCapacity);
    }

    public TypedLinkedMultiValueMap(Map<K, List<V>> otherMap) {
        super(otherMap);
    }

    @Override
    public V getValue(K key) {
        return getFirst(key);
    }

    @Override
    public V removeKey(K key) {
        List<V> values = remove(key);
        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }

}
