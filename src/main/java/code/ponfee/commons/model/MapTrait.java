package code.ponfee.commons.model;

/**
 * Extended Map
 * 
 * @author 01367825
 * @param <K>
 * @param <V>
 */
public interface MapTrait<K, V> {

    V getValue(K key);

    // --------------------------------------------------------string
    default String getRequireString(K key) {
        V value = getValue(key);
        if (value == null) {
            throw new IllegalArgumentException("Not exists: " + key);
        }
        return value.toString();
    }

    default String getString(K key, String defaultVal) {
        V value = getValue(key);
        return value == null ? defaultVal : value.toString();
    }

    default String getString(K key) {
        V value = getValue(key);
        return value == null ? null : value.toString();
    }

    // --------------------------------------------------------string
    default boolean getRequireBoolean(K key) {
        String value = getRequireString(key);
        if (value == null) {
            throw new IllegalArgumentException("Boolean value cannot be null.");
        }

        switch (value) {
            case "TRUE" : case "True" : case "true" : return true;
            case "FALSE": case "False": case "false": return false;
            default: throw new IllegalArgumentException("Invalid boolean value: " + value);
        }
    }

    default boolean getBoolean(K key, boolean defaultValue) {
        Boolean value = getBoolean(key);
        return value == null ? defaultValue : value;
    }

    default Boolean getBoolean(K key) {
        String value = getString(key);
        return value == null ? null : "true".equalsIgnoreCase(value);
    }

    // --------------------------------------------------------------int
    default int getRequireInteger(K key) {
        return Integer.parseInt(getRequireString(key));
    }

    default int getInteger(K key, int defaultValue) {
        Integer value = getInteger(key);
        return value == null ? defaultValue : value;
    }

    default Integer getInteger(K key) {
        String value = getString(key);
        return value == null ? null : Integer.parseInt(value);
    }

    // --------------------------------------------------------------long
    default long getRequireLong(K key) {
        return Long.parseLong(getRequireString(key));
    }

    default long getLong(K key, long defaultValue) {
        Long value = getLong(key);
        return value == null ? defaultValue : value;
    }

    default Long getLong(K key) {
        String value = getString(key);
        return value == null ? null : Long.parseLong(value);
    }

    // --------------------------------------------------------------float
    default float getRequireFloat(K key) {
        return Float.parseFloat(getRequireString(key));
    }

    default float getFloat(K key, float defaultValue) {
        Float value = getFloat(key);
        return value == null ? defaultValue : value;
    }

    default Float getFloat(K key) {
        String value = getString(key);
        return value == null ? null : Float.parseFloat(value);
    }

    // --------------------------------------------------------------double
    default double getRequireDouble(K key) {
        return Double.parseDouble(getRequireString(key));
    }

    default double getDouble(K key, double defaultValue) {
        Double value = getDouble(key);
        return value == null ? defaultValue : value;
    }

    default Double getDouble(K key) {
        String value = getString(key);
        return value == null ? null : Double.parseDouble(value);
    }

}
