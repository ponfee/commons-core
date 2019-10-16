package code.ponfee.commons.util;

import java.util.Properties;

/**
 * Properties utility
 * 
 * @author Ponfee
 */
public class PropertiesUtils {

    // --------------------------------------------------------------string
    public static String getString(Properties props, String name) {
        return getString(props, name, null);
    }

    public static String getString(Properties props, String name, String defaultValue) {
        return props == null ? null : props.getProperty(name, defaultValue);
    }

    // --------------------------------------------------------------boolean
    public static boolean getBoolean(Properties props, String name, boolean defaultValue) {
        Boolean value = getBoolean(props, name);
        return (value == null) ? defaultValue : value;
    }

    public static Boolean getBoolean(Properties props, String name) {
        String prop = getString(props, name);
        return (prop == null) ? null : "true".equalsIgnoreCase(prop);
    }

    // --------------------------------------------------------------integer
    public static int getInteger(Properties props, String name, int defaultValue) {
        Integer value = getInteger(props, name);
        return (value == null) ? defaultValue : value;
    }

    public static Integer getInteger(Properties props, String name) {
        String prop = getString(props, name);
        return props == null ? null : Integer.parseInt(prop);
    }

    // --------------------------------------------------------------long
    public static long getLong(Properties props, String name, long defaultValue) {
        Long value = getLong(props, name);
        return (value == null) ? defaultValue : value;
    }

    public static Long getLong(Properties props, String name) {
        String prop = getString(props, name);
        return props == null ? null : Long.parseLong(prop);
    }

    // --------------------------------------------------------------integer
    public static double getDouble(Properties props, String name, double defaultValue) {
        Double value = getDouble(props, name);
        return (value == null) ? defaultValue : value;
    }

    public static Double getDouble(Properties props, String name) {
        String prop = getString(props, name);
        return props == null ? null : Double.parseDouble(prop);
    }

}
