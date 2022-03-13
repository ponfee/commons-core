package code.ponfee.commons.export;

import java.util.concurrent.Executor;

/**
 * Export multiple excel file
 *
 * @author Ponfee
 */
public class SplitExcelExporter extends AbstractSplitExporter {

    public SplitExcelExporter(int batchSize, String savingFilePathPrefix, Executor executor) {
        super(batchSize, savingFilePathPrefix, ".xlsx", executor);
    }

    @Override
    protected AbstractAsyncSplitExporter splitExporter(Table<Object[]> subTable, String savingFilePath) {
        return new AsnycExcelExporter(subTable, savingFilePath, super.getName());
    }

    private static class AsnycExcelExporter extends AbstractAsyncSplitExporter {
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
