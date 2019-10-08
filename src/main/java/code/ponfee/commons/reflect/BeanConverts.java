package code.ponfee.commons.reflect;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.reflect.Fields;

/**
 * Extract bean to array, list and map
 * 
 * @author Ponfee
 */
public final class BeanConverts {

    /**
     * 将对象指定字段转为map
     * 
     * @param bean
     * @param fields
     * @return
     */
    public static <E> Object[] toArray(E bean, String... fields) {
        if (bean == null || fields == null) {
            return null;
        }
        return Stream.of(fields).map(
            field -> Fields.get(bean, field)
        ).toArray();
    }

    /**
     * 获取对象指定字段的值
     * 
     * @param beans
     * @param field
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    public static <T, E> List<T> toList(List<E> beans, String field) {
        if (beans == null) {
            return null;
        }
        return beans.stream().map(
            bean -> (T) Fields.get(bean, field)
        ).collect(Collectors.toList());
    }

    public static <E> List<Object[]> toList(List<E> beans, String... fields) {
        if (beans == null || fields == null || fields.length == 0) {
            return null;
        }

        return beans.stream().map(
            b -> Stream.of(fields).map(f -> Fields.get(b, f)).toArray()
        ).collect(Collectors.toList());
    }

    /**
     * 指定对象字段keyField的值作为key，字段valueField的值作为value
     * 
     * @param bean
     * @param keyField
     * @param valueField
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V, E> Map<K, V> toMap(E bean, String keyField, String valueField) {
        if (bean == null) {
            return null;
        }
        return ImmutableMap.of(
            (K) Fields.get(bean, keyField), (V) Fields.get(bean, valueField)
        );
    }

    @SuppressWarnings("unchecked")
    public static <K, V, E> Map<K, V> toMap(List<E> beans, String keyField, String valueField) {
        if (beans == null) {
            return null;
        }
        return beans.stream().collect(Collectors.toMap(
            bean -> (K) Fields.get(bean, keyField), 
            bean -> (V) Fields.get(bean, valueField)
        ));
    }

    @SuppressWarnings("unchecked")
    public static <K, E> Map<K, E> toMap(List<E> beans, String keyField) {
        if (beans == null) {
            return null;
        }
        return beans.stream().collect(Collectors.toMap(
            bean -> (K) Fields.get(bean, keyField), 
            Function.identity()
        ));
    }

}
