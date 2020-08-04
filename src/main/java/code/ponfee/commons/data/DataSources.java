package code.ponfee.commons.data;

import code.ponfee.commons.model.TypedMapWrapper;
import code.ponfee.commons.reflect.ClassUtils;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_INITIAL_SIZE;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_MAX_ACTIVE_SIZE;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_MAX_WAIT;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_MIN_IDLE;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_TEST_ON_BORROW;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_TEST_ON_RETURN;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_WHILE_IDLE;

/**
 * The enum of DataSourceType
 *
 * @author Ponfee
 */
public enum DataSources {

    DruidDataSource("com.alibaba.druid.pool.DruidDataSource") {
        @Override
        protected void configDataSource(DataSource dataSource, String dsName, Properties props, String prefix) {
            DruidDataSource ds = (DruidDataSource) dataSource;
            TypedMapWrapper<Object, Object> p = new TypedMapWrapper<>(props);

            // ----------------------------------------------------database special config
            ds.setName(dsName);
            ds.setDriverClassName(p.getString(prefix + dsName + ".driver-class-name"));
            ds.setUrl(p.getString(prefix + dsName + ".url"));
            ds.setUsername(p.getString(prefix + dsName + ".username"));
            ds.setPassword(p.getString(prefix + dsName + ".password"));

            // ----------------------------------------------------druid pool commons config
            ds.setMaxActive(p.getInteger(prefix + "maxActive", DEFAULT_MAX_ACTIVE_SIZE));
            ds.setInitialSize(p.getInteger(prefix + "initialSize", DEFAULT_INITIAL_SIZE));
            ds.setMinIdle(p.getInteger(prefix + "minIdle", DEFAULT_MIN_IDLE));
            ds.setMaxWait(p.getInteger(prefix + "maxWait", DEFAULT_MAX_WAIT));
            ds.setTimeBetweenEvictionRunsMillis(p.getLong(prefix + "timeBetweenEvictionRunsMillis", DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS));
            ds.setMinEvictableIdleTimeMillis(p.getLong(prefix + "minEvictableIdleTimeMillis", DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS));
            ds.setValidationQuery(p.getString(prefix + "validationQuery"));

            ds.setTestWhileIdle(p.getBoolean(prefix + "testWhileIdle", DEFAULT_WHILE_IDLE));
            ds.setTestOnBorrow(p.getBoolean(prefix + "testOnBorrow", DEFAULT_TEST_ON_BORROW));
            ds.setTestOnReturn(p.getBoolean(prefix + "testOnReturn", DEFAULT_TEST_ON_RETURN));
            ds.setPoolPreparedStatements(p.getBoolean(prefix + "poolPreparedStatements", false));
            ds.setMaxOpenPreparedStatements(p.getInteger(prefix + "maxOpenPreparedStatements", 10));
            ds.setBreakAfterAcquireFailure(p.getBoolean(prefix + "breakAfterAcquireFailure", false)); // 尝试连接失败后是否中断连接

            ds.setRemoveAbandoned(p.getBoolean(prefix + "removeAbandoned", false));
            ds.setRemoveAbandonedTimeoutMillis(p.getInteger(prefix + "removeAbandonedTimeoutMillis", 300000));
            ds.setLogAbandoned(p.getBoolean(prefix + "logAbandoned", false));

            // filters and monitor
            try {
                String filters = p.getString(prefix + "filters");
                if (StringUtils.isNotBlank(filters)) {
                    filters = filters.trim();
                    boolean force = false;
                    if (filters.startsWith("!")) {
                        filters = filters.substring(1);
                        force = true;
                    }
                    List<String> list = Arrays.stream(filters.split(",")).map(String::trim).collect(Collectors.toList());
                    boolean hasWall = list.remove("wall");
                    filters = (force ? "!" : "") + String.join(",", list);
                    ds.setFilters(filters);
                    if (hasWall) {
                        WallFilter wallf = new WallFilter();
                        WallConfig wallc = new WallConfig();
                        wallc.setCommentAllow(true);
                        wallc.setMultiStatementAllow(true);
                        wallf.setConfig(wallc);
                        ds.setProxyFilters(Collections.singletonList(wallf));
                    }
                }
            } catch (SQLException e) {
                throw new IllegalArgumentException(e);
            }
            ds.setConnectionProperties(p.getString(prefix + "connectionProperties"));
        }
    };

    private final String typeName;
    private final Class<? extends DataSource> typeClass;

    @SuppressWarnings("unchecked")
    DataSources(String typeName) {
        this.typeName = typeName;
        Class<?> clazz;
        try {
            clazz = Class.forName(typeName);
        } catch (Exception e) {
            System.err.println("Datasource class not found: " + typeName);
            clazz = null;
        }
        if (!DataSource.class.isAssignableFrom(clazz)) {
            throw new RuntimeException("Not javax.sql.DataSource type: " + typeName);
        }

        this.typeClass = (Class<? extends DataSource>) clazz;
    }

    protected abstract void configDataSource(DataSource ds, String dsName, Properties props, String prefix);

    public String typeName() {
        return this.typeName;
    }

    public Class<? extends DataSource> typeClass() {
        return this.typeClass;
    }

    // ----------------------------------------------------------------------------------------------static methods
    @SuppressWarnings("unchecked")
    public static DataSource createDataSource(String typeName, String dsName,
                                              Properties props, @Nonnull String prefix) {
        Class<? extends DataSource> datasourceClass;
        try {
            datasourceClass = (Class<? extends DataSource>) Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Datasource type not found: " + typeName, e);
        }
        return createDataSource(datasourceClass, dsName, props, prefix);
    }

    public static DataSource createDataSource(Class<? extends DataSource> typeClass, String dsName,
                                              Properties props, @Nonnull String prefix) {
        DataSources dataSources = of(typeClass);
        if (dataSources == null) {
            throw new RuntimeException("Not match dataSource type: " + typeClass.getName());
        }

        DataSource ds = ClassUtils.newInstance(typeClass);
        dataSources.configDataSource(ds, dsName, props, prefix);
        return ds;
    }

    private static DataSources of(Class<? extends DataSource> typeClass) {
        for (DataSources dst : DataSources.values()) {
            if (dst.typeClass != null && dst.typeClass.isAssignableFrom(typeClass)) {
                return dst;
            }
        }
        return null;
    }

}
