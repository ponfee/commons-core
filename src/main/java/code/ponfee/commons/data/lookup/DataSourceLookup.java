package code.ponfee.commons.data.lookup;

import javax.sql.DataSource;

/**
 * Looking up DataSources by name.
 * 
 * @author Ponfee
 */
public interface DataSourceLookup {

    DataSource lookupDataSource(String name);
}
