package code.ponfee.commons.export;

import code.ponfee.commons.export.Tmeta.Type;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.tree.FlatNode;
import code.ponfee.commons.util.Colors;
import code.ponfee.commons.util.Dates;
import code.ponfee.commons.util.ImageUtils;
import code.ponfee.commons.util.Strings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * excel导出
 *
 * SXSSFWorkbook（size>65536），XSSFWorkbook（.xlsx），HSSFWorkbook（.xls）
 * 
 * @see <a href="https://blog.csdn.net/zl_momomo/article/details/80703533">poi</a>
 * 
 * |------------|----------------------------------------------------------|--------------|
 * |    model   |                         description                      | Read & Write |
 * |------------|----------------------------------------------------------|--------------|
 * | SXSSF      | holding fix rows in memory, write disk when row overflow |      R       |
 * |------------|----------------------------------------------------------|--------------|
 * | eventmodel | based event model, parse with SAX, thrift cup and memory |      R       |
 * |------------|----------------------------------------------------------|--------------|
 * | usermodel  | tradition model, need more cpu and memory                |     R & W    |
 * |------------|----------------------------------------------------------|--------------|
 * 
 * @author Ponfee
 */
public class ExcelExporter extends AbstractDataExporter<byte[]> {

    // 内存最大存放100行数据 超过100自动刷新到硬盘中
    // SXSSFSheet.flushRows(100); retain 100 last rows and flush all others
    public static final int DEFAULT_WINDOW_SIZE = 100;

    /** row and cell config，默认的列宽和行高大小 */
    private static final int DEFAULT_WIDTH = 3200;
    private static final short DEFAULT_HEIGHT = 350;

    /** nested image config，office 2013版本 */
    private static final double RATE_WIDTH = 70.5; // 图片真实宽度与excel列宽的比例
    private static final double RATE_HEIGHT = 18; // 图片真实高度与excel行高的比例

    /** 作为分隔符（类似html的<hr />）的合并列数目 */
    private static final int MARGIN_ROW_CELL_SIZE = 26;

    private final IndexedColorMap defaultColorMap = new DefaultIndexedColorMap();

    private SXSSFWorkbook workbook; // excel
    private final XSSFCellStyle titleStyle; // 标题样式
    private final XSSFCellStyle headStyle; // 表头样式
    private final XSSFCellStyle dataStyle; // 数据样式
    private final XSSFCellStyle tfootMergeStyle; // 合计行样式
    private final XSSFCellStyle noneStyle; // 无样式
    private final XSSFCellStyle tipStyle; // 无样式
    private final XSSFDataFormat dataFormat; // 数据格式

    private final Map<String, SXSSFSheet> sheets = new HashMap<>();
    private final Map<String, Integer>    images = new HashMap<>();
    private final Map<String, Freeze>    freezes = new HashMap<>();

    public ExcelExporter() {
        workbook = new SXSSFWorkbook(DEFAULT_WINDOW_SIZE);
        //workbook.setCompressTempFiles(true); // temp files will be gzipped
        dataFormat = (XSSFDataFormat) workbook.createDataFormat();

        XSSFFont titleFont = (XSSFFont) workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontName("黑体");
        //titleFont.setFontHeightInPoints((short) 12); // set font 1 to 12 point type

        XSSFFont headFont = (XSSFFont) workbook.createFont();
        headFont.setBold(true);
        headFont.setFontName("宋体");

        XSSFFont redFont = (XSSFFont) workbook.createFont();
        redFont.setColor(new XSSFColor(new Color(255, 0, 0), defaultColorMap));
        redFont.setFontName("宋体");

        XSSFCellStyle baseStyle = (XSSFCellStyle) workbook.createCellStyle();
        baseStyle.setBorderLeft(BorderStyle.THIN);
        baseStyle.setBorderTop(BorderStyle.THIN);
        baseStyle.setBorderRight(BorderStyle.THIN);
        baseStyle.setBorderBottom(BorderStyle.THIN);
        baseStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        baseStyle.setWrapText(true);
        baseStyle.setDataFormat(dataFormat.getFormat("@"));

        titleStyle = (XSSFCellStyle) workbook.createCellStyle();
        titleStyle.cloneStyleFrom(baseStyle);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setFont(titleFont);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); // 填充样式
        titleStyle.setFillForegroundColor(new XSSFColor(new Color(255, 255, 224), defaultColorMap)); // 填充颜色

