package code.ponfee.commons.data;

import code.ponfee.commons.util.Asserts;
import code.ponfee.commons.util.PropertiesUtils;
import code.ponfee.commons.util.Strings;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Named Data Source
 * 
 * @author Ponfee
 */
public class NamedDataSource {

    private static final Pattern PATTERN_DBURL_KEY = Pattern.compile("^(\\w+)\\.url$");

    private final String name;
    private final DataSource dataSource;

    public NamedDataSource(String name, DataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
    }

    public static NamedDataSource of(String name, DataSource dataSource) {
        return new NamedDataSource(name, dataSource);
    }

    public String getName() {
        return name;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     *
     * @param prefix
     * @param props
     * @return
     */
    public static NamedDataSource[] build(String prefix, Properties props) {
        prefix = StringUtils.isBlank(prefix) ? "" : prefix.trim() + ".";
        props = PropertiesUtils.filterProperties(props, prefix);

        Set<String> names = new LinkedHashSet<>();
        props.forEach((k, v) -> {
            Matcher matcher = PATTERN_DBURL_KEY.matcher(k.toString());
            if (matcher.matches()) {
                String name = matcher.group(1);
                if (!names.add(name)) {
                    throw new IllegalStateException("Duplicated datasource name '" + name + "'.");
                }
            }
        });
        Asserts.isTrue(!names.isEmpty(), "Not configured datasource 'name' option.");

        Pattern pattern = Pattern.compile("^(?!(" + Strings.join(names, "|") + ")\\.).*");
        Properties commonConfig = new Properties(); // commons properties
        props.entrySet().stream()
             .filter(x -> pattern.matcher(x.toString()).matches())
             .forEach(x -> commonConfig.put(x.getKey(), x.getValue()));
        String defaultDsName = (String) commonConfig.remove("default"); // default datasource name
        String defaultType = (String) commonConfig.remove("type"); // default datasource type

        List<NamedDataSource> dataSources = new LinkedList<>();
        for (String name : names) {
            // specify "{name}.type" or default "type" for datasource type
            String dsType = Strings.ifEmpty((String) props.remove(name + ".type"), defaultType);
            Properties basicConfig = PropertiesUtils.filterProperties(props, name + ".");
            DataSource dataSource = DataSourceFactory.COMMON_FACTORY.create(dsType, basicConfig, commonConfig);

            NamedDataSource namedDs = NamedDataSource.of(name, dataSource);
            if (name.equals(defaultDsName)) {
                dataSources.add(0, namedDs); // default ds at index 0
            } else {
                dataSources.add(namedDs);
            }
        }

        return dataSources.toArray(new NamedDataSource[dataSources.size()]);
    }
}
