package code.ponfee.commons.data;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.ImmutableList;

import code.ponfee.commons.collect.Collects;

/**
 * Multiple DataSource Context
 * 
 * @author Ponfee
 */
public final class MultipleDataSourceContext {

    private static List<String> dataSourceKeys = new CopyOnWriteArrayList<>();

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private MultipleDataSourceContext() {}

    public static void set(String datasourceName) {
        CONTEXT.set(datasourceName);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    // -----------------------------------------------------datasource keys
    static void add(String key) {
        if (dataSourceKeys.contains(key)) {
            throw new IllegalArgumentException("Duplicate key: " + key);
        }
        dataSourceKeys.add(key);
    }

    static void addAll(List<String> keys) {
        Set<String> duplicates = Collects.duplicate(keys);
        if (CollectionUtils.isNotEmpty(duplicates)) {
            throw new IllegalArgumentException("Duplicated key: " + duplicates);
        }

        List<String> repeats = Collects.intersect(dataSourceKeys, duplicates);
        if (CollectionUtils.isNotEmpty(repeats)) {
            throw new IllegalArgumentException("Repeated key: " + repeats);
        }

        dataSourceKeys.addAll(keys);
    }

    static void remove(String key) {
        dataSourceKeys.remove(key);
    }

    public static List<String> getDataSourceKeys() {
        return dataSourceKeys == null 
             ? Collections.emptyList() 
             : ImmutableList.copyOf(dataSourceKeys);
    }
}
