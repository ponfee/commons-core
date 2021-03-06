package code.ponfee.commons.data.lookup;

import code.ponfee.commons.base.Initializable;
import code.ponfee.commons.base.Releasable;
import code.ponfee.commons.data.DataSources;
import code.ponfee.commons.data.NamedDataSource;
import code.ponfee.commons.exception.Throwables;
import code.ponfee.commons.model.TypedMapWrapper;
import code.ponfee.commons.util.Strings;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        TypedMapWrapper<Object, Object> p = new TypedMapWrapper<>(props);
        String defaultDsName = p.getString(prefix + "default", names.get(0));
        String defaultType = p.getString(prefix + "type"); // default datasource type

        String dsType;
        List<NamedDataSource> dataSources = new ArrayList<>();
        for (String name : names) {
            // specify "{prefix}.{name}.type" of default "{prefix}.type" for datasource type
            dsType = p.getString(prefix + name + ".type", defaultType);
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
            Initializable.init(nds.getDataSource());
        }
    }

    @Override
    public void close() {
        for (NamedDataSource nds : array) {
            try {
                Releasable.release(nds.getDataSource());
            } catch (Exception e) {
                Throwables.console(e); // ignored
            }
        }
    }

    public NamedDataSource[] getArray() {
        return array;
    }

}
