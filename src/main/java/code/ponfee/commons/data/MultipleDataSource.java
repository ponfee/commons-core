package code.ponfee.commons.data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.google.common.collect.ImmutableList;

/**
 * Multiple DataSource: 
 *   super.setTargetDataSources(map); // 设置数据源集
 *   super.setDefaultTargetDataSource(defaultDataSource); // 设置默认的数据源
 *   determineCurrentLookupKey() // 获取当前数据源， 当返回为空或无对应数据源时
 *                               // 会使用defaultTargetDataSource
 * 
 * @author Ponfee
 */
public class MultipleDataSource extends AbstractRoutingDataSource {

    public MultipleDataSource(NamedDataSource dataSource) {
        this(dataSource.getName(), dataSource.getDataSource());
    }

    public MultipleDataSource(NamedDataSource... dataSources) {
        this(
            dataSources[0].getName(), 
            dataSources[0].getDataSource(), 
            ArrayUtils.subarray(dataSources, 1, dataSources.length)
        );
    }

    public MultipleDataSource(String defaultName, DataSource defaultDataSource, 
                              NamedDataSource... othersDataSource) {
        if (othersDataSource == null) {
            othersDataSource = new NamedDataSource[0];
        }
        List<String> names = Arrays.stream(othersDataSource)
                                   .map(NamedDataSource::getName)
                                   .collect(Collectors.toList());
        names.add(0, defaultName); // default data source at the first

        // checks whether duplicate datasource name
        Set<String> duplicates = names.stream().collect(
            Collectors.groupingBy(Function.identity(), Collectors.counting())
        ).entrySet().stream().filter(
            e -> (e.getValue() > 1)
        ).map(
            Entry::getKey
        ).collect(
            Collectors.toSet()
        );
        if (CollectionUtils.isNotEmpty(duplicates)) {
            throw new IllegalArgumentException("Duplicated data source name: " + duplicates.toString());
        }

        // to map
        Map<Object, Object> map = Arrays.stream(othersDataSource).collect(
            Collectors.toMap(NamedDataSource::getName, NamedDataSource::getDataSource)
        );
        map.put(defaultName, defaultDataSource);

        super.setTargetDataSources(map); // 设置数据源集

        // if determineCurrentLookupKey not get, then use this default
        super.setDefaultTargetDataSource(defaultDataSource);

        MultipleDataSourceContext.dataSourceKeys = ImmutableList.copyOf(names);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return MultipleDataSourceContext.get();
    }

}
