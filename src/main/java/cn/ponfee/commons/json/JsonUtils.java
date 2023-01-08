/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.json;

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
            || value instanceof List       // List
            || value.getClass().isArray(); // Array
    }

}
