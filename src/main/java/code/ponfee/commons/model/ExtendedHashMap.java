package code.ponfee.commons.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import code.ponfee.commons.math.Numbers;

/**
 * Extended {@link HashMap} with pre-defined get methods
 * 
 * @author Ponfee
 */
public class ExtendedHashMap<K, V> extends HashMap<K, V> {

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

    public String getString(K key) {
        return getString(key, null);
    }

    public String getString(K key, String defaultVal) {
        return Objects.toString(get(key), defaultVal);
    }

    public Boolean getBoolean(K key) {
        return Numbers.toWrapBoolean(get(key));
    }

    public boolean getBoolean(K key, boolean defaultVal) {
        return Numbers.toBoolean(get(key), defaultVal);
    }

    public Integer getInt(K key) {
        return Numbers.toWrapInt(get(key));
    }

    public int getInt(K key, int defaultVal) {
        return Numbers.toInt(get(key), defaultVal);
    }

    public Long getLong(K key) {
        return Numbers.toWrapLong(get(key));
    }

    public long getLong(K key, long defaultVal) {
        return Numbers.toLong(get(key), defaultVal);
    }

    public Float getFloat(K key) {
        return Numbers.toWrapFloat(get(key));
    }

    public float getFloat(K key, float defaultVal) {
        return Numbers.toFloat(get(key), defaultVal);
    }

    public Double getDouble(K key) {
        return Numbers.toWrapDouble(get(key));
    }

    public double getDouble(K key, double defaultVal) {
        return Numbers.toDouble(get(key), defaultVal);
    }

}
