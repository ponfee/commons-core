/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.EnumUtils;

import java.util.Map;
import java.util.function.Function;

/**
 * Enum utility
 * 
 * @author Ponfee
 */
public class Enums {

    /**
     * Gets the {@code Map} of enums by name.
     *
     * @param enumType the enum type
     * @param <E>      map key mapper
     * @return the immutable map of enum to map enums, never null
     * @see EnumUtils#getEnumMap(Class)
     */
    public static <E extends Enum<E>> Map<String, E> toMap(Class<E> enumType) {
        return toMap(enumType, Enum::name);
    }

    /**
     * Returns {@code Map} of enum
     *
     * @param enumType  the enum type
     * @param keyMapper map key mapper
     * @param <K>       then map key type
     * @param <E>       the enum type
     * @return the immutable map of enum to map enums, never null
     */
    public static <K, E extends Enum<E>> Map<K, E> toMap(Class<E> enumType, Function<E, K> keyMapper) {
        E[] enumConstants = enumType.getEnumConstants();
        ImmutableMap.Builder<K, E> mapping = ImmutableMap.builderWithExpectedSize(enumConstants.length << 1);
        for (final E e: enumConstants) {
            mapping.put(keyMapper.apply(e), e);
        }
        return mapping.build();
    }
}