        headStyle = (XSSFCellStyle) workbook.createCellStyle();
        headStyle.cloneStyleFrom(baseStyle);
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setFillForegroundColor(new XSSFColor(new Color(192, 192, 192), defaultColorMap));
        headStyle.setFont(headFont);
        headStyle.setWrapText(false);

        dataStyle = (XSSFCellStyle) workbook.createCellStyle();
        dataStyle.cloneStyleFrom(baseStyle);
        dataStyle.setWrapText(false);

        tfootMergeStyle = (XSSFCellStyle) workbook.createCellStyle();
        tfootMergeStyle.cloneStyleFrom(dataStyle);
        tfootMergeStyle.setFont(headFont);
        tfootMergeStyle.setWrapText(false);
        tfootMergeStyle.setAlignment(HorizontalAlignment.RIGHT);
        tfootMergeStyle.setBorderLeft(BorderStyle.THIN);
        tfootMergeStyle.setBorderTop(BorderStyle.THIN);
        tfootMergeStyle.setBorderRight(BorderStyle.THIN);
        tfootMergeStyle.setBorderBottom(BorderStyle.THIN);

        tipStyle = (XSSFCellStyle) workbook.createCellStyle();
        tipStyle.cloneStyleFrom(baseStyle);
        tipStyle.setFont(redFont);

        noneStyle = (XSSFCellStyle) workbook.createCellStyle();
        noneStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    /**
     * 构建excel
     */
    @Override
    public <E> void build(Table<E> table) {
        // 1、校验表头是否为空
        List<FlatNode<Integer, Thead>> flats = table.getThead();
        if (CollectionUtils.isEmpty(flats)) {
            throw new IllegalArgumentException("thead can't be null");
        }

        // 2、获取工作簿
        String name = this.getName();
        SXSSFSheet sheet = getSheet(name);

        // 3、判断工作簿是否已创建过行数据，还没有创建行时sheet.getLastRowNum()返回-1
        CursorRowNumber cursorRow = new CursorRowNumber(Math.max(sheet.getLastRowNum(), 0));
        if (cursorRow.get() > 0) {
            // 创建两行空白行
            cursorRow.increment();
            int i = cursorRow.getAndIncrement(), j = cursorRow.getAndIncrement();
            SXSSFRow row1 = sheet.createRow(i);
            row1.setHeight(DEFAULT_HEIGHT);
            SXSSFRow row2 = sheet.createRow(j);
            row2.setHeight(DEFAULT_HEIGHT);
            for (int k = 0; k < MARGIN_ROW_CELL_SIZE; k++) {
                createCell(row1, k, noneStyle, null);
                createCell(row2, k, noneStyle, null);
            }
            sheet.addMergedRegion(new CellRangeAddress(i, j, 0, MARGIN_ROW_CELL_SIZE - 1));
        }

        // 4、构建复合表头
        buildComplexThead(table, sheet, cursorRow);

        // 5、冻结窗口配置
        Freeze freeze;
        if ((freeze = freezes.get(name)) != null) {
            freeze.disable();
        } else {
            freezes.put(name, new Freeze(1, cursorRow.get())); // 叶子节点只占一列，故colSplit=1
        }

        // 6、处理tbody数据
        Map<CellStyleOptions, Object> options = table.getOptions();
        List<Thead> leafs = getLeafThead(flats);
        List<XSSFCellStyle> styles = createStyles(leafs);
        rollingTbody(table, (data, i) -> {
            SXSSFRow row = sheet.createRow(cursorRow.getAndIncrement());
            //row.setHeight(DEFAULT_HEIGHT);
            for (int m = data.length, j = 0; j < m; j++) {
                createCell(row, j, styles.get(j), getTmeta(leafs, j), data[j], i, j, options);
            }
        });

        // 7、判断是否有数据
        SXSSFRow row;
        int totalLeafCount = flats.get(0).getTreeLeafCount();
        if (table.isEmptyTbody()) {
            createBlankRow(NO_RESULT_TIP, sheet, tipStyle, cursorRow, totalLeafCount);
        } else {
            super.nonEmpty();
        }

        // 8、处理tfoot数据
        Object[] tfoots = table.getTfoot();
        if (ArrayUtils.isNotEmpty(tfoots)) {
            int rowNum = cursorRow.getAndIncrement();
            row = sheet.createRow(rowNum);
            row.setHeight(DEFAULT_HEIGHT);

            if (table.getTfoot().length > totalLeafCount) {
                throw new IllegalStateException("tfoot data length cannot more than total leaf count.");
            }

            // 合计单元格
            int mergeNum = totalLeafCount - table.getTfoot().length;
            for (int i = 0; i < mergeNum; i++) {
                createCell(row, i, tfootMergeStyle, (i == 0) ? "合计" : null);
            }
            if (mergeNum > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, mergeNum - 1));
            }

