package code.ponfee.commons.collect;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.PageHandler;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.util.ObjectUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        return source.copy(source.getData().map(Collects::map2array));
    }

    /**
     * Result<Page<Map<String, Object>>>转Result<Page<Object[]>>
     * @param source
     * @param fields
     * @return
     */
    public static Result<Page<Object[]>> map2array(Result<Page<Map<String, Object>>> source, 
                                                   String... fields) {
        return source.copy(source.getData().map(map -> map2array(map, fields)));
    }

    // ----------------------------------------------------------------to List, Map and Array
    /**
     * Converts array to map
     * 
     * @param kv the key value array
     * @return a map
     */
    public static Map<String, Object> toMap(Object... kv) {
        if (kv == null) {
            return null;
        }

        int length = kv.length;
        if ((length & 0x01) != 0) { // length % 2
            throw new IllegalArgumentException("args must be pair.");
        }

        Map<String, Object> map = new LinkedHashMap<>(length/*>>>1*/);
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

    // -----------------------------the collection of intersect, union and different operations
    /**
     * two Collection intersect
     * intersect([1,2,3], [2,3,4]) = [2,3]
     * 
     * @param coll1 the collection 1
     * @param coll2 the collection 2
     * @return a list of the two collection intersect result
     */
    public static <T> List<T> intersect(Collection<T> coll1, Collection<T> coll2) {
        return coll1.stream().filter(coll2::contains).collect(Collectors.toList());
    }

    /**
     * two array intersect
     *
     * @param array1
     * @param array2
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> T[] intersect(T[] array1, T[] array2) {
        List<T> list = Stream.of(array1)
                             .filter(t -> ArrayUtils.contains(array2, t))
                             .collect(Collectors.toList());

        Class<?> type = array1.getClass().getComponentType();
        return list.toArray((T[]) Array.newInstance(type, list.size()));
    }

    /**
     * two Collection union result
     * 
     * @param coll1
     * @param coll2
     * @return
     */
    public static <T> List<T> union(Collection<T> coll1, Collection<T> coll2) {
        int max = coll1.size(), min = coll2.size();
        if (max < min) {
            int tmp = max;
            max = min;
            min = tmp;
        }
        List<T> res = new ArrayList<>(max + (min >> 1));
        res.addAll(coll1);
        coll2.stream().filter(ObjectUtils.not(coll1::contains)).forEach(res::add);
        return res;
    }

    /**
     * list差集
     * different([1,2,3], [2,3,4]) = [1,4]
     *
     * @param list1
     * @param list2
     * @return
     */
    public static <T> List<T> different(List<T> list1, List<T> list2) {
        List<T> res = new ArrayList<>();
        list1.stream().filter(ObjectUtils.not(list2::contains)).forEach(res::add);
        list2.stream().filter(ObjectUtils.not(list1::contains)).forEach(res::add);
        return res;
    }

    /**
     * The two set different elements
     * 
     * @param set1
     * @param set2
     * @return
     */
    public static <T> Set<T> different(Set<T> set1, Set<T> set2) {
        Set<T> res = new HashSet<>();
        set1.stream().filter(ObjectUtils.not(set2::contains)).forEach(res::add);
        set2.stream().filter(ObjectUtils.not(set1::contains)).forEach(res::add);
        return res;
    }

    /**
     * map差集
     * 
     * @param map1
     * @param map2
     * @return
     */
    public static <K, V> Map<K, V> different(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> res = new HashMap<>();
        map1.entrySet().stream().filter(e -> !map2.containsKey(e.getKey()))
                                .forEach(e -> res.put(e.getKey(), e.getValue()));

        map2.entrySet().stream().filter(e -> !map1.containsKey(e.getKey()))
                                .forEach(e -> res.put(e.getKey(), e.getValue()));
        return res;
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
        List<List<T>> result = new ArrayList<>(
            PageHandler.computeTotalPages(coll.size(), batchSize)
        );
        splinter(coll, batchSize, result::add);
        return result;
    }

    /**
     * Splinter the collection to batch
     * 
     * @param coll      the collection
     * @param batchSize the batch size
     * @param action    the Consumer for process eatch batch
     */
    public static <T> void splinter(Collection<T> coll, int batchSize, 
                                    Consumer<List<T>> action) {
        List<T> batch = new ArrayList<>(batchSize);
        for (T item : coll) {
            batch.add(item);
            if (batch.size() == batchSize) {
                action.accept(batch);
                batch = new ArrayList<>(batchSize);
            }
        }
        if (!batch.isEmpty()) {
            action.accept(batch);
        }
    }

    /**
     * Splinter the List to batch
     * 
     * @param list the collection
     * @param batchSize the batch size
     * @return batch collection
     */
    public static <T> List<List<T>> splinter(List<T> list, int batchSize) {
        List<List<T>> result = new ArrayList<>(
            PageHandler.computeTotalPages(list.size(), batchSize)
        );
        splinter(list, batchSize, result::add);
        return result;
    }

    /**
     * Splinter the List to batch
     * 
     * @param list the collection
     * @param batchSize the batch size
     * @param action    process each batch data
     */
    public static <T> void splinter(List<T> list, int batchSize, Consumer<List<T>> action) {
        for (int i = 0, n = list.size(); i < n; i += batchSize) {
            action.accept(list.subList(i, Math.min(i + batchSize, n)));
        }
    }

    /**
     * Puts the element to list specified index
     * 
     * @param list a list
     * @param index spec index
     * @param obj the element
     */
    public static <T> void set(List<T> list, int index, T obj) {
        for (int i = list.size(); i <= index; i++) {
            list.add(null);
        }
        list.set(index, obj);
    }

    /**
     * Gets the index element from list, if not arrival index pos then auto imcrement
     * 
     * @param list     the list
     * @param index    the index
     * @param supplier the element generator
     * @return an element
     */
    public static <T> T get(List<T> list, int index, Supplier<T> supplier) {
        for (int i = list.size(); i <= index; i++) {
            list.add(supplier.get());
        }
        return list.get(index);
    }

    /**
     * Compute cartesian product
     * 
     * @param x the list of type A
     * @param y the list of type B
     * @param fun convert A and B to T
     * @return a list of type T
     */
    public static <A, B, T> List<T> cartesian(List<A> x, List<B> y, 
                                              BiFunction<A, B, T> fun) {
        List<T> product = new ArrayList<>(x.size() * y.size());
        for (A a : x) {
            for (B b : y) {
                product.add(fun.apply(a, b));
            }
        }
        return product;
    }

    /**
     * Swaps x[a] with x[b].
     * 
     * @see org.apache.commons.lang3.ArrayUtils#swap(Object[], int, int)
     */
    public static <T> void swap(T[] x, int a, int b) {
        T t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    /**
     * Reverse list array data
     * 
     * [[a,b,c,d],[1,2,3,4]] -> [[a,1],[b,2],[c,3],[d,4]]
     * 
     * @param list the list
     * @return a list array result
     */
    public static List<Object[]> reverse(List<Object[]> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        int length = list.get(0).length, size = list.size();
        List<Object[]> result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            Object[] array = new Object[size];
            for (int j = 0; j < size; j++) {
                array[j] = list.get(j)[i];
            }
            result.add(array);
        }
        return result;
    }

    public static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static int[] sortAndGetIndexSwapMapping(int[] array) {
        int[] indexSwapMapping = IntStream.range(0, array.length).toArray();
        for (int n = array.length - 1, i = 0; i < n; i++) {
            int minimumIndex = i;
            for (int j = i + 1; j <= n; j++) {
                if (array[minimumIndex] > array[j]) {
                    minimumIndex = j;
                }
            }
            if (minimumIndex != i) {
                swap(array, i, minimumIndex);
                swap(indexSwapMapping, i, minimumIndex);
            }
        }
        return indexSwapMapping;
    }

    /**
     * Checks that the specified array reference is not null and not empty,
     * throws a customized {@link IllegalStateException} if it is.
     * @param array the array
     * @param <T> the type of the array element
     * @return {@code array} if not null and not empty
     */
    public static <T> T[] requireNonEmpty(T[] array) {
        if (ArrayUtils.isEmpty(array)) {
            throw new IllegalStateException("The array cannot be empty.");
        }
        return array;
    }

    /**
     * Checks that the specified list reference is not null and not empty,
     * throws a customized {@link IllegalStateException} if it is.
     *
     * @param list the list
     * @param <T> the type of the list element
     * @return {@code list} if not null and not empty
     */
    public static <T> List<T> requireNonEmpty(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new IllegalStateException("The list cannot be empty.");
        }
        return list;
    }
}
