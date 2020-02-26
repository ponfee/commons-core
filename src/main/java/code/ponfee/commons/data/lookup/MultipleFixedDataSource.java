package code.ponfee.commons.data.lookup;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.data.NamedDataSource;

/**
 * Multiple DataSource: <p>
 *   {@linkplain #setTargetDataSources(Map)}：设置数据源集 <p>
 *   {@linkplain #setDefaultTargetDataSource(Object)}：设置默认的数据源 <p>
 *   {@linkplain #determineCurrentLookupKey()}：获取当前数据源， 当返回为空或无对应数据源时会使用defaultTargetDataSource <p>
 * 
 * @author Ponfee
 * @see MultipletScalableDataSource
 * @see MultipletCachedDataSource
 */
public class MultipleFixedDataSource extends AbstractRoutingDataSource implements DataSourceLookup {

    private final Map<String, DataSource> dataSources;

    public MultipleFixedDataSource(NamedDataSource dataSource) {
        this(dataSource.getName(), dataSource.getDataSource());
    }

    public MultipleFixedDataSource(NamedDataSource... dataSources) {
        this(
            dataSources[0].getName(), 
            dataSources[0].getDataSource(), 
            ArrayUtils.subarray(dataSources, 1, dataSources.length)
        );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MultipleFixedDataSource(String defaultName, DataSource defaultDataSource, 
                                   NamedDataSource... othersDataSource) {
        Map<String, DataSource> dataSources = MultipleDataSourceContext.process(
            defaultName, defaultDataSource, othersDataSource
        );

        // if determineCurrentLookupKey not get, then use this default
        super.setDefaultTargetDataSource(defaultDataSource);

        // set all the data sources
        super.setTargetDataSources((Map) dataSources);

        this.dataSources = ImmutableMap.copyOf(dataSources);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return MultipleDataSourceContext.get();
    }

    @Override
    public DataSource lookupDataSource(String name) {
        return this.dataSources.get(name);
    }

}
