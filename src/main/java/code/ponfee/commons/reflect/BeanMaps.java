package code.ponfee.commons.reflect;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cglib.beans.BeanMap;

import com.google.common.collect.ImmutableList;

import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.commons.util.Strings;

/**
 * Utility of Java Bean and Map mutual conversion
 * 
 * @author Ponfee
 */
public enum BeanMaps {

    CGLIB() {
        @Override
        public Map<String, Object> toMap(Object bean) {
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

        @Override
        public void copyFromMap(Map<String, Object> sourceMap, Object targetBean) {
            BeanMap.create(targetBean).putAll(sourceMap);
        }
    },

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
            List<Field> fields = getFields(targetBean.getClass());
            sourceMap.forEach((k, v) -> {
                for (Field field : fields) {
                    if (field.getName().equals(k)) {
                        Fields.put(targetBean, field, ObjectUtils.convert(v, field.getType()));
                    }
                }
            });
        }

        private List<Field> getFields(Class<?> beanType) {
            List<Field> fields = cachedFields.get(beanType);
            if (fields == null) {
                synchronized (FIELDS) {
                    if ((fields = cachedFields.get(beanType)) == null) {
                        List<Field> list = ClassUtils.listFields(beanType);
                        fields = CollectionUtils.isEmpty(list) 
                               ? Collections.emptyList() 
                               : ImmutableList.copyOf(list);
                        cachedFields.put(beanType, fields);
                    }
                }
            }
            return fields;
        }
    },

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
                    if ("class".equals(name = prop.getName())) { // getClass()
                        continue;
                    }

                    String name0 = name;
                    if (!sourceMap.containsKey(name)
                        && !sourceMap.containsKey(name = LOWER_UNDERSCORE.to(LOWER_CAMEL, name0))
                        && !sourceMap.containsKey(name = LOWER_CAMEL.to(LOWER_UNDERSCORE, name0))) {
                        continue;
                    }

                    value = sourceMap.get(name);
                    if ((type = prop.getPropertyType()).isPrimitive() && Strings.isEmpty(value)) {
                        continue; // 原始类型时：value为null或为空字符串时跳过
                    }

                    // set value into bean field
                    prop.getWriteMethod().invoke(targetBean, ObjectUtils.convert(value, type));
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
     * Copies map key-value to bean object field-value
     * 
     * @param sourceMap  the source map
     * @param targetBean the target bean
     */
    public abstract void copyFromMap(Map<String, Object> sourceMap, Object targetBean);

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
