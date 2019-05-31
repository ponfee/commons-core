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

package code.ponfee.commons.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

import code.ponfee.commons.collect.Collects;

/**
 * Abstract {@link javax.sql.DataSource} implementation that routes {@link #getConnection()}
 * calls to one of various target DataSources based on a lookup key. The latter is usually
 * (but not necessarily) determined through some thread-bound transaction context.
 * 
 * 
 * MultipletRoutingDataSource: 
 *   super.setTargetDataSources(map); // 设置数据源集
 *   super.setDefaultTargetDataSource(defaultDataSource); // 设置默认的数据源
 *   determineCurrentLookupKey() // 获取当前数据源， 当返回为空或无对应数据源时
 *                               // 会使用defaultTargetDataSource
 * 
 * @author Juergen Hoeller
 * @author Ponfee
 * @since 2.0.1
 * @see #setTargetDataSources
 * @see #setDefaultTargetDataSource
 * @see #determineCurrentLookupKey()
 */
public class MultipletRoutingDataSource extends AbstractDataSource {

    private final boolean lenientFallback;
    private final Map<String, DataSource> dataSources = new HashMap<>();
    private final DataSource defaultDataSource;

    public MultipletRoutingDataSource(NamedDataSource dataSource) {
        this(true, dataSource.getName(), dataSource.getDataSource());
    }

    public MultipletRoutingDataSource(NamedDataSource... dataSources) {
        this(true, dataSources);
    }

    public MultipletRoutingDataSource(boolean lenientFallback, NamedDataSource... dataSources) {
        this(
            lenientFallback,
            dataSources[0].getName(), 
            dataSources[0].getDataSource(), 
            ArrayUtils.subarray(dataSources, 1, dataSources.length)
        );
    }

    public MultipletRoutingDataSource(boolean lenientFallback, String defaultName, 
                                      DataSource defaultDataSource, 
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

        this.lenientFallback = lenientFallback;

        // if determineCurrentLookupKey not get, then use this default
        this.defaultDataSource = defaultDataSource;

        // 设置数据源集
        this.dataSources.put(defaultName, defaultDataSource);
        for (NamedDataSource ds : othersDataSource) {
            this.dataSources.put(ds.getName(), ds.getDataSource());
        }

        MultipleDataSourceContext.addAll(names);
    }

    /**
     * Determine the current lookup key. This will typically be
     * implemented to check a thread-bound transaction context.
     * <p>Allows for arbitrary keys. The returned key needs
     * to match the stored lookup key type, as resolved by the
     * {@link #resolveSpecifiedLookupKey} method.
     */
    protected Object determineCurrentLookupKey() {
        return MultipleDataSourceContext.get();
    }

    public synchronized void addDataSource(NamedDataSource ds) {
        this.addDataSource(ds.getName(), ds.getDataSource());
    }

    public synchronized void addDataSource(@Nonnull String dataSourceName, 
                                           @Nonnull DataSource datasource) {
        if (dataSources.containsKey(dataSourceName)) {
            throw new IllegalArgumentException("Duplicate datasource name: " + dataSourceName);
        }
        dataSources.put(dataSourceName, datasource);
        MultipleDataSourceContext.add(dataSourceName);
    }

    public synchronized void removeDataSource(String dataSourceName) {
        dataSources.remove(dataSourceName);
        MultipleDataSourceContext.remove(dataSourceName);
    }

    public synchronized void removeDataSource(DataSource dataSource) {
        for (Iterator<Entry<String, DataSource>> iter = dataSources.entrySet().iterator(); iter.hasNext();) {
            Entry<String, DataSource> entry = iter.next();
            if (dataSource.equals(entry.getValue())) {
                iter.remove();
                MultipleDataSourceContext.remove(entry.getKey());
            }
        }
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
        Assert.notNull(this.dataSources, "DataSource router not initialized");
        Object lookupKey = determineCurrentLookupKey();
        DataSource dataSource = this.dataSources.get(lookupKey);
        if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
            dataSource = this.defaultDataSource;
        }
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }
        return dataSource;
    }

}
