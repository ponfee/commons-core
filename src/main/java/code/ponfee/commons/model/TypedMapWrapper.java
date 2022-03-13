package code.ponfee.commons.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapped the {@link Map} for with gets typed value trait
 *
 * @param <K>
 * @param <V>
 * @author Ponfee
 */
public class TypedMapWrapper<K, V> implements TypedMap<K, V>, Serializable, Cloneable {

    private static final long serialVersionUID = 6899012847958938043L;

    private final Map<K, V> target;

    public TypedMapWrapper(Map<K, V> otherMap) {
        this.target = otherMap == null ? Collections.emptyMap() : otherMap;
    }

    @Override
    public int size() {
        return this.target.size();
    }

    @Override
    public boolean isEmpty() {
        return this.target.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.target.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.target.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.target.get(key);
    }

    @Override
    public V put(K key, V value) {
        return this.target.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.target.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.target.putAll(m);
    }

    @Override
    public void clear() {
        this.target.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.target.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.target.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.target.entrySet();
    }

    @Override
    public boolean equals(Object obj) {
        return this.target.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.target.hashCode();
    }

    @Override
    public String toString() {
        return this.target.toString();
    }

    public static <K, V> TypedMapWrapper<K, V> empty() {
        return new TypedMapWrapper<>(Collections.emptyMap());
    }

    @Override
    public TypedMapWrapper<K, V> clone() {
        return new TypedMapWrapper<>(this.target);
    }

    public TypedMapWrapper<K, V> copy() {
        return new TypedMapWrapper<>(target.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    }

}
