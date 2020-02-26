package code.ponfee.commons.data.lookup;

import static code.ponfee.commons.util.PropertiesUtils.getString;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidDataSource;

import code.ponfee.commons.base.Initializable;
import code.ponfee.commons.data.DataSources;
import code.ponfee.commons.data.NamedDataSource;
import code.ponfee.commons.io.Closeables;
import code.ponfee.commons.util.Strings;

/**
 * NamedDataSource array from properties configuration
 * 
 * @author Ponfee
 */
public class PropertiedNamedDataSourceArray implements Initializable, Closeable {

    private final NamedDataSource[] array;

    public PropertiedNamedDataSourceArray(Properties props) {
        this(null, props);
    }

    public PropertiedNamedDataSourceArray(String prefix, Properties props) {
        prefix = StringUtils.isBlank(prefix) ? "" : prefix.trim() + ".";
        Pattern pattern = Pattern.compile("^" + Strings.escapeRegex(prefix) + "(\\w+)\\.url$");

        List<String> names = props.keySet().stream().map(key -> {
            Matcher matcher = pattern.matcher(key.toString());
            return matcher.matches() ? matcher.group(1) : null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        String defaultDsName = getString(props, prefix + "default", names.get(0));
        String defaultType = getString(props, prefix + "type"); // default datasource type

        String dsType;
        List<NamedDataSource> dataSources = new ArrayList<>();
        for (String name : names) {
            // specify "{prefix}.{name}.type" of default "{prefix}.type" for datasource type
            dsType = getString(props, prefix + name + ".type", defaultType);
            NamedDataSource namedDs = NamedDataSource.of(
                name, DataSources.createDataSource(dsType, name, props, prefix)
            );
            if (defaultDsName.equals(name)) {
                dataSources.add(0, namedDs); // default ds at index 0
            } else {
                dataSources.add(namedDs);
            }
        }

        this.array = dataSources.toArray(new NamedDataSource[dataSources.size()]);
    }

    @Override
    public void init() {
        for (NamedDataSource nds : array) {
            try {
                ((DruidDataSource) nds.getDataSource()).init();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
        for (NamedDataSource nds : array) {
            Closeables.log((DruidDataSource) nds.getDataSource());
        }
    }

    public NamedDataSource[] getArray() {
        return array;
    }

}
