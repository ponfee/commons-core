package code.ponfee.commons.util;

import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;

/**
 * Enum utility
 * 
 * @author Ponfee
 */
public class Enums {

    public static <E extends Enum<E>> E of(Class<E> type, String name) {
        return of(type, name, null);
    }

    public static <E extends Enum<E>> E of(Class<E> type, String name, E defaultVal) {
        return Optional.ofNullable(EnumUtils.getEnum(type, name)).orElse(defaultVal);
    }

    public static <E extends Enum<E>> E ofIgnoreCase(Class<E> type, String name) {
        return ofIgnoreCase(type, name, null);
    }

    public static <E extends Enum<E>> E ofIgnoreCase(Class<E> type, String name, E defaultVal) {
        return Optional.ofNullable(EnumUtils.getEnumIgnoreCase(type, name)).orElse(defaultVal);
    }

}
