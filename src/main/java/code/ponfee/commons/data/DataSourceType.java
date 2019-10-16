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
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;

import code.ponfee.commons.util.Enums;

/**
 * The enum of DataSourceType
 * 
 * @author Ponfee
 */
public enum DataSourceType {

    DruidDataSource("com.alibaba.druid.pool.DruidDataSource") {
        @Override
        protected DataSource createDataSourceInternal(String dsName, Properties props, String prefix) {
            DruidDataSource ds = new DruidDataSource();
            ds.setDriverClassName(getString(props, prefix + dsName + ".driver-class-name"));
            ds.setUrl(getString(props, prefix + dsName + ".url"));
            ds.setUsername(getString(props, prefix + dsName + ".username"));
            ds.setPassword(getString(props, prefix + dsName + ".password"));
            return ds;
        }

        @Override
        protected void configDataSourceInternal(DataSource dataSource, Properties props, String prefix) {
            DruidDataSource ds = (DruidDataSource) dataSource;
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
                ds.setFilters(getString(props, prefix + "filters"));
            } catch (SQLException e) {
                throw new IllegalArgumentException(e);
            }
            ds.setConnectionProperties(getString(props, prefix + "connectionProperties"));
        }
    };

    DataSourceType(String type) {
        this.type = type;
    }

    public final DataSource createDataSource(String dsName, Properties props, @Nonnull String prefix) {
        DataSource ds = createDataSourceInternal(dsName, props, prefix);
        configDataSourceInternal(ds, props, prefix);
        return ds;
    }

    protected abstract DataSource createDataSourceInternal(String dsName, Properties props, String prefix);

    protected abstract void configDataSourceInternal(DataSource ds, Properties props, String prefix);

    private final String type;

    public String type() {
        return this.type;
    }

    public static DataSourceType ofType(Class<?> type) {
        return ofName(type.getName());
    }

    public static DataSourceType ofType(String type) {
        for (DataSourceType dst : DataSourceType.values()) {
            if (dst.type.equals(type)) {
                return dst;
            }
        }
        return null;
    }

    public static DataSourceType ofName(String name) {
        return Enums.ofIgnoreCase(DataSourceType.class, name);
    }

}
