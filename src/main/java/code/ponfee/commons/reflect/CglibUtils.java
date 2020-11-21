package code.ponfee.commons.reflect;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cglib.beans.BeanCopier;

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
        ObjectArrayWrapper<Class<?>> beanKey = ObjectArrayWrapper.of(sclass, tclass);
        BeanCopier copier = COPIER_CACHE.get(beanKey);
        if (copier == null) {
            synchronized (COPIER_CACHE) {
                if ((copier = COPIER_CACHE.get(beanKey)) == null) {
                    copier = BeanCopier.create(sclass, tclass, false);
                    COPIER_CACHE.put(beanKey, copier);
                }
            }
        }
        copier.copy(source, target, null);
    }

}
