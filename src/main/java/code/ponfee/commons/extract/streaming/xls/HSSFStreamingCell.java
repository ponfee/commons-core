package code.ponfee.commons.extract.streaming.xls;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

/**
 * The version for 2003 or early XSL excel file streaming reader excel cell
 *
 * @author Ponfee
 */
public class HSSFStreamingCell implements Cell {

    private final String value;

    public HSSFStreamingCell(String value) {
        this.value = value;
    }

    @Override
    public String getStringCellValue() {
        return this.value;
    }

    @Override
    public CellType getCellType() {
        return CellType.STRING;
    }

    // ----------------------------------------------unsupported operation
    @Override @Deprecated
    public int getColumnIndex() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public int getRowIndex() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Sheet getSheet() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Row getRow() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellType(CellType cellType) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public CellType getCellTypeEnum() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public CellType getCachedFormulaResultType() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public CellType getCachedFormulaResultTypeEnum() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellValue(double value) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellValue(Date value) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellValue(Calendar value) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellValue(RichTextString value) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellValue(String value) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellFormula(String formula) throws FormulaParseException {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public String getCellFormula() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public double getNumericCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Date getDateCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public RichTextString getRichStringCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellValue(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellErrorValue(byte value) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean getBooleanCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public byte getErrorCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellStyle(CellStyle style) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public CellStyle getCellStyle() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setAsActiveCell() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public CellAddress getAddress() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellComment(Comment comment) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Comment getCellComment() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void removeCellComment() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public Hyperlink getHyperlink() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setHyperlink(Hyperlink link) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void removeHyperlink() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public CellRangeAddress getArrayFormulaRange() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean isPartOfArrayFormulaGroup() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void removeFormula() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setBlank() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void setCellValue(LocalDateTime value) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public LocalDateTime getLocalDateTimeCellValue() {
        throw new UnsupportedOperationException();
    }

}
