package code.ponfee.commons;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Options template code
 * 
 * @author Ponfee
 */
public class Options {

    public static final Type<Boolean> BOOLEAN = new Type<>();
    public static final Type<Integer> INTEGER = new Type<>();
    public static final Type<Long> LONG = new Type<>();
    public static final Type<Double> DOUBLE = new Type<>();
    public static final Type<Float> FLOAT = new Type<>();
    public static final Type<String> STRING = new Type<>();

    private final Map<String, Object> options = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T option(Type<T> key) {
        return (T) options.get(MAPPER.get(key));
    }

    public <T> void option(Type<T> key, T value) {
        options.put(MAPPER.get(key), value);
    }

    public static class Type<T> {
        private Type() {}
    }

    private static final Map<Type<?>, String> MAPPER;
    static {
        ImmutableMap.Builder<Type<?>, String> builder = ImmutableMap.builder();
        try {
            for (Field field : Options.class.getDeclaredFields()) {
                int m = field.getModifiers();
                Object value;
                if (   Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m) 
                    && Type.class.isInstance(value = field.get(null))
                ) {
                    builder.put((Type<?>) value, field.getName());
                }
            }
        } catch (Exception e) {
            // cannot happend
            throw new AssertionError(e);
        }
        MAPPER = builder.build();
    }

}

