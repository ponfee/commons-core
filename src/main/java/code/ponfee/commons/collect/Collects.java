package code.ponfee.commons.collect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.PageHandler;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.util.ObjectUtils;

/**
 * 集合工具类
 * @author Ponfee
 */
public final class Collects {
    private Collects() {}

    /**
     * Returns a map contains key specified key 
     * 
     * @param map the map
     * @param key the string of key
     * @return {@code true} means constains
     */
    public static boolean hasKey(Map<?, ?> map, String key) {
        return map != null && map.containsKey(key);
    }

    // ----------------------------------------------------------------map to array
    /**
     * map转数组
     * @param map
     * @param fields
     * @return
     */
    public static Object[] map2array(Map<String, Object> map, String... fields) {
        return Stream.of(fields).map(
            field -> Optional.ofNullable(map.get(field)).orElse("")
        ).toArray();
    }

    /**
     * List<Map<String, Object>>转List<Object[]>
     * @param data
     * @param fields
     * @return
     */
    public static List<Object[]> map2array(List<Map<String, Object>> data, String... fields) {
        if (data == null) {
            return null;
        }
        return data.stream().map(
            map -> Collects.map2array(map, fields)
        ).collect(Collectors.toList());
    }

    /**
     * LinkedHashMap<String, Object>转Object[]
     * @param data
     * @return
     */
    public static Object[] map2array(LinkedHashMap<String, Object> data) {
        if (data == null) {
            return null;
        }

        return data.entrySet().stream().map(
            e -> Optional.ofNullable(e.getValue()).orElse("")
        ).toArray();
    }

    /**
     * List<LinkedHashMap<String, Object>> -> List<Object[]>
     * @param data
     * @return
     */
    public static List<Object[]> map2array(List<LinkedHashMap<String, Object>> data) {
        if (data == null) {
            return null;
        }

        return data.stream().map(Collects::map2array).collect(Collectors.toList());
    }

    /**
     * Result<Page<LinkedHashMap<String, Object>>>转Result<Page<Object[]>>
     * @param source
     * @return
     */
    public static Result<Page<Object[]>> map2array(
        Result<Page<LinkedHashMap<String, Object>>> source) {
        return source.copy(source.getData().transform(
            Collects::map2array
        ));
    }

    /**
     * Result<Page<Map<String, Object>>>转Result<Page<Object[]>>
     * @param source
     * @param fields
     * @return
     */
    public static Result<Page<Object[]>> map2array(Result<Page<Map<String, Object>>> source, 
                                                   String... fields) {
        return source.copy(source.getData().transform(map -> map2array(map, fields)));
    }

    // ----------------------------------------------------------------flat map
    /**
     * 指定对象字段keyField的值作为key，字段valueField的值作为value
     * 
     * @param bean
     * @param keyField
     * @param valueField
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V, E> Map<K, V> flatMap(E bean, String keyField, String valueField) {
        if (bean == null) {
            return null;
        }
        return ImmutableMap.of((K) Fields.get(bean, keyField), (V) Fields.get(bean, valueField));
    }

    @SuppressWarnings("unchecked")
    public static <K, V, E> Map<K, V> flatMap(List<E> beans, String keyField, String valueField) {
        if (beans == null) {
            return null;
        }
        return beans.stream().collect(Collectors.toMap(
            bean -> (K) Fields.get(bean, keyField), 
            bean -> (V) Fields.get(bean, valueField)
        ));
    }

    // ----------------------------------------------------------------flat list
    /**
     * 获取对象指定字段的值
     * 
     * @param beans
     * @param field
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    public static <T, E> List<T> flatList(List<E> beans, String field) {
        if (beans == null) {
            return null;
        }
        return beans.stream().map(
            bean -> (T) Fields.get(bean, field)
        ).collect(Collectors.toList());
    }

    public static <E> List<Object[]> flatList(List<E> beans, String... fields) {
        if (beans == null || fields == null || fields.length == 0) {
            return null;
        }

        return beans.stream().map(
            b -> Stream.of(fields).map(f -> Fields.get(b, f)).toArray()
        ).collect(Collectors.toList());
    }

    // ----------------------------------------------------------------of ...
    /**
     * 将对象指定字段转为map
     * 
     * @param bean
     * @param fields
     * @return
     */
    public static <E> Map<String, Object> ofMap(E bean, String... fields) {
        if (bean == null || fields == null) {
            return null;
        }
        return Stream.of(fields).collect(Collectors.toMap(
            Function.identity(), 
            field -> Fields.get(bean, field)
        ));
    }

