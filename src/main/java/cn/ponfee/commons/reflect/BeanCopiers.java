/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.reflect;

import cn.ponfee.commons.util.SynchronizedCaches;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cglib.beans.BeanCopier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The bean copier utility based cglib
 * @see <a href="https://mapstruct.org/documentation/installation/">mapstruct官方文档</a>
 * 
 * @author Ponfee
 */
public class BeanCopiers {

    private static final Map<Pair<Class<?>, Class<?>>, BeanCopier> COPIER_CACHES = new HashMap<>();

    public static BeanCopier get(Class<?> sourceType, Class<?> targetType) {
        Pair<Class<?>, Class<?>> key = Pair.of(sourceType, targetType);
        return SynchronizedCaches.get(key, COPIER_CACHES, () -> BeanCopier.create(sourceType, targetType, false));
    }

    /**
     * Copy properties from source to target
     *
     * <pre>
     * 1、名称相同而类型不同的属性不会被拷贝
     * 2、注意：即使源类型是基本类型(int、short、char等)，目标类型是其包装类型(Integer、Short、Character等)，或反之，都不会被拷贝
     * 3、源类和目标类有相同的属性(两者的getter都存在)，但目标类的setter不存在：创建BeanCopier的时候抛异常
     * 4、当类型不一致是需要convert进行类型转换：org.springframework.cglib.core.Converter
     * 
     * BeanUtils/PropertyUtils -> commons-beanutils:commons-beanutils
     * </pre>
     * 
     * @param source source object
     * @param target target object
     * 
     * @see org.apache.commons.beanutils.BeanUtils#copyProperties(Object, Object);
     * @see org.apache.commons.beanutils.PropertyUtils#copyProperties(Object, Object);
     * @see org.springframework.beans.BeanUtils#copyProperties(Object, Object)
     * @see org.springframework.cglib.beans.BeanCopier#create(Class, Class, boolean)
     * @see mapstruct
     */
    public static void copy(Object source, Object target) {
        get(source.getClass(), target.getClass()).copy(source, target, null);
    }

    public static <T> T copy(Object source, Supplier<T> supplier) {
        T target = supplier.get();
        copy(source, target);
        return target;
    }

    public static <T> T copy(Object source, Class<T> targetType) {
        T target = ClassUtils.newInstance(targetType);
        copy(source, target);
        return target;
    }

}
