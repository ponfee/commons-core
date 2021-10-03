package code.ponfee.commons.reflect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import code.ponfee.commons.base.tuple.Tuple2;
import org.springframework.cglib.beans.BeanCopier;

/**
 * <p>The bean copier utility based cglib</p>
 * @see <a href="https://mapstruct.org/documentation/installation/">mapstruct官方文档</a>
 * 
 * @author Ponfee
 */
public class BeanCopiers {

    private static final Map<Tuple2<Class<?>, Class<?>>, BeanCopier> COPIER_CACHES = new HashMap<>();

    /**
     * Copy properties from source to target
     *
     * <pre>
     * 1、名称相同而类型不同的属性不会被拷贝
     * 2、注意：即使源类型是基本类型(int、short、char等)，目标类型是其包装类型(Integer、Short、Character等)，或反之，都不会被拷贝
     * 3、源类和目标类有相同的属性(两者的getter都存在)，但目标类的setter不存在：创建BeanCopier的时候抛异常
     * 4、当类型不一致是需要convert进行类型转换：org.springframework.cglib.core.Converter
     * </pre>
     * <p>BeanUtils/PropertyUtils -> commons-beanutils:commons-beanutils</p>
     * 
     * @param source source object
     * @param target target object
     * 
     * @see org.apache.commons.beanutils.BeanUtils#copyProperties(Object, Object);
     * @see org.apache.commons.beanutils.PropertyUtils#copyProperties(Object, Object);
     * @see org.springframework.beans.BeanUtils#copyProperties(Object, Object)
     * @see org.springframework.cglib.beans.BeanCopier#create(Class, Class, boolean)
     */
    public static void copyProperties(Object source, Object target) {
        Class<?> sclass = source.getClass(), tclass = target.getClass();
        //Long beanKey = ((long) System.identityHashCode(sclass) << 32) | System.identityHashCode(tclass);
        Tuple2<Class<?>, Class<?>> beanKey = Tuple2.of(sclass, tclass);
        BeanCopier copier = COPIER_CACHES.get(beanKey);
        if (copier == null) {
            synchronized (COPIER_CACHES) {
                if ((copier = COPIER_CACHES.get(beanKey)) == null) {
                    copier = BeanCopier.create(sclass, tclass, false);
                    COPIER_CACHES.put(beanKey, copier);
                }
            }
        }
        copier.copy(source, target, null);
    }

    public static <T> T copyProperties(Object source, Supplier<T> supplier) {
        T target = supplier.get();
        copyProperties(source, target);
        return target;
    }

    public static <T> T copyProperties(Object source, Class<T> targetType) {
        T target = ClassUtils.newInstance(targetType);
        copyProperties(source, target);
        return target;
    }

}
