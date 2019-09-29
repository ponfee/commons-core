package code.ponfee.commons.data.lookup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableSet;

import code.ponfee.commons.cache.Cache;
import code.ponfee.commons.cache.CacheBuilder;
import code.ponfee.commons.collect.Collects;
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

    private final Set<String> unremovedDataSourceNames;
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
        if (othersDataSource == null) {
            othersDataSource = new NamedDataSource[0];
        }
        List<String> names = Arrays.stream(othersDataSource)
                                   .map(NamedDataSource::getName)
                                   .collect(Collectors.toList());
        names.add(0, defaultName); // default data source at the first

        // checks whether duplicate datasource name
        Set<String> duplicates = Collects.duplicate(names);
        if (CollectionUtils.isNotEmpty(duplicates)) {
            throw new IllegalArgumentException("Duplicated data source name: " + duplicates.toString());
        }

        // if determineCurrentLookupKey not get, then use this default
        this.defaultDataSource = defaultDataSource;

        // 设置数据源集（初始设置的数据源永不失效）
        this.dataSources.set(defaultName, defaultDataSource, Cache.KEEPALIVE_FOREVER);
        for (NamedDataSource ds : othersDataSource) {
            this.dataSources.set(ds.getName(), ds.getDataSource(), Cache.KEEPALIVE_FOREVER);
        }

        unremovedDataSourceNames = ImmutableSet.copyOf(names);
        MultipleDataSourceContext.addAll(names);
    }

    // -----------------------------------------------------------------add/remove
    public synchronized void addIfAbsent(String dataSourceName, Supplier<DataSource> supplier, 
                                         long expireTimeMillis) {
        if (!this.dataSources.containsKey(dataSourceName)) {
            this.add(dataSourceName, supplier.get(), expireTimeMillis);
        }
    }

    public synchronized void addIfAbsent(String dataSourceName, DataSource datasource,
                                         long expireTimeMillis) {
        if (!this.dataSources.containsKey(dataSourceName)) {
            this.add(dataSourceName, datasource, expireTimeMillis);
        }
    }

    public synchronized void add(NamedDataSource ds, long expireTimeMillis) {
        this.add(ds.getName(), ds.getDataSource(), expireTimeMillis);
    }

    public synchronized void add(@Nonnull String dataSourceName, 
                                 @Nonnull DataSource datasource,
                                 long expireTimeMillis) {
        Assert.isTrue(expireTimeMillis > 0, "ExpireTimeMillis must greater than 0.");
        if (dataSources.containsKey(dataSourceName)) {
            throw new IllegalArgumentException("Duplicated name: " + dataSourceName);
        }
        dataSources.set(dataSourceName, datasource, expireTimeMillis);
        MultipleDataSourceContext.add(dataSourceName);
    }

    public synchronized void remove(String dataSourceName) {
        if (unremovedDataSourceNames.contains(dataSourceName)) {
            throw new UnsupportedOperationException("Inited datasource cannot remove: " + dataSourceName);
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
    protected DataSource determineTargetDataSource() {
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
