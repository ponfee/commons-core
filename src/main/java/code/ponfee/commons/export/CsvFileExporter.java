package code.ponfee.commons.export;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import code.ponfee.commons.io.ByteOrderMarks;
import code.ponfee.commons.io.WrappedBufferedWriter;

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
