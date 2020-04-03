package code.ponfee.commons.data.lookup;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.AbstractDataSource;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.base.Releasable;
import code.ponfee.commons.data.NamedDataSource;
import code.ponfee.commons.exception.Throwables;

/**
 * 可动态增加/移除数据源/数据源自动超时失效
 * 
 * @author Ponfee
 * @see org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
 */
public class MultipletCachedDataSource extends AbstractDataSource implements DataSourceLookup {

    private final Map<String, DataSource> localDataSources;
    private final DataSource defaultDataSource;

    private final Cache<String, DataSource> strangerDataSources;

    public MultipletCachedDataSource(int expireSeconds, NamedDataSource dataSource) {
        this(expireSeconds, dataSource.getName(), dataSource.getDataSource());
    }

    public MultipletCachedDataSource(int expireSeconds, NamedDataSource... dataSources) {
        this(
            expireSeconds,
            dataSources[0].getName(), 
            dataSources[0].getDataSource(), 
            ArrayUtils.subarray(dataSources, 1, dataSources.length)
        );
    }

    public MultipletCachedDataSource(int expireSeconds, String defaultName, 
                                     DataSource defaultDataSource, 
                                     NamedDataSource... othersDataSource) {
        // set the default data source
        this.defaultDataSource = defaultDataSource;

        this.localDataSources = ImmutableMap.copyOf(
            MultipleDataSourceContext.process(defaultName, defaultDataSource, othersDataSource)
        );

        this.strangerDataSources = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(expireSeconds))
            .maximumSize(8192)
            .<String, DataSource> removalListener(
                notification -> {
                    try {
                        Releasable.release(notification.getValue());
                    } catch (Exception e) {
                        Throwables.console(e); // ignored
                    }
                }
            )
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

        this.strangerDataSources.put(dataSourceName, datasource);
        MultipleDataSourceContext.add(dataSourceName);
    }

    public synchronized void remove(String dataSourceName) {
        if (this.localDataSources.containsKey(dataSourceName)) {
            throw new UnsupportedOperationException("Local datasource cannot remove: " + dataSourceName);
        }

        this.strangerDataSources.invalidate(dataSourceName);
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
        DataSource dataSource = this.localDataSources.get(name);
        if (dataSource != null) {
            return dataSource;
        }

        return this.strangerDataSources.getIfPresent(name);
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
        return this.localDataSources.containsKey(name) 
            || this.strangerDataSources.getIfPresent(name) != null;
    }

}
