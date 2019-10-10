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
        protected DataSource createDataSourceInternal(String dsName, Properties props) {
            DruidDataSource ds = new DruidDataSource();
            ds.setUrl(getString(props, dsName + ".url"));
            ds.setUsername(getString(props, dsName + ".username"));
            ds.setPassword(getString(props, dsName + ".password"));
            return ds;
        }

        @Override
        protected void configDataSourceInternal(DataSource dataSource, Properties props) {
            DruidDataSource ds = (DruidDataSource) dataSource;
            ds.setDriverClassName(getString(props, "driver-class-name"));
            ds.setMaxActive(getInteger(props, "maxActive", DEFAULT_MAX_ACTIVE_SIZE));
            ds.setInitialSize(getInteger(props, "initialSize", DEFAULT_INITIAL_SIZE));
            ds.setMinIdle(getInteger(props, "minIdle", DEFAULT_MIN_IDLE));
            ds.setMaxWait(getInteger(props, "maxWait", DEFAULT_MAX_WAIT));
            ds.setTimeBetweenEvictionRunsMillis(getLong(props, "timeBetweenEvictionRunsMillis", DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS));
            ds.setMinEvictableIdleTimeMillis(getLong(props, "minEvictableIdleTimeMillis", DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS));
            ds.setRemoveAbandonedTimeoutMillis(getInteger(props, "removeAbandonedTimeoutMillis", 300000));
            ds.setValidationQuery(getString(props, "validationQuery"));

            ds.setTestWhileIdle(getBoolean(props, "testWhileIdle", DEFAULT_WHILE_IDLE));
            ds.setTestOnBorrow(getBoolean(props, "testOnBorrow", DEFAULT_TEST_ON_BORROW));
            ds.setTestOnReturn(getBoolean(props, "testOnReturn", DEFAULT_TEST_ON_RETURN));
            ds.setPoolPreparedStatements(getBoolean(props, "poolPreparedStatements", false));
            ds.setMaxOpenPreparedStatements(getInteger(props, "maxOpenPreparedStatements", 10));
            ds.setBreakAfterAcquireFailure(getBoolean(props, "breakAfterAcquireFailure", false)); // 尝试连接失败后是否中断连接

            // filters and monitor
            try {
                ds.setFilters(getString(props, "filters"));
            } catch (SQLException e) {
                throw new IllegalArgumentException(e);
            }
            ds.setConnectionProperties(getString(props, "connectionProperties"));
        }
    };

    DataSourceType(String type) {
        this.type = type;
    }

    public final DataSource createDataSource(String dsName, Properties props) {
        DataSource ds = createDataSourceInternal(dsName, props);
        configDataSourceInternal(ds, props);
        return ds;
    }

    protected abstract DataSource createDataSourceInternal(String dsName, Properties props);

    protected abstract void configDataSourceInternal(DataSource ds, Properties props);

    private final String type;

    public String type() {
        return this.type;
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
