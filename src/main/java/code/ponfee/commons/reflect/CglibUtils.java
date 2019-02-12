package code.ponfee.commons.reflect;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.beans.BeanMap;

import code.ponfee.commons.collect.ObjectArrayWrapper;

/**
 * The utility class based cglib
 * 
 * @author Ponfee
 */
public class CglibUtils {

    private static final Map<ObjectArrayWrapper<Class<?>>, BeanCopier> COPIER_CACHE = new HashMap<>();

    /**
     * 1、名称相同而类型不同的属性不会被拷贝
     * 2、注意：即使源类型是原始类型(int、short、char等)，目标类型是其包装类型(Integer、Short、Character等)，或反之，都不会被拷贝
     * 3、源类和目标类有相同的属性(两者的getter都存在)，但目标类的setter不存在：创建BeanCopier的时候抛异常
     * 4、当类型不一致是需要convert进行类型转换：org.springframework.cglib.core.Converter
     * 
     * Copy properties from source to target
     * 
     * @param source source object
     * @param target target object
     */
    public static void copyProperties(Object source, Object target) {
        /*Long beanKey = ((long) System.identityHashCode(source.getClass()) << 32) 
                       | System.identityHashCode(target.getClass());*/
        Class<?> sclass = source.getClass(), tclass = target.getClass();
        ObjectArrayWrapper<Class<?>> beanKey = ObjectArrayWrapper.create(
            sclass, tclass
        );
        BeanCopier copier = COPIER_CACHE.get(beanKey);
        if (copier == null) {
            synchronized (COPIER_CACHE) {
                copier = COPIER_CACHE.get(beanKey);
                if (copier == null) {
                    copier = BeanCopier.create(sclass, tclass, false);
                    COPIER_CACHE.put(beanKey, copier);
                }
            }
        }
        copier.copy(source, target, null);
    }

    /**
     * Returns a map of bean field-value
     * 
     * @param bean the bean object
     * @return a map
     */
    public static Map<String, Object> bean2map(Object bean) {
        if (bean == null) {
            return null;
        }

        BeanMap beanMap = BeanMap.create(bean);
        Map<String, Object> map = new HashMap<>(beanMap.size());
        for (Object key : beanMap.keySet()) {
            map.put(String.valueOf(key), beanMap.get(key));
        }
        return map;
    }

    /**
     * Copy map key-value to bean object field-value
     * 
     * @param map  the map
     * @param bean the bean
     */
    public static void map2bean(Map<String, Object> map, Object bean) {
        BeanMap.create(bean).putAll(map);
    }

    /**
     * Returns a bean object of specified Class<T> instance,
     * and copy map key-value to bean object field-value
     * 
     * @param map  the map
     * @param type the type, must be has no args default constructor
     * @return a bean object of specified Class<T> instance
     */
    public static <T> T map2bean(Map<String, Object> map, Class<T> type) {
        try {
            T bean = type.getConstructor().newInstance();
            map2bean(map, bean);
            return bean;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
