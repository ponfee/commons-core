package code.ponfee.commons.export;

import java.util.concurrent.ExecutorService;

/**
 * Export multiple csv file
 *
 * @author fupf
 */
public class SplitCsvFileExporter extends AbstractSplitExporter {

    private final boolean withBom;

    public SplitCsvFileExporter(int batchSize, String savingFilePathPrefix,
                                boolean withBom, ExecutorService executor) {
        super(batchSize, savingFilePathPrefix, ".csv", executor);
        this.withBom = withBom;
    }

    @Override
    protected AsnycSplitExporter splitExporter(Table<Object[]> subTable, String savingFilePath) {
        return new AsnycCsvFileExporter(subTable, savingFilePath, withBom);
    }

    private static class AsnycCsvFileExporter extends AsnycSplitExporter {
        final boolean withBom;

        AsnycCsvFileExporter(Table<Object[]> subTable, String savingFilePath,
                             boolean withBom) {
            super(subTable, savingFilePath);
            this.withBom = withBom;
        }

        @Override
        protected AbstractDataExporter<?> createExporter() {
            return new CsvFileExporter(savingFilePath, withBom);
        }
    }

}
