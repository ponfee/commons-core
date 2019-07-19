package code.ponfee.commons.extract.streaming.xls;

import java.util.Calendar;
import java.util.Date;

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

/**
 * The version for 2003 or early XSL excel file 
 * streaming reader
 * 
 * excel cell
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
    @Override
    public int getColumnIndex() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRowIndex() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sheet getSheet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Row getRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellType(CellType cellType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellType getCellTypeEnum() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellType getCachedFormulaResultType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellType getCachedFormulaResultTypeEnum() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellValue(double value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellValue(Date value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellValue(Calendar value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellValue(RichTextString value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellValue(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellFormula(String formula) throws FormulaParseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCellFormula() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getNumericCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDateCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RichTextString getRichStringCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellValue(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellErrorValue(byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBooleanCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getErrorCellValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellStyle(CellStyle style) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellStyle getCellStyle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAsActiveCell() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellAddress getAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellComment(Comment comment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comment getCellComment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeCellComment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Hyperlink getHyperlink() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHyperlink(Hyperlink link) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeHyperlink() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellRangeAddress getArrayFormulaRange() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPartOfArrayFormulaGroup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeFormula() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBlank() {
        throw new UnsupportedOperationException();
    }

}
