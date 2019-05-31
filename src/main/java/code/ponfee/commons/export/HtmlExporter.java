package code.ponfee.commons.export;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.export.Tmeta.Type;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.tree.FlatNode;

/**
 * html导出
 * @author fupf
 */
public class HtmlExporter extends AbstractDataExporter<String> {

    //private static final Pattern PATTERN_NEGATIVE = Pattern.compile("^(-(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*)))(%)?$");

    public static final String HORIZON = "<hr style=\"border:3 double #b0c4de;with:95%;margin:20px 0;\" />";
    private static final String TEMPLATE = new StringBuilder(4096) 
       .append("<!DOCTYPE html>                                                                   \n")
       .append("<html>                                                                            \n")
       .append("  <head lang=\"en\">                                                              \n")
       .append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />     \n")
       .append("    <title>{0}</title>                                                            \n")
       .append("    '<style>                                                                      \n")
       .append("      * {font-family: Microsoft YaHei;}                                           \n")
       .append("      .grid {overflow-x: auto;background-color: #fff;color: #555;}                \n")
       .append("      .grid table {                                                               \n")
       .append("        width:100%;font-size:12px;border-collapse:collapse;border-style:hidden;   \n")
       .append("      }                                                                           \n")
       .append("      .grid table, div.grid table caption, div.grid table tr {                    \n")
       .append("        border: 1px solid #6d6d6d;                                                \n")
       .append("      }                                                                           \n")
       .append("      .grid table tr td, div.grid table tr th {border: 1px solid #6d6d6d;}        \n")
       .append("      .grid table caption {                                                       \n")
       .append("        font-size:14px; padding:5px;                                              \n")
       .append("        background:#e6e6fa; font-weight:bolder; border-bottom:none;               \n")
       .append("      }                                                                           \n")
       .append("      .grid table thead th {padding: 5px;background: #ccc;}                       \n")
       .append("      .grid table td {text-align: center;padding: 3px;}                           \n")
       .append("      .grid table td.text-left, .grid table th.text-left {text-align:left;}       \n")
       .append("      .grid table td.text-right, .grid table th.text-right {text-align:right;}    \n")
       .append("      .grid table td.text-center, .grid table th.text-center {text-align:center;} \n")
       .append("      .grid table tfoot th {padding: 5px;}                                        \n")
       .append("      .grid table tr:nth-child(odd) td{background:#fff;}                          \n")
       .append("      .grid table tr:nth-child(even) td{background: #e8e8e8}                      \n")
       .append("      .grid p.remark {font-size: 14px;}                                           \n")
       .append("      .grid .nowrap {                                                             \n")
       .append("        white-space:nowrap; word-break:keep-all;                                  \n")
       .append("        overflow:hidden; text-overflow:ellipsis; max-width:200px;                 \n")
       .append("      }                                                                           \n")
       .append("    </style>'                                                                     \n")
       .append("  </head>                                                                         \n")
       .append("  <body>{1}</body>                                                                \n")
       .append("</html>                                                                           \n")
       .toString().replaceAll(" +\n", "\n");

    private StringBuilder html; // StringBuilder扩容：(value.length << 1) + 2
                                // 容量如果不够，直接扩充到需要的容量大小

    public HtmlExporter() {
        this.html = new StringBuilder(0x2000); // 初始容量8192
    }

    public HtmlExporter(String initHtml) {
        this.html = new StringBuilder(initHtml);
    }

