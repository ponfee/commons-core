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
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Exports csv file
 * 
 * @author Ponfee
 */
public class CsvFileExporter extends CsvWriteExporter {

    public CsvFileExporter(String filePath, boolean withBom) throws IOException {
        this(new File(filePath), StandardCharsets.UTF_8, withBom);
    }

    public CsvFileExporter(File file, Charset charset, boolean withBom) throws IOException {
        super(createWriter(file, charset, withBom));
    }

    public CsvFileExporter(File file, Charset charset, boolean withBom, char csvSeparator) throws IOException {
        super(createWriter(file, charset, withBom), csvSeparator);
    }

    private static Writer createWriter(File file, Charset charset, boolean withBom) throws IOException {
        WrappedBufferedWriter writer = new WrappedBufferedWriter(file, charset);
        byte[] bom;
        if (withBom && (bom = ByteOrderMarks.get(charset)) != null) {
            writer.write(bom);
        }
        return writer;
    }

}