    /**
     * 将对象指定字段转为map
     * 
     * @param bean
     * @param fields
     * @return
     */
    public static <E> Object[] ofArray(E bean, String... fields) {
        if (bean == null || fields == null) {
            return null;
        }
        return Stream.of(fields).map(
            field -> Fields.get(bean, field)
        ).toArray();
    }

    // -----------------------------the collection of intersect, union and different operations
    /**
     * 求两集合的交集
     * intersect([1,2,3], [2,3,4]) = [2,3]
     * 
     * @param coll1 the collection 1
     * @param coll2 the collection 2
     * @return a list of the two collection intersect 
     */
    public static <T> List<T> intersect(Collection<T> coll1, Collection<T> coll2) {
        return coll1.stream().filter(coll2::contains).collect(Collectors.toList());
    }

    /**
     * 数组与list交集
     * @param array1
     * @param array2
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> T[] intersect(T[] array1, T[] array2) {
        List<T> list = Stream.of(array1).filter(
            t -> ArrayUtils.contains(array2, t)
        ).collect(Collectors.toList());

        Class<?> type = array1.getClass().getComponentType();
        return list.toArray((T[]) Array.newInstance(type, list.size()));
    }

    /**
     * two list union result
     * 
     * @param list1
     * @param list2
     * @return
     */
    public static <T> List<T> union(Collection<T> list1, Collection<T> list2) {
        Set<T> sets = Sets.newHashSet(list1);
        sets.addAll(list2);
        return Lists.newArrayList(sets);
    }

    /**
     * list差集
     * different([1,2,3], [2,3,4]) = [1,4]
     *
     * @param list1
     * @param list2
     * @return
     */
    public static <T> List<T> different(Collection<T> list1, Collection<T> list2) {
        List<T> list = list1.stream().filter(ObjectUtils.not(list2::contains))
                                     .collect(Collectors.toList());

        list.addAll(list2.stream().filter(ObjectUtils.not(list1::contains))
                                  .collect(Collectors.toList()));

        return list;
    }

    /**
     * Returns the duplicates elements for list
     * 
     * @param list the list
     * @return a set of duplicates elements for list
     */
    public static <T> Set<T> duplicate(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptySet();
        }

