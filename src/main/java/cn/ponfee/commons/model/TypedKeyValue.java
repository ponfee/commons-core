/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.model;

import cn.ponfee.commons.math.Numbers;

import java.util.Objects;
import java.util.function.Function;

/**
 * Get the value with typed for dictionary key-value
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Ponfee
 */
public interface TypedKeyValue<K, V> {

    V getValue(K key);

    default boolean hasKey(K key) {
        return getValue(key) != null;
    }

    // --------------------------------------------------------object

    default V getRequired(K key) {
        return getRequired(key, Function.identity());
    }

    default V get(K key, V defaultVal) {
        V value = getValue(key);
        return value == null ? defaultVal : value;
    }

    // --------------------------------------------------------string
    default String getRequiredString(K key) {
        return getRequired(key, Object::toString);
    }

    default String getString(K key) {
        return getString(key, null);
    }

    default String getString(K key, String defaultVal) {
        return Objects.toString(getValue(key), defaultVal);
    }

    // --------------------------------------------------------boolean
    default boolean getRequiredBoolean(K key) {
        Object value = getValue(key);
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        if (value == null) {
            throw new IllegalArgumentException("Not presented value of '" + key + "'");
        }
        switch (value.toString()) {
            case "TRUE" : case "True" : case "true" : return true;
            case "FALSE": case "False": case "false": return false;
            default: throw new IllegalArgumentException("Invalid boolean value: " + value);
        }
    }

    default boolean getBoolean(K key, boolean defaultValue) {
        return Numbers.toBoolean(getValue(key), defaultValue);
    }

    default Boolean getBoolean(K key) {
        return Numbers.toWrapBoolean(getValue(key));
    }

    // --------------------------------------------------------------int
    default int getRequiredInt(K key) {
        return getRequired(key, Numbers::toInt);
    }

    default int getInt(K key, int defaultValue) {
        return Numbers.toInt(getValue(key), defaultValue);
    }

    default Integer getInt(K key) {
        return Numbers.toWrapInt(getValue(key));
    }

    // --------------------------------------------------------------long
    default long getRequiredLong(K key) {
        return getRequired(key, Numbers::toLong);
    }

    default long getLong(K key, long defaultValue) {
        return Numbers.toLong(getValue(key), defaultValue);
    }

    default Long getLong(K key) {
        return Numbers.toWrapLong(getValue(key));
    }

    // --------------------------------------------------------------float
    default float getRequiredFloat(K key) {
        return getRequired(key, Numbers::toFloat);
    }

    default float getFloat(K key, float defaultValue) {
        return Numbers.toFloat(getValue(key), defaultValue);
    }

    default Float getFloat(K key) {
        return Numbers.toWrapFloat(getValue(key));
    }

    // --------------------------------------------------------------double
    default double getRequiredDouble(K key) {
        return getRequired(key, Numbers::toDouble);
    }

    default double getDouble(K key, double defaultValue) {
        return Numbers.toDouble(getValue(key), defaultValue);

    }

    default Double getDouble(K key) {
        return Numbers.toWrapDouble(getValue(key));
    }

    // ---------------------------------------------------- methods
    default <R> R getRequired(K key, Function<V, R> mapper) {
        V value = getValue(key);
        if (value == null) {
            throw new IllegalArgumentException("Not presented value of '" + key + "'");
        }
        return mapper.apply(value);
    }

}
