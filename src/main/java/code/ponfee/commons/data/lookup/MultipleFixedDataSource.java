package code.ponfee.commons.data.lookup;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import code.ponfee.commons.collect.Collects;
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
public class MultipleFixedDataSource extends AbstractRoutingDataSource {

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

    public MultipleFixedDataSource(String defaultName, DataSource defaultDataSource, 
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
        super.setDefaultTargetDataSource(defaultDataSource);

        // set datasources
        Map<Object, Object> dataSources = Arrays.stream(othersDataSource).collect(
            Collectors.toMap(NamedDataSource::getName, NamedDataSource::getDataSource)
        );
        dataSources.put(defaultName, defaultDataSource);
        super.setTargetDataSources(dataSources);

        MultipleDataSourceContext.addAll(names);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return MultipleDataSourceContext.get();
    }

}
