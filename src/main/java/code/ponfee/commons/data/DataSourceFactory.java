package code.ponfee.commons.data;

import code.ponfee.commons.reflect.BeanMaps;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * DataSource factory interface
 *
 * @author Ponfee
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public interface DataSourceFactory<T extends DataSource> {

    default void configure(T dataSource, Properties commonConfig) {
        BeanMaps.PROPS.copyFromMap((Map) commonConfig, dataSource);
    }

    default T create(String dataSourceClassName, Properties basicConfig, Properties commonConfig) {
        T dataSource;
        try {
            dataSource = (T) Class.forName(dataSourceClassName).newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        BeanMaps.PROPS.copyFromMap((Map) basicConfig, dataSource);

        configure(dataSource, commonConfig);

        return dataSource;
    }

    DataSourceFactory COMMON_FACTORY = new DataSourceFactory() {};
}
