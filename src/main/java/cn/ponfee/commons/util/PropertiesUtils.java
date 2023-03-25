/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import cn.ponfee.commons.reflect.ClassUtils;
import cn.ponfee.commons.reflect.Fields;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
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

    public static <T> T extract(Properties props, Class<T> beanType, String prefix, char... separators) {
        T bean = ClassUtils.newInstance(beanType);
        List<Field> fields = Objects.requireNonNull(ClassUtils.listFields(beanType));
        for (Field field : fields) {
            for (char separator : separators) {
                String name = prefix + Strings.toSeparatedName(field.getName(), separator);
                if (props.containsKey(name)) {
                    Fields.put(bean, field, ObjectUtils.cast(props.get(name), field.getType()));
                    break;
                }
            }
        }
        return bean;
    }

}
