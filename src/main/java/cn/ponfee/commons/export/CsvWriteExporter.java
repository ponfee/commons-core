/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.export;

import java.io.Writer;

/**
 * Exports csv wirte
 * 
 * @author Ponfee
 */
public class CsvWriteExporter extends AbstractCsvExporter<Void> {

    public CsvWriteExporter(Writer writer) {
        super(writer);
    }

    public CsvWriteExporter(Writer writer, char csvSeparator) {
        super(writer, csvSeparator);
    }

    @Override
    public Void export() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        try {
            ((Writer) super.csv).close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
