package code.ponfee.commons.export;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import code.ponfee.commons.io.WrappedBufferedWriter;

/**
 * Exports csv file
 * 
 * @author fupf
 */
public class CsvFileExporter extends AbstractCsvExporter<Void> {

    public CsvFileExporter(String filePath, boolean withBom) {
        this(new File(filePath), StandardCharsets.UTF_8, withBom);
    }

    public CsvFileExporter(File file, Charset charset, boolean withBom) {
        super(createWriter(file, charset, withBom));
    }

    public CsvFileExporter(char csvSeparator, File file, 
                           Charset charset, boolean withBom) {
        super(createWriter(file, charset, withBom), csvSeparator);
    }

    private static Appendable createWriter(File file, Charset charset, 
                                           boolean withBom) {
        try {
            WrappedBufferedWriter writer = new WrappedBufferedWriter(file, charset);
            if (withBom) {
                writer.write(WINDOWS_BOM);
            }
            return writer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Void export() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        ((WrappedBufferedWriter) super.csv).close();
    }

    @Override
    protected void flush() {
        try {
            ((WrappedBufferedWriter) super.csv).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
