package code.ponfee.commons.model;

/**
 * Pair trait for get the value with typed
 * 
 * @author Ponfee
 * @param <K>
 * @param <V>
 */
public interface PairTrait<K, V> {

    V getValue(K key);

    V removeValue(K key);

    // --------------------------------------------------------string
    default String getRequireString(K key) {
        V value = getValue(key);
        if (value == null) {
            throw new IllegalArgumentException("Not presented value of '" + key + "'");
        }
        return value.toString();
    }

    default String getString(K key) {
        return getString(key, null);
    }

    default String getString(K key, String defaultVal) {
        V value = getValue(key);
        return value == null ? defaultVal : value.toString();
    }

    default String removeString(K key) {
        return removeString(key, null);
    }

    default String removeString(K key, String defaultVal) {
        V value = removeValue(key);
        return value == null ? defaultVal : value.toString();
    }

    // --------------------------------------------------------boolean
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

    default boolean removeBoolean(K key, boolean defaultValue) {
        Boolean value = removeBoolean(key);
        return value == null ? defaultValue : value;
    }

    default Boolean removeBoolean(K key) {
        String value = removeString(key);
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

    default int removeInteger(K key, int defaultValue) {
        Integer value = removeInteger(key);
        return value == null ? defaultValue : value;
    }

    default Integer removeInteger(K key) {
        String value = removeString(key);
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

    default long removeLong(K key, long defaultValue) {
        Long value = removeLong(key);
        return value == null ? defaultValue : value;
    }

    default Long removeLong(K key) {
        String value = removeString(key);
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

    default float removeFloat(K key, float defaultValue) {
        Float value = removeFloat(key);
        return value == null ? defaultValue : value;
    }

    default Float removeFloat(K key) {
        String value = removeString(key);
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

    default double removeDouble(K key, double defaultValue) {
        Double value = removeDouble(key);
        return value == null ? defaultValue : value;
    }

    default Double removeDouble(K key) {
        String value = removeString(key);
        return value == null ? null : Double.parseDouble(value);
    }

}
