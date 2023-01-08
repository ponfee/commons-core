/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.export;

import cn.ponfee.commons.io.ByteOrderMarks;
import cn.ponfee.commons.io.WrappedBufferedWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Exports csv string
 * 
 * @author Ponfee
 */
public class CsvStringExporter extends AbstractCsvExporter<String> {

    public CsvStringExporter() {
        this(0x2000);
    }

    public CsvStringExporter(int capacity) {
        super(new StringBuilder(capacity));
    }

    public CsvStringExporter(int capacity, char csvSeparator) {
        super(new StringBuilder(capacity), csvSeparator);
    }

    @Override
    public String export() {
        return csv.toString();
    }

    public void write(String filePath, Charset charset, boolean withBom) {
        File file = new File(filePath);
        try (WrappedBufferedWriter writer = new WrappedBufferedWriter(file, charset)) {
            byte[] bom;
            if (withBom && (bom = ByteOrderMarks.get(charset)) != null) {
                writer.write(bom);
            }
            writer.append((StringBuilder) super.csv);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
