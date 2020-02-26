package code.ponfee.commons.util;

import java.util.Properties;

/**
 * Properties utility
 * 
 * @author Ponfee
 */
public class PropertiesUtils {

    // --------------------------------------------------------------string
    public static String getRequireString(Properties props, String name) {
        if (props == null || !props.containsKey(name)) {
            throw new IllegalArgumentException("Not presented config entry: " + name);
        }
        return props.getProperty(name);
    }

    public static String getString(Properties props, String name) {
        return getString(props, name, null);
    }

    public static String getString(Properties props, String name, String defaultValue) {
        return props == null ? defaultValue : props.getProperty(name, defaultValue);
    }

    // --------------------------------------------------------------boolean
    public static Boolean getRequireBoolean(Properties props, String name) {
        return "true".equalsIgnoreCase(getRequireString(props, name));
    }

    public static boolean getBoolean(Properties props, String name, boolean defaultValue) {
        Boolean value = getBoolean(props, name);
        return value == null ? defaultValue : value;
    }

    public static Boolean getBoolean(Properties props, String name) {
        String value = getString(props, name);
        return value == null ? null : "true".equalsIgnoreCase(value);
    }

    // --------------------------------------------------------------int
    public static int getRequireInteger(Properties props, String name) {
        return Integer.parseInt(getRequireString(props, name));
    }

    public static int getInteger(Properties props, String name, int defaultValue) {
        Integer value = getInteger(props, name);
        return value == null ? defaultValue : value;
    }

    public static Integer getInteger(Properties props, String name) {
        String value = getString(props, name);
        return value == null ? null : Integer.parseInt(value);
    }

    // --------------------------------------------------------------long
    public static long getRequireLong(Properties props, String name) {
        return Long.parseLong(getRequireString(props, name));
    }

    public static long getLong(Properties props, String name, long defaultValue) {
        Long value = getLong(props, name);
        return value == null ? defaultValue : value;
    }

    public static Long getLong(Properties props, String name) {
        String value = getString(props, name);
        return value == null ? null : Long.parseLong(value);
    }

    // --------------------------------------------------------------float
    public static float getRequireFloat(Properties props, String name) {
        return Float.parseFloat(getRequireString(props, name));
    }

    public static float getFloat(Properties props, String name, float defaultValue) {
        Float value = getFloat(props, name);
        return value == null ? defaultValue : value;
    }

    public static Float getFloat(Properties props, String name) {
        String value = getString(props, name);
        return value == null ? null : Float.parseFloat(value);
    }

    // --------------------------------------------------------------double
    public static double getRequireDouble(Properties props, String name) {
        return Double.parseDouble(getRequireString(props, name));
    }

    public static double getDouble(Properties props, String name, double defaultValue) {
        Double value = getDouble(props, name);
        return value == null ? defaultValue : value;
    }

    public static Double getDouble(Properties props, String name) {
        String value = getString(props, name);
        return value == null ? null : Double.parseDouble(value);
    }

}
