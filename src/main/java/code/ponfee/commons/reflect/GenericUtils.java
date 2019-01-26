package code.ponfee.commons.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import code.ponfee.commons.util.ObjectUtils;

/**
 * 泛型工具类
 * @author fupf
 */
public final class GenericUtils {
    private GenericUtils() {}

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
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getActualTypeArgument(final Class<?> clazz) {
        return getActualTypeArgument(clazz, 0);
    }

    /**
     * getActualTypeArgument(public class ResultPageMapNormalAdapter extends ResultPageMapAdapter<String, Integer>, 0) -> String.class
     * getActualTypeArgument(public class ResultPageMapNormalAdapter extends ResultPageMapAdapter<String, Integer>, 1) -> Integer.class
     * 获取泛型的实际类型参数
     * @param clazz
     * @param genericArgsIndex
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Class getActualTypeArgument(Class<?> clazz, int genericArgsIndex) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (genericArgsIndex >= params.length || genericArgsIndex < 0) {
            return Object.class;
        } else if (params[genericArgsIndex] instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) params[genericArgsIndex]).getRawType();
        } else if (params[genericArgsIndex] instanceof GenericArrayType) {
            Type type = ((GenericArrayType) params[genericArgsIndex]).getGenericComponentType();
            return Array.newInstance((Class<?>) ((ParameterizedType) type).getRawType(), 0).getClass();
        } else if (params[genericArgsIndex] instanceof Class<?>) {
            return (Class<?>) params[genericArgsIndex];
        } else {
            return Object.class;
        }
    }

    // ----------------------------------------------------------------------------
    public static Class<?> getActualTypeArgument(Method method, int methodParamsIndex) {
        return getActualTypeArgument(method, methodParamsIndex, 0);
    }

    /**
     * 获取泛型的实际类型参数
     * getActualTypeArgument(ClassUtils.class.getMethod("getField", Class.class, String.class), 0)  --> java.lang.Object
     * @param method            方法对象
     * @param methodParamsIndex 方法参数索引号
     * @param genericArgsIndex  泛型参数索引号
     * @return
     */
    public static Class<?> getActualTypeArgument(Method method, int methodParamsIndex, int genericArgsIndex) {
        Type type = method.getGenericParameterTypes()[methodParamsIndex];
        return getActualTypeArgument(((ParameterizedType) type).getActualTypeArguments()[genericArgsIndex]);
    }

    // ----------------------------------------------------------------------------
    public static Class<?> getActualTypeArgument(Field field) {
        return getActualTypeArgument(field, 0);
    }

    /**
     * private List<test.ClassA> data; -> test.ClassA
     * 获取泛型的实际类型参数
     * @param field
     * @param genericArgsIndex
     * @return
     */
    public static Class<?> getActualTypeArgument(Field field, int genericArgsIndex) {
        Type type = field.getGenericType();
        //type.getTypeName(); -> java.util.List<test.ClassA>
        //((ParameterizedType) type).getRawType(); -> interface java.util.List
        //((ParameterizedType) type).getOwnerType(); -> null
        return getActualTypeArgument(((ParameterizedType) type).getActualTypeArguments()[genericArgsIndex]);
    }

    public static String getFieldGenericType(Field field) {
        Type type = field.getGenericType();
        if (type == null) {
            return field.getType().getCanonicalName();
        }
        if (!(type instanceof ParameterizedType)) {
            return type.getTypeName();
        }

        ParameterizedType pt = (ParameterizedType) type;
        Type[] types = pt.getActualTypeArguments();
        StringBuilder builder = new StringBuilder();
        builder.append(((Class<?>) pt.getRawType()).getCanonicalName()).append("<");
        for (int len = types.length, i = 0; i < len; i++) {
            builder.append(types[i].getTypeName());
            if (i != len - 1) {
                builder.append(",");
            }
        }
        return builder.append(">").toString();
    }

    // --------------------------------------private methods----------------------------------
    private static Class<?> getActualTypeArgument(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof WildcardType) {
            WildcardType wType = (WildcardType) type;
            if (!ObjectUtils.isEmpty(wType.getLowerBounds())) { // List<? super A>
                return (Class<?>) wType.getLowerBounds()[0];
            } else if (!ObjectUtils.isEmpty(wType.getUpperBounds())) { // List<? extends A>
                return (Class<?>) wType.getUpperBounds()[0];
            } else { // List<?>
                return Object.class;
            }
        } else {
            return Object.class;
        }
    }

}
