package code.ponfee.commons.collect;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.util.ObjectUtils;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
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
        }
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            List<Object> result = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                result.add(Array.get(obj, i));
            }
            return result;
        }
        if (obj instanceof Collection) {
            return new ArrayList<>((Collection<?>) obj);
        }
        return Collections.singletonList(obj);

    }

    public static <E> LinkedList<E> newLinkedList(E element) {
        LinkedList<E> list = new LinkedList<>();
        list.add(element);
        return list;
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
        Map<K, V> res = new HashMap<>(Math.max(map1.size(), map2.size()));
        map1.entrySet()
            .stream()
            .filter(e -> !map2.containsKey(e.getKey()))
            .forEach(e -> res.put(e.getKey(), e.getValue()));

        map2.entrySet()
            .stream()
            .filter(e -> !map1.containsKey(e.getKey()))
            .forEach(e -> res.put(e.getKey(), e.getValue()));
        return res;
    }

    public static <T> List<T> duplicate(Collection<T> list) {
        return duplicate(list, Function.identity());
    }

    /**
     * Returns the duplicates elements for list
     * 
     * @param list the list
     * @return a set of duplicates elements for list
     */
    public static <T, R> List<R> duplicate(Collection<T> list, Function<T, R> mapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream()
                   .map(mapper)
                   .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                   .entrySet()
                   .stream()
                   .filter(e -> e.getValue() > 1)
                   .map(Entry::getKey)
                   .collect(Collectors.toList());
    }

    /**
     * Gets the last element for list
     *
     * @param list the list
     * @param <T>  the list element type
     * @return last element of list
     */
    public static <T> T getLast(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        //return list.stream().reduce((a, b) -> b).orElse(null);
        return list instanceof Deque ? ((Deque<T>) list).getLast() : list.get(list.size() - 1);
    }

    /**
     * Gets the first element for list
     *
     * @param list the list
     * @param <T>  the list element type
     * @return first element of list
     */
    public static <T> T getFirst(List<T> list) {
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    public static <T> T get(T[] array, int index) {
        return index < array.length ? array[index] : null;
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
        // component type maybe not correct
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
     * [1, 2, 3] x [4, 5, 6] = [[4, 5, 6], [8, 10, 12], [12, 15, 18]]
     * 
     * @param x the list of type A
     * @param y the list of type B
     * @param fun convert A and B to T
     * @return a list of type T
     */
    public static <A, B, T> List<List<T>> cartesian(List<A> x, List<B> y, BiFunction<A, B, T> fun) {
        List<List<T>> product = new ArrayList<>(x.size());
        for (A a : x) {
            List<T> row = new ArrayList<>(y.size());
            for (B b : y) {
                row.add(fun.apply(a, b));
            }
            product.add(row);
        }
        return product;
    }

    /**
     * Returns consecutive sub array of an array, 
     * each of the same size (the final list may be smaller).
     *
     * <pre>
     *  Collects.partition(new int[]{1,1,2,5,3}, 1)    ->  [1, 1, 2, 5, 3]
     *  Collects.partition(new int[]{1,1,2,5,3}, 3)    ->  [1, 1]; [2, 5]; [3]
     *  Collects.partition(new int[]{1,1,2,5,3}, 5)    ->  [1]; [1]; [2]; [5]; [3]
     *  Collects.partition(new int[]{1,1,2,5,3}, 6)    ->  [1]; [1]; [2]; [5]; [3]
     *  Collects.partition(new int[]{1,1,2,5,3}, 100)  ->  [1]; [1]; [2]; [5]; [3]
     * </pre>
     *
     * @param array the array
     * @param size  the size
     * @return a list of consecutive sub sets
     */
    public static List<int[]> partition(int[] array, int size) {
        Assert.isTrue(size > 0, "Size must be greater than 0.");
        if (array == null || array.length == 0) {
            return null;
        }
        size = Math.min(size, array.length);
        if (size == 1) {
            return Collections.singletonList(array);
        }

        List<int[]> result = new ArrayList<>(size);
        int pos = 0;
        for (int number : Numbers.slice(array.length, size)) {
            if (number == 0) {
                break;
            }
            result.add(Arrays.copyOfRange(array, pos, pos = pos + number));
        }
        return result;
    }

    /**
     * Rotate list array data
     * 
     * [[a,b,c,d],[1,2,3,4]] -> [[a,1],[b,2],[c,3],[d,4]]
     * 
     * @param list the list
     * @return a list array result
     */
    public static List<Object[]> rotate(List<Object[]> list) {
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
                ArrayUtils.swap(array, i, minimumIndex);
                ArrayUtils.swap(indexSwapMapping, i, minimumIndex);
            }
        }
        return indexSwapMapping;
    }

    /**
     * Checks that the specified array reference is not null and not empty,
     * throws a customized {@link IllegalStateException} if it is.
     * 
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

    public static <S, T> List<T> convert(List<S> source, Function<S, T> mapper) {
        ImmutableList.Builder<T> builder = ImmutableList.builderWithExpectedSize(source.size());
        source.stream().map(mapper::apply).forEach(builder::add);
        return builder.build();
    }

    public static <T> List<T> concat(List<T> list, T... array) {
        if (array == null || array.length == 0) {
            return list;
        }
        List<T> result = new ArrayList<>(list.size() + array.length);
        result.addAll(list);
        for (T t : array) {
            result.add(t);
        }
        return result;
    }
}
