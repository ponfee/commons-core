package code.ponfee.commons.data.lookup;

import code.ponfee.commons.base.Initializable;
import code.ponfee.commons.base.Releasable;
import code.ponfee.commons.data.NamedDataSource;
import code.ponfee.commons.exception.Throwables;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.io.Closeable;
import java.util.Map;

/**
 * 固定的多数据源类型：一旦创建则不可再变
 *
 * Multiple DataSource: <p>
 *   {@linkplain #setTargetDataSources(Map)}：设置数据源集 <p>
 *   {@linkplain #setDefaultTargetDataSource(Object)}：设置默认的数据源 <p>
 *   {@linkplain #determineCurrentLookupKey()}：获取当前数据源， 当返回为空或无对应数据源时会使用defaultTargetDataSource <p>
 * 
 * @author Ponfee
 * @see MultipleScalableDataSource
 * @see MultipleCachedDataSource
 */
public class MultipleFixedDataSource extends AbstractRoutingDataSource
    implements DataSourceLookup, Initializable, Closeable {

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

    @Override
    public void init() {
        dataSources.forEach((name, ds) -> Initializable.init(ds));
    }

    @Override
    public void close() {
        dataSources.forEach((name, ds) -> {
            try {
                Releasable.release(ds);
            } catch (Exception e) {
                Throwables.console(e);
            }
        });
    }

}
