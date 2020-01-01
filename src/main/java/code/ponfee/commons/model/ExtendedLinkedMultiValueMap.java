package code.ponfee.commons.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.util.LinkedMultiValueMap;

import code.ponfee.commons.math.Numbers;

/**
 * Extended {@link LinkedHashMap} with pre-defined get methods
 * 
 * @author Ponfee
 */
public class ExtendedLinkedMultiValueMap<K, V> extends LinkedMultiValueMap<K, V> {

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

    public String getString(K key) {
        return getString(key, null);
    }

    public String getString(K key, String defaultVal) {
        return Objects.toString(getFirst(key), defaultVal);
    }

    public Boolean getBoolean(K key) {
        return Numbers.toWrapBoolean(getFirst(key));
    }

    public boolean getBoolean(K key, boolean defaultVal) {
        return Numbers.toBoolean(getFirst(key), defaultVal);
    }

    public Integer getInt(K key) {
        return Numbers.toWrapInt(getFirst(key));
    }

    public int getInt(K key, int defaultVal) {
        return Numbers.toInt(getFirst(key), defaultVal);
    }

    public Long getLong(K key) {
        return Numbers.toWrapLong(getFirst(key));
    }

    public long getLong(K key, long defaultVal) {
        return Numbers.toLong(getFirst(key), defaultVal);
    }

    public Float getFloat(K key) {
        return Numbers.toWrapFloat(getFirst(key));
    }

    public float getFloat(K key, float defaultVal) {
        return Numbers.toFloat(getFirst(key), defaultVal);
    }

    public Double getDouble(K key) {
        return Numbers.toWrapDouble(getFirst(key));
    }

    public double getDouble(K key, double defaultVal) {
        return Numbers.toDouble(getFirst(key), defaultVal);
    }

}
