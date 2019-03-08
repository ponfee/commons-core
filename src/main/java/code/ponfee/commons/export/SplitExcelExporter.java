package code.ponfee.commons.export;

import java.util.concurrent.ExecutorService;

/**
 * Export multiple excel file
 *
 * @author fupf
 */
public class SplitExcelExporter extends AbstractSplitExporter {

    public SplitExcelExporter(int batchSize, String savingFilePathPrefix,
                              ExecutorService executor) {
        super(batchSize, savingFilePathPrefix, ".xlsx", executor);
    }

    @Override
    protected AsnycSplitExporter splitExporter(Table<Object[]> subTable, String savingFilePath) {
        return new AsnycExcelExporter(subTable, savingFilePath, super.getName());
    }

    private static class AsnycExcelExporter extends AsnycSplitExporter {
        final String sheetName;

        AsnycExcelExporter(Table<Object[]> subTable, String savingFilePath,
                           String sheetName) {
            super(subTable, savingFilePath);
            this.sheetName = sheetName;
        }

        @Override
        protected AbstractDataExporter<?> createExporter() {
            AbstractDataExporter<?> excel = new ExcelExporter();
            excel.setName(sheetName);
            return excel;
        }

        @Override
        protected void complete(AbstractDataExporter<?> exporter) {
            ((ExcelExporter) exporter).write(savingFilePath);
        }
    }

}
