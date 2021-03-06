package code.ponfee.commons.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Preconditions;

/**
 * 泛型工具类
 * 
 * https://segmentfault.com/a/1190000018319217
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

        Map<String, String> target = new HashMap<>(origin.size());
        for (Entry<String, ?> entry : origin.entrySet()) {
            target.put(entry.getKey(), Objects.toString(entry.getValue(), null));
        }
        return target;
    }

    // ----------------------------------------------------------------------------class actual type argument
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
     * public class GenericClass extends GenericSuperClass<Long,Integer,...,String> implements GenericInterface<String,Short,..,Long> {}
     * 
     * @param clazz
     * @param genericArgsIndex
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getActualTypeArgument(Class<?> clazz, int genericArgsIndex) {
        int index = 0;
        for (Type type : getGenericTypes(clazz)) {
            if (type instanceof ParameterizedType) {
                Type[] acts = ((ParameterizedType) type).getActualTypeArguments();
                if (acts.length + index < genericArgsIndex) {
                    index += acts.length;
                } else {
                    return getActualType(null, acts[genericArgsIndex - index]);
                }
            }
        }

        return (Class<T>) Object.class;
    }

    // ----------------------------------------------------------------------------method actual arg type argument
    public static <T> Class<T> getActualArgTypeArgument(Method method, int methodArgsIndex) {
        return getActualArgTypeArgument(method, methodArgsIndex, 0);
    }

    /**
     * public void genericMethod(List<Long> list, Map<String, String> map){}
     * 
     * @param method            方法对象
     * @param methodArgsIndex 方法参数索引号
     * @param genericArgsIndex  泛型参数索引号
     * @return
     */
    public static <T> Class<T> getActualArgTypeArgument(Method method, int methodArgsIndex, int genericArgsIndex) {
        return getActualTypeArgument(method.getGenericParameterTypes()[methodArgsIndex], genericArgsIndex);
    }

    // ----------------------------------------------------------------------------method actual return type argument
    public static <T> Class<T> getActualReturnTypeArgument(Method method) {
        return getActualReturnTypeArgument(method, 0);
    }

    /**
     * public List<String> genericMethod(){}
     * 
     * @param method the method
     * @param genericArgsIndex the generic argument index
     * @return
     */
    public static <T> Class<T> getActualReturnTypeArgument(Method method, int genericArgsIndex) {
        return getActualTypeArgument(method.getGenericReturnType(), genericArgsIndex);
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
    }

    // -------------------------------------------------------------------get actual variable type
    /**
     * public abstract class BaseEntity<I> {
     *   private I id;
     * }
     * 
     * public class BeanClass extends BaseEntity<String> {}
     * 
     * @param clazz the sub class
     * @param field the super class defined field
     * @return a Class of field actual type
     */
    public static <T> Class<T> getFieldActualType(Class<?> clazz, Field field) {
        return getActualType(clazz, field.getGenericType());
    }

    /**
     * Returns method arg actual type
     * 
     * public abstract class ClassA<T> {
     *   public void method(T arg) {}
     * }
     * 
     * public class ClassB extends classA<String>{}
     * 
     * @param clazz            the sub class
     * @param method           the super class defined method
     * @param methodArgsIndex  the method arg index
     * @return a Class of method arg actual type
     */
    public static <T> Class<T> getMethodArgActualType(Class<?> clazz, Method method, int methodArgsIndex) {
        return getActualType(clazz, method.getGenericParameterTypes()[methodArgsIndex]);
    }

    /**
     * Returns method return actual type
     * 
     * public abstract class ClassA<T> {
     *   public T method() {}
     * }
     * 
     * public class ClassB extends classA<String>{}
     * 
     * @param clazz  the sub class
     * @param method the super class defined method
     * @return a Class of method return actual type
     */
    public static <T> Class<T> getMethodReturnActualType(Class<?> clazz, Method method) {
        return getActualType(clazz, method.getGenericReturnType());
    }

    // public class ClassA extends ClassB<Map<U,V>> implements interfaceC<List<X>>, interfaceD<Y> {}
    public static List<Type> getGenericTypes(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return Collections.emptyList();
        }
        List<Type> types = new ArrayList<>();
        if (!clazz.isInterface()) {
            types.add(clazz.getGenericSuperclass()); // Map<U,V>
        }
        Collections.addAll(types, clazz.getGenericInterfaces()); // List<X>, Y
        return types;
    }

    public static Map<String, Class<?>> getActualTypeVariableMapping(Class<?> clazz) {
        Map<String, Class<?>> result = new HashMap<>();
        for (Type type : getGenericTypes(clazz)) {
            resolveMapping(result, type);
        }
        return result.isEmpty() ? Collections.emptyMap() : result;
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
            return getVariableActualType(clazz, (TypeVariable<?>) type);
        } else if (type instanceof WildcardType) {
            WildcardType wtype = (WildcardType) type;
            if (ArrayUtils.isNotEmpty(wtype.getLowerBounds())) {
                // 下限List<? super A>
                return getActualType(clazz, wtype.getLowerBounds()[0]);
            } else if (ArrayUtils.isNotEmpty(wtype.getUpperBounds())) {
                // 上限List<? extends A>
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
    private static <T> Class<T> getVariableActualType(Class<?> clazz, TypeVariable<?> var) {
        if (clazz == null) {
            return (Class<T>) Object.class;
        }

        Map<String, Class<?>> map = VARIABLE_TYPE_MAPPING.get(clazz);
        if (map == null) {
            synchronized (VARIABLE_TYPE_MAPPING) {
                if ((map = VARIABLE_TYPE_MAPPING.get(clazz)) == null) {
                    VARIABLE_TYPE_MAPPING.put(clazz, map = getActualTypeVariableMapping(clazz));
                }
            }
        }
        return (Class<T>) map.getOrDefault(getTypeVariableName(null, var).get(0), Object.class);
    }

    private static void resolveMapping(Map<String, Class<?>> result, Type type) {
        if (!(type instanceof ParameterizedType)) {
            return;
        }

        ParameterizedType ptype = (ParameterizedType) type;
        Class<?> rawType = (Class<?>) ptype.getRawType();
        // (Map<U,V>).getRawType() -> Map
        TypeVariable<?>[] vars = rawType.getTypeParameters();
        Type[] acts = ptype.getActualTypeArguments();
        for (int i = 0; i < acts.length; i++) {
            Class<?> varType = getActualType(null, acts[i]);
            getTypeVariableName(rawType, vars[i]).forEach(x -> result.put(x, varType));
            resolveMapping(result, acts[i]);
        }
    }

    private static List<String> getTypeVariableName(Class<?> clazz, TypeVariable<?> var) {
        List<String> names = new ArrayList<>();
        getTypeVariableName(names, clazz, var);
        return names;
    }

    // Class, Method, Constructor
    private static void getTypeVariableName(List<String> names, Class<?> clazz, TypeVariable<?> var) {
        names.add(var.getGenericDeclaration().toString() + "[" + var.getName() + "]");
        if (clazz == null || clazz == Object.class) {
            return;
        }
        for (Type type : getGenericTypes(clazz)) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }

            ParameterizedType ptype = (ParameterizedType) type;
            Type[] types = ptype.getActualTypeArguments();
            if (ArrayUtils.isEmpty(types)) {
                continue;
            }

            for (int i = 0; i < types.length; i++) {
                if (!(types[i] instanceof TypeVariable<?>)) {
                    continue;
                }
                // find the type variable origin difined class
                if (((TypeVariable<?>) types[i]).getName().equals(var.getTypeName())) {
                    clazz = (Class<?>) ptype.getRawType();
                    getTypeVariableName(names, clazz, clazz.getTypeParameters()[i]);
                    break;
                }
            }
        }
    }

}
