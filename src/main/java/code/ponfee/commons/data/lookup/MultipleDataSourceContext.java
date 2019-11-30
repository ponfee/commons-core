package code.ponfee.commons.data.lookup;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.ImmutableList;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.data.NamedDataSource;

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

    static Map<String, DataSource> process(String defaultName, DataSource defaultDataSource,
                                           NamedDataSource... othersDataSource) {
        if (othersDataSource == null) {
            othersDataSource = new NamedDataSource[0];
        }
        List<String> names = Arrays.stream(othersDataSource)
                                   .map(NamedDataSource::getName)
                                   .collect(Collectors.toList());
        names.add(0, defaultName); // default data source at the first

        // checks whether duplicate datasource name
        Set<String> duplicates = Collects.duplicate(names);
        if (CollectionUtils.isNotEmpty(duplicates)) {
            throw new IllegalArgumentException(
                "Duplicated data source name: " + duplicates.toString()
            );
        }

        addAll(names); // add data source keys 

        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        dataSources.put(defaultName, defaultDataSource);
        Arrays.stream(othersDataSource).forEach(
            ns -> dataSources.put(ns.getName(), ns.getDataSource())
        );
        return dataSources;
    }

    /**
     * Provides gets the list of data source name to external
     * 
     * @return a list of data source name string
     */
    public static List<String> listDataSourceNames() {
        return dataSourceKeys.isEmpty()
             ? Collections.emptyList() 
             : ImmutableList.copyOf(dataSourceKeys);
    }

}
