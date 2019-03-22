package code.ponfee.commons.data;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Multiple DataSource Context
 * 
 * @author Ponfee
 */
public final class MultipleDataSourceContext {

    static ImmutableList<String> dataSourceKeys;

    private MultipleDataSourceContext() {}

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void set(String datasourceName) {
        CONTEXT.set(datasourceName);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void remove() {
        CONTEXT.remove();
    }

    public static List<String> getDataSourceKeys() {
        return dataSourceKeys == null 
             ? Collections.emptyList() 
             : dataSourceKeys;
    }
}
