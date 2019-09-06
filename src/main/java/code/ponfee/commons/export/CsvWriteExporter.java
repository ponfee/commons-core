package code.ponfee.commons.export;

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
