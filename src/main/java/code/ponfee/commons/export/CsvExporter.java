package code.ponfee.commons.export;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import code.ponfee.commons.io.WrappedBufferedWriter;

/**
 * csv导出
 * 
 * @author fupf
 */
public class CsvExporter extends AbstractCsvExporter<String> {

    public CsvExporter() {
        this(0x2000);
    }

    public CsvExporter(int capacity) {
        super(new StringBuilder(capacity));
    }

    public CsvExporter(int capacity, char csvSeparator) {
        super(new StringBuilder(capacity), csvSeparator);
    }

    @Override
    public String export() {
        return csv.toString();
    }

    public void write(String filePath, Charset charset, boolean withBom) {
        File file = new File(filePath);
        try (WrappedBufferedWriter writer = new WrappedBufferedWriter(file, charset)) {
            if (withBom) {
                writer.write(WINDOWS_BOM);
            }
            writer.append((StringBuilder) super.csv);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        //((StringBuilder) super.csv).setLength(0);
    }

}
