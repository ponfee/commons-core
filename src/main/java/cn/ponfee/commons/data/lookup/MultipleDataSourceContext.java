/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.data.lookup;

import cn.ponfee.commons.collect.Collects;
import cn.ponfee.commons.data.NamedDataSource;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Multiple DataSource Context
 * 
 * @author Ponfee
 */
public final class MultipleDataSourceContext {

    private static final List<String> KEYS = new LinkedList<>(); // new CopyOnWriteArrayList<>()
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void set(String datasourceName) {
        CONTEXT.set(datasourceName);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Provides gets the list of data source name to external
     * 
     * @return a list of data source name string
     */
    public static List<String> listDataSourceNames() {
        return KEYS.isEmpty()
             ? Collections.emptyList() 
             : Collections.unmodifiableList(KEYS);
    }

    // -----------------------------------------------------datasource keys
    synchronized static void add(@Nonnull String key) {
        if (KEYS.contains(key)) {
            throw new IllegalArgumentException("Duplicate key: " + key);
        }
        KEYS.add(key);
    }

    synchronized static void addAll(List<String> keys) {
        if (CollectionUtils.isNotEmpty(keys)) {
            keys.forEach(MultipleDataSourceContext::add);
        }
    }

    synchronized static void remove(String key) {
        KEYS.remove(key);
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
        List<String> duplicates = Collects.duplicate(names);
        if (CollectionUtils.isNotEmpty(duplicates)) {
            throw new IllegalArgumentException("Duplicated data source name: " + duplicates);
        }

        addAll(names); // add data source keys 

        Map<String, DataSource> dataSources = new LinkedHashMap<>(othersDataSource.length + 1, 1);
        dataSources.put(defaultName, defaultDataSource);
        Arrays.stream(othersDataSource)
              .forEach(ns -> dataSources.put(ns.getName(), ns.getDataSource()));
        return dataSources;
    }

}
