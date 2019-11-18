package code.ponfee.commons.data;

import static code.ponfee.commons.util.PropertiesUtils.getBoolean;
import static code.ponfee.commons.util.PropertiesUtils.getInteger;
import static code.ponfee.commons.util.PropertiesUtils.getLong;
import static code.ponfee.commons.util.PropertiesUtils.getString;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_INITIAL_SIZE;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_MAX_ACTIVE_SIZE;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_MAX_WAIT;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_MIN_IDLE;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_TEST_ON_BORROW;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_TEST_ON_RETURN;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
import static com.alibaba.druid.pool.DruidAbstractDataSource.DEFAULT_WHILE_IDLE;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;

import code.ponfee.commons.reflect.ClassUtils;

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

            // ----------------------------------------------------database special config
            ds.setName(dsName);
            ds.setDriverClassName(getString(props, prefix + dsName + ".driver-class-name"));
            ds.setUrl(getString(props, prefix + dsName + ".url"));
            ds.setUsername(getString(props, prefix + dsName + ".username"));
            ds.setPassword(getString(props, prefix + dsName + ".password"));

            // ----------------------------------------------------druid pool commons config
            ds.setMaxActive(getInteger(props, prefix + "maxActive", DEFAULT_MAX_ACTIVE_SIZE));
            ds.setInitialSize(getInteger(props, prefix + "initialSize", DEFAULT_INITIAL_SIZE));
            ds.setMinIdle(getInteger(props, prefix + "minIdle", DEFAULT_MIN_IDLE));
            ds.setMaxWait(getInteger(props, prefix + "maxWait", DEFAULT_MAX_WAIT));
            ds.setTimeBetweenEvictionRunsMillis(getLong(props, prefix + "timeBetweenEvictionRunsMillis", DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS));
            ds.setMinEvictableIdleTimeMillis(getLong(props, prefix + "minEvictableIdleTimeMillis", DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS));
            ds.setRemoveAbandonedTimeoutMillis(getInteger(props, prefix + "removeAbandonedTimeoutMillis", 300000));
            ds.setValidationQuery(getString(props, prefix + "validationQuery"));

            ds.setTestWhileIdle(getBoolean(props, prefix + "testWhileIdle", DEFAULT_WHILE_IDLE));
            ds.setTestOnBorrow(getBoolean(props, prefix + "testOnBorrow", DEFAULT_TEST_ON_BORROW));
            ds.setTestOnReturn(getBoolean(props, prefix + "testOnReturn", DEFAULT_TEST_ON_RETURN));
            ds.setPoolPreparedStatements(getBoolean(props, prefix + "poolPreparedStatements", false));
            ds.setMaxOpenPreparedStatements(getInteger(props, prefix + "maxOpenPreparedStatements", 10));
            ds.setBreakAfterAcquireFailure(getBoolean(props, prefix + "breakAfterAcquireFailure", false)); // 尝试连接失败后是否中断连接

            // filters and monitor
            try {
                String filters = getString(props, prefix + "filters");
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
            ds.setConnectionProperties(getString(props, prefix + "connectionProperties"));
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
