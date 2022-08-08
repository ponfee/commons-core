package code.ponfee.commons.util;

import java.util.Properties;

/**
 * Properties Utility
 *
 * @author Ponfee
 */
public final class PropertiesUtils {

    public static Properties filterProperties(Properties props, String keyPrefix) {
        Properties properties = new Properties();
        int prefixLen = keyPrefix.length();
        props.forEach((k, v) -> {
            String key = k.toString();
            if (key.startsWith(keyPrefix)) {
                properties.put(key.substring(prefixLen), v);
            }
        });
        return properties;
    }

}
