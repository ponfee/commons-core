package code.ponfee.commons.data.lookup;

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

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.druid.pool.DruidDataSource;

import code.ponfee.commons.base.Initializable;
import code.ponfee.commons.data.NamedDataSource;
import code.ponfee.commons.io.Closeables;

/**
 * NamedDataSource array
 * 
 * @author Ponfee
 */
public class PropertiedNamedDruidDataSourceArray implements Initializable, Closeable {

    private static final Pattern PATTERN = Pattern.compile("^(\\w+)\\.url$");
    private final NamedDataSource[] array;

    public PropertiedNamedDruidDataSourceArray(Properties props) {
        List<String> names = props.keySet().stream().map(key -> {
            Matcher matcher = PATTERN.matcher(key.toString());
            return matcher.find() ? matcher.group(1) : null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        String defaultDsName = getString(props, "default", names.get(0));
        List<NamedDataSource> dataSources = new ArrayList<>();
        for (String name : names) {
            DruidDataSource ds = new DruidDataSource();
            ds.setUrl(getString(props, name + ".url"));
            ds.setUsername(getString(props, name + ".username"));
            ds.setPassword(getString(props, name + ".password"));
            dataSourceSetting(ds, props);
            NamedDataSource namedDs = new NamedDataSource(name, ds);
            if (defaultDsName.equals(name)) {
                dataSources.add(0, namedDs); // default ds at index 0
            } else {
                dataSources.add(namedDs);
            }
        }

        this.array = dataSources.toArray(new NamedDataSource[dataSources.size()]);
    }

    @Override
    public void init() {
        for (NamedDataSource nds : array) {
            try {
                ((DruidDataSource) nds.getDataSource()).init();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        for (NamedDataSource nds : array) {
            Closeables.log((DruidDataSource) nds.getDataSource());
        }
    }

    public NamedDataSource[] getArray() {
        return array;
    }

    private void dataSourceSetting(DruidDataSource ds, Properties props) {
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

        // filters and monitor
        try {
            ds.setFilters(getString(props, "filters"));
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
        ds.setConnectionProperties(getString(props, "connectionProperties"));
    }

}
