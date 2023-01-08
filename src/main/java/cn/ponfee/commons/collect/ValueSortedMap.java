/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.collect;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <pre>
 * Extends TreeMap sort by value: {@code
 *   ValueSortedMap valueSortedMap = ValueSortedMap.nullsFirst(a map);
 * }
 *
 * Also use like this: {@code
 *    Map<String, Integer> originMap = ImmutableMap.of("b", 2, "a", 1);
 *    TreeMap<String, Integer> sortedByValueMap = new TreeMap<>(Comparator.comparing(originMap::get));
 *    sortedByValueMap.putAll(originMap);
 * }
 * </pre>
 *
 * @author Ponfee
 */
public class ValueSortedMap<K, V> extends TreeMap<K, V> {

    private static final long serialVersionUID = -6242175050718596776L;

    private ValueSortedMap(Map<? extends K, ? extends V> map, 
                           Comparator<? super V> comparator) {
        super(new MapValueComparator<>(map, comparator));
        map.forEach(super::put);
    }

    public static <K, V extends Comparable<? super V>> ValueSortedMap<K, V> nullsFirst(
        Map<? extends K, ? extends V> map) {
        return nullsFirst(map, Comparators.asc());
    }

    public static <K, V> ValueSortedMap<K, V> nullsFirst(
        Map<? extends K, ? extends V> map, Comparator<? super V> comparator) {
        return new ValueSortedMap<>(map, Comparator.nullsFirst(comparator));
    }

    public static <K, V extends Comparable<? super V>> ValueSortedMap<K, V> nullsLast(
        Map<? extends K, ? extends V> map) {
        return nullsLast(map, Comparators.asc());
    }

    public static <K, V> ValueSortedMap<K, V> nullsLast(
        Map<? extends K,? extends V> map, Comparator<? super V> comparator) {
        return new ValueSortedMap<>(map, Comparator.nullsLast(comparator));
    }

    // ------------------------------------------------------------Comparator
    private static class MapValueComparator<K, V> implements Comparator<K> {
        private final Map<? extends K, ? extends V> data;
        private final Comparator<? super V> comparator;

        private MapValueComparator(Map<? extends K, ? extends V> data,
                                   Comparator<? super V> comparator) {
            this.data = data;
            this.comparator = comparator;
        }

        @Override
        public int compare(K k1, K k2) {
            int n = comparator.compare(data.get(k1), data.get(k2));
            return n != 0 ? n : k1.toString().compareTo(k2.toString());
        }
    }

    // ------------------------------------------------------------Deprecated Methods
    @Deprecated @Override
    public final V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final V putIfAbsent(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final boolean replace(K key, V oldValue, V newValue) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final V replace(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final V computeIfPresent(
        K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final V merge(
        K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final void putAll(Map<? extends K, ? extends V> map) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final V remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
    }

    @Deprecated @Override
    public final void clear() {
      throw new UnsupportedOperationException();
    }
}