    /**
     * 构建html
     */
    @Override
    public <E> void build(Table<E> table) {
        List<FlatNode<Integer, Thead>> flats = table.getThead();
        if (flats == null || flats.isEmpty()) {
            throw new IllegalArgumentException("thead can't be null");
        }

        // horizon-------
        horizon();

        // table start-------
        html.append("<div class=\"grid\"><table cellpadding=\"0\" cellspacing=\"0\">");
        if (StringUtils.isNotBlank(table.getCaption())) {
            html.append("<caption>")
                .append(table.getCaption())
                .append("</caption>");
        }

        // thead-------
        buildComplexThead(flats);

        // tbody-------
        List<Thead> leafs = getLeafThead(flats);
        html.append("<tbody>");
        rollingTbody(table, (data, i) -> {
            html.append("<tr>");
            for (int m = data.length, j = 0; j < m; j++) {
                html.append("<td");
                processMeta(data[j], getTmeta(leafs, j), i, j, table.getOptions()); // 样式
                html.append(">").append(formatData(data[j], getTmeta(leafs, j))).append("</td>");
            }
            html.append("</tr>");
        });

        int totalLeafCount = flats.get(0).getChildLeafCount();
        if (table.isEmptyTbody()) {
            html.append("<tr><td colspan=\"")
                .append(totalLeafCount)
                .append("\" style=\"color:red;padding:3px;font-size:14px;\">")
                .append(NO_RESULT_TIP)
                .append("</td></tr>");
        } else {
            super.nonEmpty();
        }
        html.append("</tbody>");

        // tfoot-------
        boolean hasTfoot = false;
        if (ArrayUtils.isNotEmpty(table.getTfoot())) {
            hasTfoot = true;
            html.append("<tfoot><tr>");

            if (table.getTfoot().length > totalLeafCount) {
                throw new IllegalStateException("tfoot data length cannot more than total leaf count.");
            }

            int mergeNum = totalLeafCount - table.getTfoot().length;
            if (mergeNum > 0) {
                html.append("<th colspan=\"")
                    .append(mergeNum)
                    .append("\" style=\"text-align:right;\">合计</th>");
            }

            for (int i = 0; i < table.getTfoot().length; i++) {
                html.append("<th");
                processMeta(table.getTfoot()[i], getTmeta(leafs, mergeNum + i));
                html.append(">")
                    .append(formatData(table.getTfoot()[i], getTmeta(leafs, mergeNum + i)))
                    .append("</th>");
            }
            html.append("</tr></tfoot>");
        }

        // comment------
        if (StringUtils.isNotBlank(table.getComment())) {
            String[] comments = table.getComment().split(";");
            StringBuilder builder = new StringBuilder("<tr><td colspan=\"")
                .append(totalLeafCount)
                .append("\" style=\"color:red; padding:3px;font-size:14px;\">")
                .append("<div style=\"text-align:left;font-weight:bold;\">备注：</div>");
            for (String comment : comments) {
                builder.append("<div style=\"text-align:left;text-indent:2em;\">")
                       .append(comment)
                       .append("</div>");
            }
            builder.append("</td></tr>");

            if (hasTfoot) {
                html.insert(html.length() - "</tfoot>".length(), builder);
            } else {
                html.append("<tfoot>").append(builder).append("</tfoot>");
            }
        }

        // table end-----
        html.append("</table></div>");
    }

    @Override
    public String export() {
        return MessageFormat.format(TEMPLATE, super.getName(), html.toString());
        //return html.insert(0, "before").append("after").toString();
    }

    public String body() {
        return html.toString();
    }

    @Override
    public void close() {
        html = null;
    }

    public HtmlExporter horizon() {
        if (html.length() > 0) {
            html.append(HORIZON);
        }
        return this;
    }

    //htmlExporter.horizon().append("<div align=\"center\"><img src=\"cid:")
    //                      .append(img.getId()).apend("\" /></div>");
    public HtmlExporter append(String string) {
        if (StringUtils.isNotBlank(string)) {
            super.nonEmpty();
            html.append(string);
        }
        return this;
    }

    // <img src="data:image/jpg;base64,/9j/4QMZR..." />
    public HtmlExporter insertImage(String imageB64) {
        super.nonEmpty();
        html.append("<img src=\"").append(imageB64).append("\" />");
        return this;
    }

    // 创建简单表头
    /*private void buildSimpleThead(String[] theadName) {
        html.append("<thead><tr>");
        for (String th : theadName) {
            html.append("<th>").append(th).append("</th>");
        }
        html.append("</tr></thead>");
    }*/

