package code.ponfee.commons.export;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import code.ponfee.commons.io.ByteOrderMarks;
import code.ponfee.commons.io.WrappedBufferedWriter;

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

    @Override
    public void close() {
        //((StringBuilder) super.csv).setLength(0);
    }

}
