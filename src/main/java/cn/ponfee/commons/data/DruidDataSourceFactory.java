/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.data;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Druid DataSource factory
 *
 * @author Ponfee
 */
public class DruidDataSourceFactory implements DataSourceFactory<DruidDataSource> {

    @Override
    public void configure(DruidDataSource ds, Properties props) {
        configureFilters(ds, props.getProperty("filters"));
    }

    private void configureFilters(DruidDataSource ds, String filters) {
        if (StringUtils.isBlank(filters)) {
            return;
        }

        filters = filters.trim();
        boolean force = filters.startsWith("!");
        if (force) {
            filters = filters.substring(1);
        }

        List<String> list = Arrays.stream(filters.split(","))
                                  .map(String::trim)
                                  .collect(Collectors.toList());
        boolean hasWall = list.remove("wall");
        if (hasWall) {
            WallConfig wallConfig = new WallConfig();
            wallConfig.setCommentAllow(true);
            wallConfig.setMultiStatementAllow(true);
            WallFilter wallFilter = new WallFilter();
            wallFilter.setConfig(wallConfig);
            ds.setProxyFilters(Collections.singletonList(wallFilter));
        }

        filters = (force ? "!" : "") + String.join(",", list);
        try {
            ds.setFilters(filters);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
