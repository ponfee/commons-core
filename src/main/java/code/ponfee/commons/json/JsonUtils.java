package code.ponfee.commons.json;

import java.util.List;
import java.util.Map;

/**
 * JSON utility class
 * 
 * @author Ponfee
 */
public final class JsonUtils {

    /**
     * Returns <tt>true</tt> if this object value is complex json type(Object or Array)
     * 
     * @param value the value
     * @return {@code true} if the value is complex json type
     */
    public static boolean isComplexType(Object value) {
        if (value == null) {
            return false;
        }

        return value instanceof Map        // Object
            || value instanceof List       // Array
            || value.getClass().isArray(); // Array
    }

}