        return list.stream().collect(
            Collectors.groupingBy(Function.identity(), Collectors.counting())
        ).entrySet().stream().filter(
            e -> (e.getValue() > 1)
        ).map(
            Entry::getKey
        ).collect(
            Collectors.toSet()
        );
    }

    /**
     * The two set different elements
     * 
     * @param set1
     * @param set2
     * @return
     */
    public static <T> Set<T> different(Set<T> set1, Set<T> set2) {
        Set<T> diffSet = Sets.newHashSet(Sets.difference(set1, set2));
        diffSet.addAll(Sets.difference(set2, set1));
        return diffSet;
    }

    /**
     * map差集
     * 
     * @param map1
     * @param map2
     * @return
     */
    public static <K, V> Map<K, V> different(Map<K, V> map1, Map<K, V> map2) {
        Set<K> set1 = map1.keySet();
        Set<K> set2 = map2.keySet();
        Set<K> diffSet = Sets.newHashSet(Sets.difference(set1, set2));
        diffSet.addAll(Sets.difference(set2, set1));
        return diffSet.stream().collect(Collectors.toMap(
            Function.identity(), 
            key -> map1.containsKey(key) ? map1.get(key) : map2.get(key)
        ));
    }

    /**
     * 转map
     * @param kv
     * @return
     */
    public static Map<String, Object> toMap(Object... kv) {
        if (kv == null) {
            return null;
        }

        int length = kv.length;
        if ((length & 0x01) != 0) { // length % 2
            throw new IllegalArgumentException("args must be pair.");
        }

        Map<String, Object> map = new LinkedHashMap<>(length);
        for (int i = 0; i < length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

    /**
     * 转数组
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(T... args) {
        return args;
    }

    /**
     * object to list
     * @param obj of elements
     * @return list with the same elements
     */
    public static List<Object> toList(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            List<Object> result = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                result.add(Array.get(obj, i));
            }
            return result;
        } else if (obj instanceof Collection) {
            return new ArrayList<>((Collection<?>) obj);
        } else {
            return Collections.singletonList(obj);
        }
    }

    /**
     * Returns a new array for merged the generic array generator
     * 
     * @see org.apache.commons.lang3.ArrayUtils#addAll(T[] array1, T... array2)
     * @param generator the generic array generator
     * @param arrays the multiple generic object array
     * @return a new array of merged
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> T[] concat(IntFunction<T[]> generator, T[]... arrays) {
        //Class<?> type = arrays[0].getClass().getComponentType();
        //return list.toArray((T[]) Array.newInstance(type, list.size()));

        // [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
        //return list.toArray((T[]) new Object[list.size()]);

        if (ArrayUtils.isEmpty(arrays)) {
            return null;
        }

        return Arrays.stream(arrays)
                     .filter(Objects::nonNull)
                     .flatMap(Arrays::stream)
                     .toArray(generator);
    }

    /**
     * Splinter the collection to batch
     * 
     * @param coll the collection
     * @param batchSize the batch size
     * @return batch collection
     */
    public static <T> List<List<T>> splinter(Collection<T> coll, int batchSize) {
        List<List<T>> result = new ArrayList<>(PageHandler.computeTotalPages(coll.size(), batchSize));
        List<T> batch = new ArrayList<>(batchSize);
        for (T item : coll) {
            batch.add(item);
            if (batch.size() == batchSize) {
                result.add(batch);
                batch = new ArrayList<>(batchSize);
            }
        }
        if (!batch.isEmpty()) {
            result.add(batch);
        }
        return result;
    }

    /**
     * Splinter the List to batch
     * 
     * @param list the collection
     * @param batchSize the batch size
     * @return batch collection
     */
    public static <T> List<List<T>> splinter(List<T> list, int batchSize) {
        List<List<T>> result = new ArrayList<>(PageHandler.computeTotalPages(list.size(), batchSize));
        for (int i = 0, n = list.size(); i < n; i += batchSize) {
            result.add(list.subList(i, Math.min(i + batchSize, n)));
        }
        return result;
    }

    /**
     * Puts the element to list specified index
     * 
     * @param list a list
     * @param index spec index
     * @param obj the element
     */
    public static <T> void set(List<T> list, int index, T obj) {
        int size;
        if (index == (size = list.size())) {
            list.add(obj);
        } else if (index < size) {
            list.set(index, obj);
        } else {
            for (int i = size; i < index; i++) {
                list.add(null);
            }
            list.add(obj);
        }
    }

    /**
     * Compute cartesian product
     * 
     * @param list1 the list of type A
     * @param list2 the list of type B
     * @param funcs convert A and B to T
     * @return a list of type T
     */
    public static <A, B, T> List<T> cartesian(List<A> list1, List<B> list2, BiFunction<A, B, T> funcs) {
        List<T> product = new ArrayList<>(list1.size() * list2.size());
        for (A a : list1) {
            for (B b : list2) {
                product.add(funcs.apply(a, b));
            }
        }
        return product;
    }

    /**
     * Swaps x[a] with x[b].
     */
    public static void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

}