    // 复合表头
    private void buildComplexThead(List<FlatNode<Integer, Thead>> flats) {
        html.append("<thead><tr>");
        int lastLevel = 1, treeMaxDepth = flats.get(0).getTreeMaxDepth() - 1, cellLevel;
        for (FlatNode<Integer, Thead> flat : flats.subList(1, flats.size())) {
            cellLevel = flat.getLevel() - 1;
            if (lastLevel < cellLevel) {
                html.append("</tr><tr>");
                lastLevel = cellLevel;
            }
            html.append("<th");
            if (flat.isLeaf()) { // 叶子节点，跨行

                if (treeMaxDepth - cellLevel > 0) {
                    html.append(" rowspan=\"").append(treeMaxDepth - cellLevel + 1).append("\"");
                }
            } else { // 非叶子节点，跨列
                if (flat.getChildLeafCount() > 1) {
                    html.append(" colspan=\"").append(flat.getChildLeafCount()).append("\"");
                }
            }
            html.append(">").append(flat.getAttach().getName()).append("</th>");
        }
        html.append("</tr></thead>");
    }

    private Tmeta getTmeta(List<Thead> thead, int index) {
        return thead.get(index).getTmeta();
    }

    private void processMeta(Object value, Tmeta tmeta) {
        processMeta(value, tmeta, -1, -1, null);
    }

    /**
     * 样式处理
     * @param value
     * @param tmeta
     * @param tbodyRowIdx
     * @param tbodyColIdx
     * @param options
     */
    private final StringBuilder style = new StringBuilder();
    private final StringBuilder clazz = new StringBuilder();
    private void processMeta(Object value, Tmeta tmeta, int tbodyRowIdx, 
                             int tbodyColIdx, Map<CellStyleOptions, Object> options) {
        style.setLength(0);
        clazz.setLength(0);

        /*if (PATTERN_NEGATIVE.matcher(Objects.toString(value, "")).matches()) {
            style.append("color:#006400;font-weight:bold;"); // 负数显示绿色
        }*/

        if (tmeta != null) {
            switch (tmeta.getAlign()) {
                case LEFT:
                    clazz.append("text-left ");
                    break;
                case CENTER:
                    clazz.append("text-center ");
                    break;
                case RIGHT:
                    clazz.append("text-right ");
                    break;
                default:
                    break;
            }

            if (tmeta.getColor() != null) {
                style.append("color:").append(tmeta.getColorHex()).append(";");
            }

            if (tmeta.isNowrap()) {
                clazz.append("nowrap ");
            }
        }

        processOptions(style, tbodyRowIdx, tbodyColIdx, options);

        if (style.length() > 0) {
            html.append(" style=\"").append(style.toString()).append("\"");
        }
        if (clazz.length() > 0) {
            clazz.setLength(clazz.length() - 1);
            html.append(" class=\"").append(clazz).append("\"");
        }
    }

    /**
     * 格式化
     * @param data
     * @param tmeta
     * @return
     */
    private static String formatData(Object data, Tmeta tmeta) {
        if (data == null) {
            return "";
        } else if (tmeta == null) {
            return data.toString();
        } else if (tmeta.getType() == Type.NUMERIC) {
            return Numbers.format(data);
        } else {
            return data.toString();
        }
    }

    /**
     * 样式自定义处理
     * @param style
     * @param dataRowIdx
     * @param dataColIdx
     * @param options
     */
    @SuppressWarnings("unchecked")
    private static void processOptions(StringBuilder style, int dataRowIdx, int dataColIdx, 
                                       Map<CellStyleOptions, Object> options) {
        if (options == null || options.isEmpty()) {
            return;
        }

        Map<String, Object> highlight = (Map<String, Object>) options.get(CellStyleOptions.HIGHLIGHT);
        if (highlight != null && !highlight.isEmpty()) {
            String color = "color:" + highlight.get("color") + ";font-weight:bold;";
            List<List<Integer>> cells = (List<List<Integer>>) highlight.get("cells");
            for (List<Integer> cell : cells) {
                if (cell.get(0).equals(dataRowIdx) && cell.get(1).equals(dataColIdx)) {
                    style.append(color);
                }
            }
        }

        Function<Object, String> processor = (Function<Object, String>) options.get(CellStyleOptions.CELL_PROCESS);
        if (processor != null) {
            style.append(processor.apply(new Object[] { dataRowIdx, dataColIdx }));
        }
    }

}
