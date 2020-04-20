package code.ponfee.commons.util;

import java.util.Properties;

/**
 * Properties Utility
 *
 * @author Ponfee
 */
public final class PropertiesUtils {

    public static Properties filterProperties(Properties props, String prefixKey) {
        Properties properties = new Properties();
        int prefixLen = prefixKey.length();
        props.forEach((k, v) -> {
            String key = k.toString();
            if (key.startsWith(prefixKey)) {
                properties.put(key.substring(prefixLen, key.length()), v);
            }
        });
        return properties;
    }

}
