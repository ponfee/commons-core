/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.reflect;

import cn.ponfee.commons.util.ObjectUtils;
import cn.ponfee.commons.util.SynchronizedCaches;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cglib.beans.BeanMap;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.CaseFormat.*;

/**
 * Utility of Java Bean and Map mutual conversion
 *
 * @author Ponfee
 */
public enum BeanMaps {

    /**
     * Based Cglib
     */
    CGLIB() {
        @Override @SuppressWarnings("unchecked")
        public Map<String, Object> toMap(Object bean) {
            if (bean == null) {
                return null;
            }
            return BeanMap.create(bean);
        }

        @Override
        public void copyFromMap(Map<String, Object> sourceMap, Object targetBean) {
            BeanMap.create(targetBean).putAll(sourceMap);
        }
    },

    /**
     * Based Unsafe class
     */
    FIELDS() {
        private final Map<Class<?>, List<Field>> cachedFields = new HashMap<>();

        @Override
        public Map<String, Object> toMap(Object bean) {
            if (bean == null) {
                return null;
            }
            List<Field> fields = getFields(bean.getClass());
            Map<String, Object> map = new HashMap<>(fields.size());
            fields.forEach(f -> map.put(f.getName(), Fields.get(bean, f)));
            return map;
        }

        @Override
        public void copyFromMap(Map<String, Object> sourceMap, Object targetBean) {
            Class<?> clazz = targetBean.getClass();
            for (Field field : getFields(clazz)) {
                String name = field.getName();
                if (sourceMap.containsKey(name)) {
                    Class<?> type = GenericUtils.getFieldActualType(clazz, field);
                    Fields.put(targetBean, field, ObjectUtils.cast(sourceMap.get(name), type));
                }
            }
        }

        private List<Field> getFields(Class<?> beanType) {
            return SynchronizedCaches.get(beanType, cachedFields, type -> {
                List<Field> list = ClassUtils.listFields(type);
                return CollectionUtils.isEmpty(list) ? Collections.emptyList() : ImmutableList.copyOf(list);
            });
        }
    },

    /**
     * Based java.beans.Introspector
     */
    PROPS() {
        @Override
        public Map<String, Object> toMap(Object bean) {
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
                Map<String, Object> map = new HashMap<>();
                String name;
                for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors()) {
                    if (!"class".equals((name = prop.getName()))) { // getClass()
                        map.put(name, prop.getReadMethod().invoke(bean));
                    }
                }
                return map;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void copyFromMap(Map<String, Object> sourceMap, Object targetBean) {
            String name;
            Object value;
            Class<?> type;
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(targetBean.getClass());
                for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors()) {
                    if ("class".equals(name = prop.getName())) { // exclude getClass()
                        continue;
                    }

                    String name0 = name;
                    if (   !sourceMap.containsKey(name)
                        && !sourceMap.containsKey(name = LOWER_CAMEL.to(LOWER_UNDERSCORE, name0))
                        && !sourceMap.containsKey(name = LOWER_CAMEL.to(LOWER_HYPHEN, name0))
                    ) {
                        continue;
                    }

                    value = sourceMap.get(name);
                    if ((type = prop.getPropertyType()).isPrimitive() && ObjectUtils.isEmpty(value)) {
                        continue; // 基本类型时：value为null或为空字符串时跳过
                    }

                    // set value into bean field
                    prop.getWriteMethod().invoke(targetBean, ObjectUtils.cast(value, type));
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    };

    /**
     * Returns a map from bean field-value
     *
     * @param bean the bean object
     * @return a map
     */
    public abstract Map<String, Object> toMap(Object bean);

    /**
     * Copies map key-value to bean object field-value
     *
     * @param sourceMap  the source map
     * @param targetBean the target bean
     */
    public abstract void copyFromMap(Map<String, Object> sourceMap, Object targetBean);

    /**
     * Returns a bean object of specified Class<T> instance,
     * and copy map key-value to bean object field-value
     *
     * @param map      the map
     * @param beanType the type, must be has no args default constructor
     * @return a bean object of specified Class<T> instance
     */
    public final <T> T toBean(Map<String, Object> map, Class<T> beanType) {
        T bean = ObjectUtils.newInstance(beanType);
        this.copyFromMap(map, bean);
        return bean;
    }

    /**
     * Copies bean object field-value to map key-value
     *
     * @param sourceBean the source bean
     * @param targetMap  the target map
     */
    public final void copyFromBean(Object sourceBean, Map<String, Object> targetMap) {
        targetMap.putAll(this.toMap(sourceBean));
    }

}
