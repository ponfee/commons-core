package code.ponfee.commons.collect;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Map Utilities
 * 
 * @author Ponfee
 */
public final class Maps {

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
     * 
     * @param map
     * @param fields
     * @return
     */
    public static Object[] toArray(Map<String, Object> map, String... fields) {
        return Stream.of(fields)
                     .map(field -> Optional.ofNullable(map.get(field)).orElse(""))
                     .toArray();
    }

    /**
     * List<Map<String, Object>>转List<Object[]>
     * @param data
     * @param fields
     * @return
     */
    public static List<Object[]> toArray(List<Map<String, Object>> data, String... fields) {
        if (data == null) {
            return null;
        }
        return data.stream()
                   .map(map -> toArray(map, fields))
                   .collect(Collectors.toList());
    }

    /**
     * LinkedHashMap<String, Object>转Object[]
     * @param data
     * @return
     */
    public static Object[] toArray(LinkedHashMap<String, Object> data) {
        if (data == null) {
            return null;
        }

        return data.entrySet()
                   .stream()
                   .map(e -> Optional.ofNullable(e.getValue()).orElse(""))
                   .toArray();
    }

    /**
     * List<LinkedHashMap<String, Object>> -> List<Object[]>
     * @param data
     * @return
     */
    public static List<Object[]> toArray(List<LinkedHashMap<String, Object>> data) {
        if (data == null) {
            return null;
        }

        return data.stream()
                   .map(Maps::toArray)
                   .collect(Collectors.toList());
    }

    /**
     * Result<Page<LinkedHashMap<String, Object>>>转Result<Page<Object[]>>
     * @param source
     * @return
     */
    public static Result<Page<Object[]>> toArray(Result<Page<LinkedHashMap<String, Object>>> source) {
        return source.copy(source.getData().map(Maps::toArray));
    }

    /**
     * Result<Page<Map<String, Object>>>转Result<Page<Object[]>>
     * @param source
     * @param fields
     * @return
     */
    public static Result<Page<Object[]>> toArray(Result<Page<Map<String, Object>>> source, String... fields) {
        return source.copy(source.getData().map(map -> toArray(map, fields)));
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
        // length % 2
        if ((length & 0x01) != 0) {
            throw new IllegalArgumentException("args must be pair.");
        }

        // length>>>1
        Map<String, Object> map = new LinkedHashMap<>(length);
        for (int i = 0; i < length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

}
