/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

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
