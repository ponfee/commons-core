/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.extract;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * The extractable DataSource is a Inputstream or File
 * 
 * @author Ponfee
 */
public class ExtractableDataSource implements Closeable {

    private final Object dataSource;

    public ExtractableDataSource(@Nonnull Object dataSource) {
        if (!(dataSource instanceof InputStream || dataSource instanceof File)) {
            throw new IllegalArgumentException(
                "Invalid datasource '" + dataSource.getClass().getName() + "', only support File or InputStream."
            );
        }
        this.dataSource = dataSource;
    }

    @Override
    public void close() throws IOException {
        if (dataSource instanceof InputStream) {
            ((InputStream) dataSource).close();
        }
    }

    public Object getDataSource() {
        return dataSource;
    }

    public InputStream asInputStream() throws IOException {
        return dataSource instanceof File
             ? new FileInputStream((File) dataSource)
             : (InputStream) dataSource;
    }

}
