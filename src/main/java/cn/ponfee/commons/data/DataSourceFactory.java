/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.data;

import cn.ponfee.commons.reflect.BeanMaps;

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
