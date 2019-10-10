package code.ponfee.commons.data;

import javax.sql.DataSource;

/**
 * Named Data Source
 * 
 * @author Ponfee
 */
public class NamedDataSource {

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

}
