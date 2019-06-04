/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import code.ponfee.commons.cache.Cache;
import code.ponfee.commons.cache.CacheBuilder;
import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.data.NamedDataSource;

/**
 * Abstract {@link javax.sql.DataSource} implementation that routes {@link #getConnection()}
 * calls to one of various target DataSources based on a lookup key. The latter is usually
 * (but not necessarily) determined through some thread-bound transaction context.
 * 
 * @author Juergen Hoeller
 * @author Ponfee
 * @since 2.0.1
 * @see #setTargetDataSources
 * @see #setDefaultTargetDataSource
 * @see #determineCurrentLookupKey()
 */
public class MultipletCachedDataSource extends AbstractDataSource {

    private final Cache<DataSource> dataSources = CacheBuilder.newBuilder()
        .autoReleaseInSeconds(3600).caseSensitiveKey(true).keepaliveInMillis(7200000).build();
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

        MultipleDataSourceContext.addAll(names);
    }

    public synchronized void add(NamedDataSource ds, long expireTimeMillis) {
        this.add(ds.getName(), ds.getDataSource(), expireTimeMillis);
    }

    public synchronized void addIfAbsent(String dataSourceName, Supplier<DataSource> supplier, 
                                         long expireTimeMillis) {
        if (!this.dataSources.containsKey(dataSourceName)) {
            this.add(dataSourceName, supplier.get(), expireTimeMillis);
        }
    }

    public synchronized void add(@Nonnull String dataSourceName, @Nonnull DataSource datasource,
                                 long expireTimeMillis) {
        Assert.isTrue(expireTimeMillis > 0, "ExpireTimeMillis must greater than 0.");
        if (dataSources.containsKey(dataSourceName)) {
            throw new IllegalArgumentException("Duplicated name: " + dataSourceName);
        }
        dataSources.set(dataSourceName, datasource, expireTimeMillis);
        MultipleDataSourceContext.add(dataSourceName);
    }

    public synchronized void remove(String dataSourceName) {
        dataSources.getAndRemove(dataSourceName);
        MultipleDataSourceContext.remove(dataSourceName);
    }

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
