/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.data.lookup;

import cn.ponfee.commons.base.Initializable;
import cn.ponfee.commons.base.Releasable;
import cn.ponfee.commons.data.NamedDataSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 可缓存的多数据源类型：可动态增加数据源、可动态移除数据源、数据源自动超时失效
 * 
 * @author Ponfee
 * @see org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
 */
public class MultipleCachedDataSource extends AbstractDataSource
    implements DataSourceLookup, Initializable, Closeable {

    private final Map<String, DataSource> naturalDataSources; // original/native
    private final DataSource defaultDataSource;

    private final Cache<String, DataSource> adoptedDataSources; // foreign/stranger

    public MultipleCachedDataSource(int expireSeconds, NamedDataSource dataSource) {
        this(expireSeconds, dataSource.getName(), dataSource.getDataSource());
    }

    public MultipleCachedDataSource(int expireSeconds, NamedDataSource... dataSources) {
        this(
            expireSeconds,
            dataSources[0].getName(), 
            dataSources[0].getDataSource(), 
            ArrayUtils.subarray(dataSources, 1, dataSources.length)
        );
    }

    public MultipleCachedDataSource(int expireSeconds, String defaultName,
                                    DataSource defaultDataSource,
                                    NamedDataSource... othersDataSource) {
        // set the default data source
        this.defaultDataSource = defaultDataSource;

        this.naturalDataSources = ImmutableMap.copyOf(
            MultipleDataSourceContext.process(defaultName, defaultDataSource, othersDataSource)
        );

        this.adoptedDataSources = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(expireSeconds))
            .maximumSize(8192)
            .removalListener(notification -> {
                try {
                    Releasable.release(notification.getValue());
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            })
            .build();
    }

    // -----------------------------------------------------------------add/remove
    public synchronized boolean addIfAbsent(String dataSourceName, 
                                            Supplier<DataSource> supplier) {
        if (existsDatasourceName(dataSourceName)) {
            return false;
        }

        this.add(dataSourceName, supplier.get());
        return true;
    }

    public synchronized boolean addIfAbsent(String dataSourceName, DataSource datasource) {
        if (existsDatasourceName(dataSourceName)) {
            return false;
        }

        this.add(dataSourceName, datasource);
        return true;
    }

    public synchronized void add(NamedDataSource ds) {
        this.add(ds.getName(), ds.getDataSource());
    }

    public synchronized void add(@Nonnull String dataSourceName, 
                                 @Nonnull DataSource datasource) {
        if (existsDatasourceName(dataSourceName)) { // check the datasource name not exists
            throw new IllegalArgumentException("Duplicated datasource name: " + dataSourceName);
        }

        this.adoptedDataSources.put(dataSourceName, datasource);
        MultipleDataSourceContext.add(dataSourceName);
    }

    public synchronized void remove(String dataSourceName) {
        if (this.naturalDataSources.containsKey(dataSourceName)) {
            throw new UnsupportedOperationException("Local datasource cannot remove: " + dataSourceName);
        }

        this.adoptedDataSources.invalidate(dataSourceName);
        MultipleDataSourceContext.remove(dataSourceName);
    }

    // -----------------------------------------------------------------override methods
    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface);
    }

    @Override
    public DataSource lookupDataSource(String name) {
        DataSource dataSource = this.naturalDataSources.get(name);
        return dataSource != null 
             ? dataSource 
             : this.adoptedDataSources.getIfPresent(name);
    }

    @Override
    public void init() {
        naturalDataSources.forEach((name, ds) -> Initializable.init(ds));
    }

    @Override
    public void close() {
        naturalDataSources.forEach((name, ds) -> {
            try {
                Releasable.release(ds);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        });
        adoptedDataSources.asMap().forEach((name, ds) -> {
            try {
                Releasable.release(ds);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        });
    }

    // -----------------------------------------------------------------private methods
    /**
     * Retrieve the current target DataSource. Determines the
     */
    private DataSource determineTargetDataSource() {
        String lookupKey = MultipleDataSourceContext.get();
        DataSource dataSource = (lookupKey == null) 
                              ? this.defaultDataSource 
                              : lookupDataSource(lookupKey);
        if (dataSource == null) {
            throw new IllegalStateException("Cannot found DataSource by name [" + lookupKey + "]");
        }
        return dataSource;
    }

    private boolean existsDatasourceName(String name) {
        return this.naturalDataSources.containsKey(name)
            || this.adoptedDataSources.getIfPresent(name) != null;
    }

}
