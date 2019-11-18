package code.ponfee.commons.data.lookup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.AbstractDataSource;

import code.ponfee.commons.data.NamedDataSource;

/**
 * 可动态增加/移除数据源
 * 
 * @author Ponfee
 * @see org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
 */
public class MultipletScalableDataSource extends AbstractDataSource {

    private final Map<String, DataSource> dataSources = new HashMap<>();
    private final DataSource defaultDataSource;

    public MultipletScalableDataSource(NamedDataSource dataSource) {
        this(dataSource.getName(), dataSource.getDataSource());
    }

    public MultipletScalableDataSource(NamedDataSource... dataSources) {
        this(
            dataSources[0].getName(), 
            dataSources[0].getDataSource(), 
            ArrayUtils.subarray(dataSources, 1, dataSources.length)
        );
    }

    public MultipletScalableDataSource(String defaultName, DataSource defaultDataSource, 
                                       NamedDataSource... othersDataSource) {
        Map<String, DataSource> dataSources = MultipleDataSourceContext.process(
            defaultName, defaultDataSource, othersDataSource
        );

        // set the default data source
        this.defaultDataSource = defaultDataSource;

        // set all the data sources
        this.dataSources.putAll(dataSources);
    }

    public synchronized void add(NamedDataSource ds) {
        this.add(ds.getName(), ds.getDataSource());
    }

    public synchronized void add(@Nonnull String dataSourceName, @Nonnull DataSource datasource) {
        if (dataSources.containsKey(dataSourceName)) {
            throw new IllegalArgumentException("Duplicated name: " + dataSourceName);
        }
        dataSources.put(dataSourceName, datasource);
        MultipleDataSourceContext.add(dataSourceName);
    }

    public synchronized void remove(String dataSourceName) {
        dataSources.remove(dataSourceName);
        MultipleDataSourceContext.remove(dataSourceName);
    }

    public synchronized void remove(@Nonnull DataSource dataSource) {
        Objects.requireNonNull(dataSource);

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