            // 合计数据
            for (int i = 0; i < tfoots.length; i++) {
                createCell(row, i + mergeNum, styles.get(mergeNum + i),
                           getTmeta(leafs, mergeNum + i), tfoots[i]);
            }
        }

        // 9、文字注释
        if (StringUtils.isNotBlank(table.getComment())) {
            createBlankRow(table.getComment(), sheet, tipStyle, cursorRow, totalLeafCount);
        }
    }

    public void insertImage(byte[] imageBytes) {
        int[] size = ImageUtils.getImageSize(new ByteArrayInputStream(imageBytes));
        insertImage(imageBytes, size[0], size[1]);
    }

    /**
     * excel中嵌入图片
     * excel.insertImage(byte[] image, width, cheight);
     */
    public void insertImage(byte[] imageBytes, int width, int height) {
        if (imageBytes == null || imageBytes.length == 0) {
            return;
        }

        super.nonEmpty();

        SXSSFSheet sheet = getSheet(getName());
        int startRow = Optional.ofNullable(images.get(getName())).orElse(1), startCol = 1;
        int endCol = startCol + (int) Math.round(((double) width) / RATE_WIDTH);
        int endRow = startRow + (int) Math.round(((double) height) / RATE_HEIGHT);
        images.put(getName(), endRow + 2);

        SXSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(
            0, 0, width, height, startCol, startRow, (short) endCol, endRow
        );

        anchor.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);
        drawing.createPicture(anchor, workbook.addPicture(imageBytes, SXSSFWorkbook.PICTURE_TYPE_PNG));

        /*int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
        CreationHelper helper = workbook.getCreationHelper();
        SXSSFDrawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(0);
        anchor.setRow1(Math.max(sheet.getLastRowNum(), 0));
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        pict.resize(1);*/
    }

    /**
     * 输出到输出流
     */
    public void write(OutputStream out) {
        try (BufferedOutputStream bos = new BufferedOutputStream(out);
             SXSSFWorkbook wb = workbook
        ) {
            createFreezePane();
            wb.write(bos);
            wb.dispose();
            workbook = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String filepath) {
        write(new File(filepath));
    }

    public void write(File file) {
        try (OutputStream out = new FileOutputStream(file)) {
            write(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 导出
     */
    @Override
    public byte[] export() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        write(out);
        return out.toByteArray();
    }

    /**
     * 关闭
     */
    @Override
    public void close() {
        if (workbook == null) {
            return;
        }

        try (SXSSFWorkbook wb = workbook) {
            // nothing to do
            wb.dispose(); // dispose of temporary files backing this workbook on disk
            workbook = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            sheets.clear();
            images.clear();
            freezes.clear();
        }
    }

    //------------------------------------------------------------protected methods
    protected SXSSFSheet getSheet(String name) {
        SXSSFSheet sheet = sheets.get(name);
        if (sheet == null) {
            sheet = workbook.createSheet(name);
            sheet.setDisplayGridlines(false); // 不显示网格线
            /*sheet.setDefaultColumnWidth(DEFAULT_WIDTH);
            sheet.setDefaultRowHeight(DEFAULT_HEIGHT);*/
            sheets.put(name, sheet);
        }
        return sheet;
    }

    protected void removeSheet(String name) {
        workbook.removeSheetAt(workbook.getSheetIndex(sheets.get(name)));
        sheets.remove(name);
    }

    private void createFreezePane() {
        for (Entry<String, SXSSFSheet> entry : sheets.entrySet()) {
            Freeze freeze = freezes.get(entry.getKey());
            if (freeze != null && freeze.freeze) {
                entry.getValue().createFreezePane(freeze.colSplit, freeze.rowSplit);
            }
        }
    }

    // 创建简单表头
    /*private void buildSimpleThead(String title, XSSFSheet sheet, CursorRow cursorRow, String[] theadName) {
        createCell(title, sheet, titleStyle, cursorRow, theadName.length);
        XSSFRow row = sheet.createRow(cursorRow.getAndIncrement());
        row.setHeight(DEFAULT_HEIGHT);
        for (int i = 0; i < theadName.length; i++) {
            sheet.setColumnWidth(i, DEFAULT_WIDTH);
            createCell(row, i, headStyle, theadName[i]);
        }
    }*/

    // 复合表头
    private <E> void buildComplexThead(Table<E> table, SXSSFSheet sheet, CursorRowNumber cursorRow) {
        List<FlatNode<Integer, Thead>> flats = table.getThead();
        FlatNode<Integer, Thead> root = flats.get(0);
        int totalLeafCount = root.getTreeLeafCount();
        List<FlatNode<Integer, Thead>> thead = flats.subList(1, flats.size());

        // create caption
        if (StringUtils.isNotBlank(table.getCaption())) {
            createBlankRow(table.getCaption(), sheet, titleStyle, cursorRow, totalLeafCount);
        }

        //sheet.trackAllColumnsForAutoSizing(); // 设置自动列宽

        // 约定非叶子节点不能跨行
        Set<Integer> rows = new HashSet<>();
        int beginCol, endRow, endCol, lastLevel = 1;
        int cellLevel, treeDepth = root.getTreeDepth() - 1;
        for (int n = thead.size(), i = 0; i < n; i++) {
            FlatNode<Integer, Thead> flat = thead.get(i);
            cellLevel = flat.getLevel() - 1;
            if (cellLevel > lastLevel) {
                lastLevel = cellLevel;
                cursorRow.increment();
            }

            beginCol = flat.getLeftLeafCount();
            endCol = beginCol + flat.getTreeLeafCount() - 1;
            if (flat.isLeaf()) {
                endRow = cursorRow.get() + treeDepth - cellLevel;
                sheet.setColumnWidth(beginCol, DEFAULT_WIDTH);
                //sheet.autoSizeColumn(beginCol); // 设置自动列宽，要在autoSizeColumn前使用trackAllColumnsForAutoSizing()
            } else {
                endRow = cursorRow.get(); // 约定非子节点不能跨行
            }

            SXSSFRow colRow;
            if (rows.add(cursorRow.get())) {
                colRow = sheet.createRow(cursorRow.get()); // 还未创建该行
                colRow.setHeight(DEFAULT_HEIGHT);
            } else {
                colRow = sheet.getRow(cursorRow.get());
            }
            createCell(colRow, beginCol, headStyle, flat.getAttach().getName());

            // -----------------------------设置被合并单元格的样式------------------------------ //
            for (int b = beginCol + 1; b <= endCol; b++) { // 列
                createCell(colRow, b, headStyle, null);
            }
            for (int a = cursorRow.get() + 1; a <= endRow; a++) { // 行
                if (rows.add(a)) { // 行未创建
                    colRow = sheet.createRow(a);
                    colRow.setHeight(DEFAULT_HEIGHT);
                } else { // 行已创建
                    colRow = sheet.getRow(a);
                }

                for (int b = beginCol; b <= endCol; b++) {
                    createCell(colRow, b, headStyle, null);
                }
            }
            if (cursorRow.get() != endRow || beginCol != endCol) {
                sheet.addMergedRegion(new CellRangeAddress(cursorRow.get(), endRow, beginCol, endCol));
            }
            // -----------------------------设置被合并单元格的样式------------------------------ //
        }
        cursorRow.increment();
    }

    private Tmeta getTmeta(List<Thead> thead, int index) {
        return thead.get(index).getTmeta();
    }

    /**
     * 创建空行
     * @param text
     * @param sheet
     * @param style
     * @param cursorRow
     * @param columnLen
     */
    private void createBlankRow(String text, SXSSFSheet sheet, XSSFCellStyle style, 
                                CursorRowNumber cursorRow, int columnLen) {
        SXSSFRow row = sheet.createRow(cursorRow.get());
        row.setHeight(DEFAULT_HEIGHT);
        createCell(row, 0, style, text);
        for (int i = 1; i < columnLen; i++) {
            createCell(row, i, style, null);
        }
        sheet.addMergedRegion(new CellRangeAddress(cursorRow.get(), cursorRow.get(), 0, columnLen - 1));
        cursorRow.increment();
    }

    private void createCell(SXSSFRow row, int colIndex, XSSFCellStyle style, Tmeta tmeta, Object value) {
        createCell(row, colIndex, style, tmeta, value, -1, -1, null);
    }

    private void createCell(SXSSFRow row, int colIndex, XSSFCellStyle style, Object value) {
        createCell(row, colIndex, style, null, value, -1, -1, null);
    }

    /**
     * 创建单元格
     * @param row
     * @param colIndex
     * @param style
     * @param tmeta
     * @param value
     * @param tbodyRowIdx
     * @param tbodyColIdx
     * @param options
     */
    private void createCell(SXSSFRow row, int colIndex, XSSFCellStyle style, Tmeta tmeta, Object value, 
                            int tbodyRowIdx, int tbodyColIdx, Map<CellStyleOptions, Object> options) {

        SXSSFCell cell = row.createCell(colIndex);
        cell.setCellStyle(style);

        // 设置单元格格式
        if (tmeta == null) {
            setCellString(cell, value);
        } else if (tmeta.getType() == Type.NUMERIC) {
            if (Strings.isBlank(value)) {
                cell.setCellType(CellType.NUMERIC);
                cell.setCellValue(new XSSFRichTextString());
            } else if (value instanceof String && ((String) value).endsWith("%")) {
                String val = ((String) value).substring(0, ((String) value).length() - 1);
                cell.setCellValue(Numbers.toDouble(val.replace(",", "")) / 100);
            } else {
                cell.setCellValue(Numbers.toDouble(value.toString().replace(",", "")));
            }
        } else if (tmeta.getType() == Type.DATETIME) {
            if (value == null) {
                cell.setCellType(CellType.BLANK);
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
            } else if (value instanceof Calendar) {
                cell.setCellValue((Calendar) value);
            } else {
                String str = value.toString();
                String format = Optional.ofNullable(tmeta.getFormat()).orElse(Dates.DEFAULT_DATE_FORMAT);
                try {
                    cell.setCellValue(DateUtils.parseDateStrictly(str, format));
                    //cell.setCellValue(FastDateFormat.getInstance(format).parse(str));
                } catch (ParseException e) {
                    throw new IllegalArgumentException("invalid date str: " + str + ", format: " + format, e);
                }
            }
        } else {
            setCellString(cell, value);
        }

        // 样式自定义处理
        processOptions(cell, tbodyRowIdx, tbodyColIdx, options);
    }

    /**
     * 处理其它配置项
     * @param cell
     * @param tbodyRowIdx
     * @param tbodyColIdx
     * @param options
     */
    @SuppressWarnings("unchecked")
    private void processOptions(SXSSFCell cell, int tbodyRowIdx, int tbodyColIdx, 
                                Map<CellStyleOptions, Object> options) {
        if (MapUtils.isEmpty(options)) {
            return;
        }

        // 单元格高亮显示
        Map<String, Object> highlight = (Map<String, Object>) options.get(CellStyleOptions.HIGHLIGHT);
        if (MapUtils.isNotEmpty(highlight)) {
            for (List<Integer> c : (List<List<Integer>>) highlight.get("cells")) {
                if (c.get(0) == tbodyRowIdx && c.get(1) == tbodyColIdx) {
                    XSSFFont font = (XSSFFont) workbook.createFont();
                    font.setColor(new XSSFColor(
                        Colors.fromHex((String) highlight.get("color")), defaultColorMap
                    ));
                    XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
                    style.cloneStyleFrom(cell.getCellStyle());
                    style.setFont(font);
                    cell.setCellStyle(style);
                }
            }
        }

        // 处理
        Consumer<Object[]> processor = (Consumer<Object[]>) options.get(CellStyleOptions.CELL_PROCESS);
        if (processor != null) {
            processor.accept(new Object[] { workbook, cell, tbodyRowIdx, tbodyColIdx });
        }
    }

    /**
     * create cell style, only called once
     * @param thead
     * @return
     */
    private List<XSSFCellStyle> createStyles(List<Thead> thead) {
        List<XSSFCellStyle> styles = new ArrayList<>(thead.size());
        for (Thead flat : thead) {
            XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
            styles.add(style);
            style.cloneStyleFrom(dataStyle);

            Tmeta tmeta = flat.getTmeta();
            if (tmeta != null) {
                switch (tmeta.getAlign()) { // 对齐方式
                    case LEFT:
                        style.setAlignment(HorizontalAlignment.LEFT);
                        break;
                    case CENTER:
                        style.setAlignment(HorizontalAlignment.CENTER);
                        break;
                    case RIGHT:
                        style.setAlignment(HorizontalAlignment.RIGHT);
                        break;
                    default:
                        break;
                }

                // 设置单元格格式
                if (StringUtils.isNotBlank(tmeta.getFormat())) {
                    //dataFormat.getFormat("0.00%")： 0.00%->0xa; #,###.00%->0xa5; #,##0->xxx;
                    style.setDataFormat(dataFormat.getFormat(tmeta.getFormat()));
                }

                // 设置颜色
                if (tmeta.getColor() != null) {
                    XSSFFont font = (XSSFFont) workbook.createFont();
                    font.setColor(new XSSFColor(tmeta.getColor(), defaultColorMap));
                    style.setFont(font);
                }
            } // end of tmeta

        }

        return styles;
    }

    private static void setCellString(SXSSFCell cell, Object value) {
        if (value != null) {
            cell.setCellValue(value.toString());
        } else {
            cell.setCellType(CellType.BLANK);
        }
    }

    /**
     * 游标行
     */
    @SuppressWarnings("unused")
    private static final class CursorRowNumber {
        int current;

        CursorRowNumber() {
            this(0);
        }

        CursorRowNumber(int initValue) {
            this.current = initValue;
        }

        int getAndIncrement() {
            return this.current++;
        }

        int incrementAndGet() {
            return ++this.current;
        }

        int getAndDecrement() {
            return this.current--;
        }

        int decrementAndGet() {
            return --this.current;
        }

        void add(int i) {
            this.current += i;
        }

        int addAndGet(int i) {
            this.current += i;
            return this.current;
        }

        int getAndAdd(int i) {
            int temp = this.current;
            this.current += i;
            return temp;
        }

        void set(int i) {
            this.current = i;
        }

        int getAndSet(int i) {
            int temp = this.current;
            this.current = i;
            return temp;
        }

        int get() {
            return this.current;
        }

        void increment() {
            this.current++;
        }
    }

    /**
     * 窗口冻结
     */
    @SuppressWarnings("unused")
    private static final class Freeze {
        boolean freeze = true;
        final int colSplit;
        final int rowSplit;

        Freeze(int colSplit, int rowSplit) {
            this.colSplit = colSplit;
            this.rowSplit = rowSplit;
        }

        void enable() {
            freeze = true;
        }

        void disable() {
            freeze = false;
        }
    }

    /*public void deleteTempFiles() {
        for (int i = 0, n = workbook.getNumberOfSheets(); i <= n; i++) {
            SXSSFSheet sheet = workbook.getSheetAt(i); // only support in SXSSFSheet
            // delete only if the sheet is written by stream
            SheetDataWriter sdw = (SheetDataWriter) Fields.get(sheet, "_writer");
            FileUtils.deleteQuietly((File) Fields.get(sdw, "_fd"));
        }
    }*/

}
