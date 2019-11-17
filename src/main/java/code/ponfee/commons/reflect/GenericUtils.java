package code.ponfee.commons.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Preconditions;

/**
 * 泛型工具类
 * 
 * @author Ponfee
 */
public final class GenericUtils {

    private static final Map<Class<?>, Map<String, Class<?>>> VARIABLE_TYPE_MAPPING = new HashMap<>();

    /**
     * map泛型协变
     * @param origin
     * @return
     */
    public static Map<String, String> covariant(Map<String, ?> origin) {
        if (origin == null) {
            return null;
        }

        Map<String, String> target = new HashMap<>();
        for (Entry<String, ?> entry : origin.entrySet()) {
            target.put(entry.getKey(), Objects.toString(entry.getValue(), null));
        }
        return target;
    }

    // ----------------------------------------------------------------------------
    /**
     * 获取泛型的实际类型参数
     * 
     * @param clazz
     * @return
     */
    public static <T> Class<T> getActualTypeArgument(Class<?> clazz) {
        return getActualTypeArgument(clazz, 0);
    }

    /**
     * public class GenericClass extends GenericSuperClass<A,B,...,N> {}
     * 
     * @param clazz
     * @param genericArgsIndex
     * @return
     */
    public static <T> Class<T> getActualTypeArgument(Class<?> clazz, int genericArgsIndex) {
        return getActualTypeArgument(clazz.getGenericSuperclass(), genericArgsIndex);
    }

    // ----------------------------------------------------------------------------
    public static <T> Class<T> getActualTypeArgument(Method method, int methodArgsIndex) {
        return getActualTypeArgument(method, methodArgsIndex, 0);
    }

    /**
     * public void genericMethod(List<A> list, Map<B, C> map){}
     * 
     * @param method            方法对象
     * @param methodArgsIndex 方法参数索引号
     * @param genericArgsIndex  泛型参数索引号
     * @return
     */
    public static <T> Class<T> getActualTypeArgument(Method method, int methodArgsIndex, int genericArgsIndex) {
        return getActualTypeArgument(method.getGenericParameterTypes()[methodArgsIndex], genericArgsIndex);
    }

    // ----------------------------------------------------------------------------
    public static <T> Class<T> getActualTypeArgument(Field field) {
        return getActualTypeArgument(field, 0);
    }

    /**
     * private List<Long> list; -> Long
     * 
     * @param field            the class field
     * @param genericArgsIndex the genericArgsIndex
     * @return
     */
    public static <T> Class<T> getActualTypeArgument(Field field, int genericArgsIndex) {
        return getActualTypeArgument(field.getGenericType(), genericArgsIndex);
        // type.getTypeName();                         ->  java.util.List<test.ClassA>
        // ((ParameterizedType) type).getRawType();    ->  interface java.util.List
        // ((ParameterizedType) type).getOwnerType();  ->  null
    }

    // -------------------------------------------------------------------get actual variable type
    /**
     * public class BeanClass extends BaseEntity<String> {}
     * 
     * @param clazz the defined class
     * @param field the field
     * @return a Class of field actual type
     */
    public static <T> Class<T> getFieldActualType(Class<?> clazz, Field field) {
        return getActualType(clazz, field.getGenericType());
    }

    /**
     * Returns method arg actual type
     * 
     * @param clazz            the defined this method class 
     * @param method           the method
     * @param methodArgsIndex  the method arg index
     * @return a Class of method arg actual type
     */
    public static <T> Class<T> getMethodArgActualType(Class<?> clazz, Method method, int methodArgsIndex) {
        return getActualType(clazz, method.getGenericParameterTypes()[methodArgsIndex]);
    }

    /**
     * Returns method return actual type
     * 
     * @param clazz  the defined this method class 
     * @param method the method
     * @return a Class of method return actual type
     */
    public static <T> Class<T> getMethodReturnActualType(Class<?> clazz, Method method) {
        return getActualType(clazz, method.getGenericReturnType());
    }

    // -------------------------------------------------------------------private methods
    @SuppressWarnings("unchecked")
    private static <T> Class<T> getActualTypeArgument(Type type, int genericArgsIndex) {
        Preconditions.checkArgument(genericArgsIndex >= 0, "Generic args index cannot be negative.");
        if (!(type instanceof ParameterizedType)) {
            return (Class<T>) Object.class;
        }

        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        return genericArgsIndex >= types.length 
             ? (Class<T>) Object.class 
             : getActualType(null, types[genericArgsIndex]);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getActualType(Class<?> clazz, Type type) {
        if (type instanceof Class<?>) {
            return (Class<T>) type;
        } else if (type instanceof ParameterizedType) {
            // List<E> -> java.util.List
            return getActualType(clazz, ((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) { // E[]
            Type etype = ((GenericArrayType) type).getGenericComponentType(); // E: element type
            return (Class<T>) Array.newInstance(getActualType(clazz, etype), 0).getClass();
        } else if (type instanceof TypeVariable) {
            return (Class<T>) (clazz == null ? Object.class : getVariableActualType(clazz, ((TypeVariable<?>) type).getName()));
        } else if (type instanceof WildcardType) {
            WildcardType wtype = (WildcardType) type;
            if (ArrayUtils.isNotEmpty(wtype.getLowerBounds())) {
                // List<? super A>
                return getActualType(clazz, wtype.getLowerBounds()[0]);
            } else if (ArrayUtils.isNotEmpty(wtype.getUpperBounds())) {
                // List<? extends A>
                return getActualType(clazz, wtype.getUpperBounds()[0]);
            } else {
                // List<?>
                return (Class<T>) Object.class;
            }
        } else {
            return (Class<T>) Object.class;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getVariableActualType(Class<?> clazz, String variableName) {
        Map<String, Class<?>> map = (Map<String, Class<?>>) VARIABLE_TYPE_MAPPING.get(clazz);
        if (map == null) {
            synchronized (VARIABLE_TYPE_MAPPING) {
                if ((map = (Map<String, Class<?>>) VARIABLE_TYPE_MAPPING.get(clazz)) == null) {
                    VARIABLE_TYPE_MAPPING.put(clazz, map = initVariableActualTypeMapping(clazz));
                }
            }
        }
        return (Class<T>) map.getOrDefault(variableName, Object.class);
    }

    private static Map<String, Class<?>> initVariableActualTypeMapping(Class<?> clazz) {
        Type gtype = clazz.getGenericSuperclass();
        if (!(gtype instanceof ParameterizedType)) {
            return Collections.emptyMap();
        }

        ParameterizedType ptype = (ParameterizedType) gtype;
        TypeVariable<?>[] vars = ((Class<?>) ptype.getRawType()).getTypeParameters();
        Type[] acts = ptype.getActualTypeArguments();
        Map<String, Class<?>> result = new HashMap<>();
        for (int i = 0; i < acts.length; i++) {
            result.put(vars[i].getName(), getActualType(null, acts[i]));
        }
        return result;
    }

}
