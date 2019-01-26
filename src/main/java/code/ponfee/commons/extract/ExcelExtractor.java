package code.ponfee.commons.extract;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_NULL_AND_BLANK;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import code.ponfee.commons.util.Dates;

/**
 * Excel file data extractor
 * 
 * @author Ponfee
 */
public class ExcelExtractor<T> extends DataExtractor<T> {

    protected final ExcelType type;
    protected final int sheetIndex; // start with 0
    private final int startRow; // start with 0

    public ExcelExtractor(Object dataSource, String[] headers, 
                          int startRow, ExcelType type) {
        this(dataSource, headers, startRow, type, 0);
    }

    public ExcelExtractor(Object dataSource, String[] headers, 
                          int startRow, ExcelType type, int sheetIndex) {
        super(dataSource, headers);
        this.startRow = startRow;
        this.type = type;
        this.sheetIndex = sheetIndex;
    }

    @Override
    public final void extract(RowProcessor<T> processor) throws IOException {
        Workbook workbook = null;
        try {
            extract(workbook = createWorkbook(), processor);
        } finally {
            if (workbook != null) try {
                workbook.close();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            if (dataSource instanceof InputStream) try {
                ((InputStream) dataSource).close();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    protected Workbook createWorkbook() throws IOException {
        // sheet.getPhysicalNumberOfRows()
        if (dataSource instanceof File) {
            return WorkbookFactory.create((File) dataSource);
        } else {
            return WorkbookFactory.create((InputStream) dataSource);
        }
    }

    @SuppressWarnings("unchecked")
    private void extract(Workbook workbook, RowProcessor<T> processor) {
        boolean specHeaders; int columnSize;
        if (ArrayUtils.isNotEmpty(headers)) {
            specHeaders = true;
            columnSize = this.headers.length;
        } else {
            specHeaders = false;
            columnSize = 0;
        }

        Row row; T data; String[] array; String str;
        Iterator<Row> iter = workbook.getSheetAt(sheetIndex).iterator();
        for (int i = 0, k = 0, m, j; iter.hasNext(); i++) {
            row = iter.next(); // row = sheet.getRow(i);
            if (row == null || i < startRow) {
                continue;
            }

            if (!specHeaders && i == startRow) {
                columnSize = row.getLastCellNum(); // 不指定表头则以开始行为表头
            }

            array = columnSize > 1 ? new String[columnSize] : null;
            str = null;
            for (m = row.getLastCellNum(), j = 0; j <= m && j < columnSize; j++) {
                // Missing cells are returned as null, Blank cells are returned as normal
                str = getStringCellValue(row.getCell(j, RETURN_NULL_AND_BLANK));
                if (columnSize > 1) {
                    array[j] = str;
                }
            }
            if (columnSize > 1) {
                for (; j < columnSize; j++) {
                    array[j] = StringUtils.EMPTY;
                }
                data = (T) array;
            } else {
                data = (T) str;
            }
            if (isNotEmpty(data)) {
                processor.process(k++, data);
            }
        }
    }

    /**
     * 获取单元格的值
     * 
     * @param cell
     * @return
     */
    protected String getStringCellValue(Cell cell) {
        if (cell == null) {
            return StringUtils.EMPTY;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return Dates.format(cell.getDateCellValue());
                } else {
                    // Solve: Cannot get a STRING value from a NUMERIC cell
                    cell.setCellType(CellType.STRING);
                    return cell.getStringCellValue();
                }

            case STRING:
                return cell.getStringCellValue();

            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());

            case FORMULA:
                cell.setCellType(CellType.STRING);
                return cell.getStringCellValue();

            case BLANK: // 空值
            case ERROR: // 错误
            default:
                return StringUtils.EMPTY;
        }
    }

    public enum ExcelType {
        XLS, XLSX
    }
}
