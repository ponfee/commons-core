package code.ponfee.commons.data.lookup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableSet;

import code.ponfee.commons.cache.Cache;
import code.ponfee.commons.cache.CacheBuilder;
import code.ponfee.commons.data.NamedDataSource;

/**
 * 可动态增加/移除数据源/数据源自动超时失效
 * 
 * @author Ponfee
 * @see org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
 */
public class MultipletCachedDataSource extends AbstractDataSource {

    private final Cache<String, DataSource> dataSources = CacheBuilder.<String, DataSource>newBuilder()
        .autoReleaseInSeconds(300).caseSensitiveKey(true)
        .removalListener(nf -> MultipleDataSourceContext.remove(nf.getKey()))
        .build();

    private final Set<String> immutableDataSourceNames;
    private final DataSource defaultDataSource;

    public MultipletCachedDataSource(NamedDataSource dataSource) {
        this(dataSource.getName(), dataSource.getDataSource());
    }

    public MultipletCachedDataSource(NamedDataSource... dataSources) {
        this(
            dataSources[0].getName(), 
            dataSources[0].getDataSource(), 
            ArrayUtils.subarray(dataSources, 1, dataSources.length)
        );
    }

    public MultipletCachedDataSource(String defaultName, DataSource defaultDataSource, 
                                     NamedDataSource... othersDataSource) {
        Map<String, DataSource> dataSources = MultipleDataSourceContext.process(
            defaultName, defaultDataSource, othersDataSource
        );

        // set the default data source
        this.defaultDataSource = defaultDataSource;

        // set all the data sources to cache container(inited data sources not expire)
        dataSources.forEach(
            (k, v) -> this.dataSources.set(k, v, Cache.KEEPALIVE_FOREVER)
        );

        this.immutableDataSourceNames = ImmutableSet.copyOf(dataSources.keySet());
    }

    // -----------------------------------------------------------------add/remove
    public synchronized boolean addIfAbsent(String dataSourceName, Supplier<DataSource> supplier, 
                                            long expireTimeMillis) {
        if (this.dataSources.containsKey(dataSourceName)) {
            return false;
        }

        this.add(dataSourceName, supplier.get(), expireTimeMillis);
        return true;
    }

    public synchronized boolean addIfAbsent(String dataSourceName, DataSource datasource,
                                            long expireTimeMillis) {
        if (this.dataSources.containsKey(dataSourceName)) {
            return false;
        }

        this.add(dataSourceName, datasource, expireTimeMillis);
        return true;
    }

    public synchronized void add(NamedDataSource ds, long expireTimeMillis) {
        this.add(ds.getName(), ds.getDataSource(), expireTimeMillis);
    }

    public synchronized void add(@Nonnull String dataSourceName, 
                                 @Nonnull DataSource datasource,
                                 long expireTimeMillis) {
        Assert.isTrue(expireTimeMillis >= 0, "ExpireTimeMillis must be >= 0.");
        if (dataSources.containsKey(dataSourceName)) { // check the datasource name not exists
            throw new IllegalArgumentException("Duplicated datasource name: " + dataSourceName);
        }

        dataSources.set(dataSourceName, datasource, expireTimeMillis);
        MultipleDataSourceContext.add(dataSourceName);
    }

    public synchronized void remove(String dataSourceName) {
        if (immutableDataSourceNames.contains(dataSourceName)) {
            throw new UnsupportedOperationException("Immutable datasource cannot remove: " + dataSourceName);
        }
        dataSources.remove(dataSourceName);
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
        return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
    }

    /**
     * Retrieve the current target DataSource. Determines the
     * {@link #determineCurrentLookupKey() current lookup key}, performs
     * a lookup in the {@link #setTargetDataSources targetDataSources} map,
     * falls back to the specified
     * {@link #setDefaultTargetDataSource default target DataSource} if necessary.
     * @see #determineCurrentLookupKey()
     */
    public DataSource determineTargetDataSource() {
        String lookupKey = MultipleDataSourceContext.get();
        DataSource dataSource = (lookupKey == null) 
                              ? this.defaultDataSource 
                              : this.dataSources.get(lookupKey);
        if (dataSource == null) {
            throw new IllegalStateException("Cannot found DataSource for name [" + lookupKey + "]");
        }
        return dataSource;
    }

}
