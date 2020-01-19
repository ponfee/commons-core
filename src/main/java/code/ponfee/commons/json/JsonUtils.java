package code.ponfee.commons.json;

import java.util.List;
import java.util.Map;

/**
 * JSON utility class
 * 
 * @author Ponfee
 */
public final class JsonUtils {

    public static boolean isComplexType(Object value) {
        if (value == null) {
            return false;
        }

        return value instanceof Map 
            || value instanceof List 
            || value.getClass().isArray();
    }

}
