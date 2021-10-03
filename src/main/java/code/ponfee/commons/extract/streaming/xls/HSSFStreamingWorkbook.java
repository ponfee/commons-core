package code.ponfee.commons.extract.streaming.xls;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.CellRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * The version for 2003 or early XSL excel file streaming reader excel workbook
 *
 * @author Ponfee
 */
public class HSSFStreamingWorkbook implements Workbook, Closeable {

    public static final int AWAIT_MILLIS = 47;

    private volatile boolean allSheetReadied = false;
    private final List<Sheet> sheets = new ArrayList<>();

    public HSSFStreamingWorkbook(InputStream input, int rowCacheSize,
                                 int[] sheetIndexs, String[] sheetNames,
                                 ExecutorService executor) {
        executor.submit(new AsyncHSSFReader(
            rowCacheSize, sheetIndexs, sheetNames, input
        ));
    }

    @Override
    public Iterator<Sheet> iterator() {
        awaitReadAllSheet();
        return sheets.iterator();
    }

    @Override
    public Iterator<Sheet> sheetIterator() {
        awaitReadAllSheet();
        return iterator();
    }

    @Override
    public String getSheetName(int sheet) {
        awaitReadAllSheet();
        return sheets.get(sheet).getSheetName();
    }

    @Override
    public int getSheetIndex(String name) {
        awaitReadAllSheet();
        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i).getSheetName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSheetIndex(Sheet sheet) {
        awaitReadAllSheet();
        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i) == sheet) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getNumberOfSheets() {
        awaitReadAllSheet();
        return sheets.size();
    }

    @Override
    public Sheet getSheetAt(int index) {
        awaitReadAllSheet();
        return sheets.size() > index ? sheets.get(index) : null;
    }

    @Override
    public Sheet getSheet(String name) {
        awaitReadAllSheet();
        for (Sheet sheet : sheets) {
            if (sheet.getSheetName().equals(name)) {
                return sheet;
            }
        }
        return null;
    }

    @Override
    public void close() {
        // do nothing
    }

    private void awaitReadAllSheet() {
        try {
            while (!allSheetReadied) {
                Thread.sleep(AWAIT_MILLIS);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs in alone thread
     */
    private class AsyncHSSFReader implements HSSFListener, Runnable {
        private final InputStream input;
        private final int rowCacheSize;
        private final int[] sheetIndexs;
        private final String[] sheetNames;

        private int currentSheetIndex = -1; // start with 0
        private HSSFStreamingSheet currentSheet;

        private int currentRowNumber = -1; // start with 0
        private int currentRowOrder = -1; // start with 0
        private HSSFStreamingRow currentRow;


        private SSTRecord sstrec;
        private FormatTrackingHSSFListener formatListener;

        private AsyncHSSFReader(int rowCacheSize, int[] sheetIndexs,
                                String[] sheetNames, InputStream input) {
            this.rowCacheSize = rowCacheSize;
            this.sheetIndexs = sheetIndexs;
            this.sheetNames = sheetNames;
            this.input = input;
        }

        @Override
        public void run() {
            try (InputStream steam = input;
                 POIFSFileSystem poi = new POIFSFileSystem(steam);
                 DocumentInputStream doc = poi.createDocumentInputStream("Workbook")
            ) {
                HSSFRequest request = new HSSFRequest();
                formatListener = new FormatTrackingHSSFListener(
                    new MissingRecordAwareHSSFListener(this)
                );
                request.addListenerForAllRecords(formatListener);
                new HSSFEventFactory().processEvents(request, doc);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                this.endRead(); // read end of xls file
            }
        }

        /**
         * This method listens for incoming records and handles them as required.
         *
         * @param record the record that was found while reading.
         */
        @Override
        public void processRecord(Record record) {
            if (record instanceof BOFRecord) { // beginning of a sheet or the workbook
                if (((BOFRecord) record).getType() == BOFRecord.TYPE_WORKSHEET) { // beginning a sheet
                    allSheetReadied = true;
                    if (currentSheet != null) {
                        putRow(currentRow);
                        currentSheet.toEnd();
                    }
                    currentRow = null;
                    currentRowNumber = -1;
                    currentRowOrder = -1; // reset current row
                    currentSheet = (HSSFStreamingSheet) sheets.get(++currentSheetIndex);
                } else {
                    // BOFRecord.TYPE_WORKBOOK: beginning the workbook
                    // others uncapture ...
                }
            } else if (record instanceof BoundSheetRecord) { // the workbook all of sheet
                BoundSheetRecord bsr = (BoundSheetRecord) record;
                int sstIdx = sheets.size();
                sheets.add(new HSSFStreamingSheet(
                        sstIdx, bsr.getSheetname(), isDiscard(sstIdx, bsr.getSheetname()), rowCacheSize
                ));
            } else if (record instanceof SSTRecord) { // store a array of unique strings used in Excel.
                sstrec = (SSTRecord) record;
            } else if (record instanceof CellRecord) { // excel cell
                CellRecord cell = (CellRecord) record;
                if (currentRowNumber != cell.getRow()) { // new row
                    putRow(currentRow);
                    currentRowNumber = cell.getRow();
                    currentRow = new HSSFStreamingRow(currentRowNumber, ++currentRowOrder);
                }
                currentRow.putCell(cell.getColumn(), new HSSFStreamingCell(getString(cell)));
            } else {
                // RowRecord: batch row loading
                // MissingCellDummyRecord: missing cell
                // LastCellOfRowDummyRecord: last cell
                // others ...
            }
        }

        private void endRead() {
            allSheetReadied = true;
            if (currentSheet != null) {
                putRow(currentRow); // last row
            }
            sheets.forEach(s -> ((HSSFStreamingSheet) s).toEnd());
        }

        private void putRow(HSSFStreamingRow row) {
            if (   this.currentSheet.isDiscard()
                || row == null || row.isEmpty()
            ) {
                return;
            }
            try {
                this.currentSheet.putRow(row);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean isDiscard(int sstIdx, String sstName) {
            if (   ArrayUtils.isEmpty(sheetIndexs)
                && ArrayUtils.isEmpty(sheetNames))
            {
                return false;
            }
            return !ArrayUtils.contains(sheetIndexs, sstIdx)
                && !ArrayUtils.contains(sheetNames, sstName);
        }

        private String getString(CellRecord record) {
            switch (record.getSid()) {
                case BlankRecord.sid:
                    return null;
                case BoolErrRecord.sid:
                    return Boolean.toString(((BoolErrRecord) record).getBooleanValue());
                case FormulaRecord.sid:
                    FormulaRecord frec = (FormulaRecord) record;
                    if (Double.isNaN(frec.getValue())) {
                        return null; // Formula result is a string, This is stored in the next record
                    } else {
                        return formatListener.formatNumberDateCell(frec);
                    }
                    // return '"' + HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression()) + '"';
                case LabelSSTRecord.sid:
                    return sstrec == null ? null : sstrec.getString(((LabelSSTRecord) record).getSSTIndex()).getString();
                case NumberRecord.sid:
                    NumberRecord number = (NumberRecord) record;
                    if (StringUtils.containsAny(formatListener.getFormatString(number), '/', ':')) {
                        return formatListener.formatNumberDateCell(number);
                    } else {
                        return String.valueOf(number.getValue());
                    }
                default:
                    return null;
            }
        }
    }

    // ------------------------------------------------------unsupported operation
    @Override @Deprecated
    public boolean isSheetHidden(int sheetIx) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean isSheetVeryHidden(int sheetIx) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int getActiveSheetIndex() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setActiveSheet(int sheetIndex) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int getFirstVisibleTab() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setFirstVisibleTab(int sheetIndex) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setSheetOrder(String sheetname, int pos) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setSelectedTab(int index) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setSheetName(int sheet, String name) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Sheet createSheet() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Sheet createSheet(String sheetname) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Sheet cloneSheet(int sheetNum) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void removeSheetAt(int index) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Font createFont() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Font findFont(boolean bold, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int getNumberOfFonts() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int getNumberOfFontsAsInt() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Font getFontAt(int idx) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public CellStyle createCellStyle() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int getNumCellStyles() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public CellStyle getCellStyleAt(int idx) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void write(OutputStream stream) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int getNumberOfNames() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Name getName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public List<? extends Name> getNames(String name) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public List<? extends Name> getAllNames() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Name createName() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void removeName(Name name) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int linkExternalWorkbook(String name, Workbook workbook) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setPrintArea(int sheetIndex, String reference) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setPrintArea(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public String getPrintArea(int sheetIndex) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void removePrintArea(int sheetIndex) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public MissingCellPolicy getMissingCellPolicy() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public DataFormat createDataFormat() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int addPicture(byte[] pictureData, int format) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public List<? extends PictureData> getAllPictures() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public CreationHelper getCreationHelper() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean isHidden() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setHidden(boolean hiddenFlag) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setSheetHidden(int sheetIx, boolean hidden) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public SheetVisibility getSheetVisibility(int sheetIx) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setSheetVisibility(int sheetIx, SheetVisibility visibility) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void addToolPack(UDFFinder toopack) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setForceFormulaRecalculation(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean getForceFormulaRecalculation() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public SpreadsheetVersion getSpreadsheetVersion() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int addOlePackage(byte[] oleData, String label, String fileName, String command) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public EvaluationWorkbook createEvaluationWorkbook() {
        throw new UnsupportedOperationException();
    }

}
