package code.ponfee.commons.ws.adapter.model;

import java.util.Map;

/**
 * 对应Map.Entry数据
 * 
 * @author Ponfee
 * @param <K>
 * @param <V>
 */
public class MapEntry<K, V> {
    private K key;
    private V value;

    public MapEntry() {
        this(null, null);
    }

    public MapEntry(Map.Entry<K, V> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public MapEntry(K key, V value) {
        super();
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
