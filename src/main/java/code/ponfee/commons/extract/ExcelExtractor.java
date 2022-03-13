package code.ponfee.commons.extract;

import code.ponfee.commons.util.Dates;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.BiConsumer;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_NULL_AND_BLANK;

/**
 * Excel file data extractor
 * 
 * <a href="https://www.cnblogs.com/cksvsaaa/p/7280261.html">xlsx big file import</a>
 * 
 * @author Ponfee
 */
public class ExcelExtractor extends DataExtractor {

    protected final ExcelType type;
    protected final int sheetIndex; // start with 0
    private final int startRow;     // start with 0

    protected ExcelExtractor(ExtractableDataSource dataSource, String[] headers, 
                             int startRow, ExcelType type, int sheetIndex) {
        super(dataSource, headers);
        this.startRow = startRow;
        this.type = type;
        this.sheetIndex = sheetIndex;
    }

    @Override
    public final void extract(BiConsumer<Integer, String[]> processor) throws IOException {
        try (ExtractableDataSource ds = dataSource; Workbook workbook = createWorkbook(ds)) {
            extract(workbook, processor);
        }
    }

    protected Workbook createWorkbook(ExtractableDataSource dataSource) throws IOException {
        Object ds = dataSource.getDataSource();
        if (ds instanceof File) {
            return WorkbookFactory.create((File) ds);
        } else {
            return WorkbookFactory.create((InputStream) ds);
        }
    }

    private void extract(Workbook workbook, BiConsumer<Integer, String[]> processor) {
        boolean specHeaders; int columnSize;
        if (ArrayUtils.isNotEmpty(headers)) {
            specHeaders = true;
            columnSize = this.headers.length;
        } else {
            specHeaders = false;
            columnSize = 0;
        }

        Row row; String[] data;
        // sheet.getPhysicalNumberOfRows()
        Iterator<Row> iter = workbook.getSheetAt(sheetIndex).iterator();
        for (int i = 0, k = 0, m, j; iter.hasNext(); i++) {
            if (super.end) {
                break;
            }
            row = iter.next(); // row = sheet.getRow(i);
            if (row == null || i < startRow) {
                continue;
            }

            if (!specHeaders && i == startRow) {
                columnSize = row.getLastCellNum(); // 不指定表头则以开始行为表头
            }

            data = new String[columnSize];
            for (m = row.getLastCellNum(), j = 0; j <= m && j < columnSize; j++) {
                // Missing cells are returned as null, Blank cells are returned as normal
                data[j] = getStringCellValue(row.getCell(j, RETURN_NULL_AND_BLANK));
            }
            for (; j < columnSize; j++) {
                data[j] = null; // padding
            }
            if (isNotEmpty(data)) {
                processor.accept(k++, data);
            }
        }
    }

    /**
     * 获取单元格的值
     * 
     * @param cell
     * @return
     */
    private static String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return getNumericAsString(cell);

            case FORMULA:
                try {
                    return getNumericAsString(cell);
                } catch (Exception e) {
                    return cell.getRichStringCellValue().getString();
                }

            case STRING:
                return cell.getStringCellValue();

            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());

            case ERROR: // 错误
                return "Error: " + cell.getErrorCellValue();

            default:
                return cell.getRichStringCellValue().getString();
        }
    }

    private static String getNumericAsString(Cell cell) {
        return (DateUtil.isCellDateFormatted(cell) || DateUtil.isCellInternalDateFormatted(cell))
             ? Dates.format(cell.getDateCellValue()) : String.valueOf(cell.getNumericCellValue());
    }

    public enum ExcelType {
        XLS, XLSX
    }

}
