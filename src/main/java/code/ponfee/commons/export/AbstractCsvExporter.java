package code.ponfee.commons.export;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.tree.FlatNode;

/**
 * Exports csv
 * 
 * @author Ponfee
 */
public abstract class AbstractCsvExporter<T> extends AbstractDataExporter<T> {

    static final byte[] WINDOWS_BOM = {
        (byte) 0xEF, (byte) 0xBB, (byte) 0xBF
    };

    protected final Appendable csv;
    private final char csvSeparator;
    private final AtomicBoolean hasBuild = new AtomicBoolean(false);

    public AbstractCsvExporter(Appendable csv) {
        this(csv, ',');
    }

    public AbstractCsvExporter(Appendable csv, char csvSeparator) {
        this.csv = csv;
        this.csvSeparator = csvSeparator;
    }

    @Override
    public final <E> void build(Table<E> table) {
        if (hasBuild.getAndSet(true)) {
            throw new UnsupportedOperationException("Only support signle table.");
        }

        List<FlatNode<Integer, Thead>> thead = table.getThead();
        if (CollectionUtils.isEmpty(thead)) {
            throw new IllegalArgumentException("Thead cannot be null.");
        }

        // build table thead
        buildComplexThead(thead);

        // tbody---------------
        rollingTbody(table, (data, i) -> {
            try {
                for (int m = data.length - 1, j = 0; j <= m; j++) {
                    // escapeCsv(String.valueOf(data[j]), csvSeparator);
                    csv.append(String.valueOf(data[j]));
                    if (j < m) {
                        csv.append(csvSeparator);
                    }
                }
                csv.append(Files.SYSTEM_LINE_SEPARATOR); // 换行
                //if ((i & 0xFF) == 0) {
                //    this.flush();
                //}
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            if (table.isEmptyTbody()) {
                csv.append(NO_RESULT_TIP);
            } else {
                super.nonEmpty();
            }

            // tfoot---------
            if (ArrayUtils.isNotEmpty(table.getTfoot())) {
                FlatNode<Integer, Thead> root = thead.get(0);
                if (table.getTfoot().length > root.getChildLeafCount()) {
                    throw new IllegalStateException("Tfoot length cannot more than total leaf count.");
                }

                int n = root.getChildLeafCount(), m = table.getTfoot().length, mergeNum = n - m;
                for (int i = 0; i < mergeNum; i++) {
                    if (i == mergeNum - 1) {
                        csv.append("合计");
                    }
                    csv.append(csvSeparator);
                }
                for (int i = mergeNum; i < n; i++) {
                    // escapeCsv(String.valueOf((table.getTfoot()[i - mergeNum])), csvSeparator);
                    csv.append(String.valueOf(table.getTfoot()[i - mergeNum]));
                    if (i != n - 1) {
                        csv.append(csvSeparator);
                    }
                }

                csv.append(Files.SYSTEM_LINE_SEPARATOR);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //protected void flush() {}

    private void buildComplexThead(List<FlatNode<Integer, Thead>> thead) {
        List<Thead> leafs = super.getLeafThead(thead);
        try {
            for (int i = 0, n = leafs.size(); i < n; i++) {
                csv.append(leafs.get(i).getName());
                if (i != n - 1) {
                    csv.append(csvSeparator);
                }
            }
            csv.append(Files.SYSTEM_LINE_SEPARATOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 创建简单表头
    /*private void buildSimpleThead(String[] theadName) {
        for (String th : theadName) {
            csv.append(th).append(csvSeparator);
        }
        csv.setLength(csv.length() - 1);
        csv.append(Files.LINE_SEPARATOR);
    }*/

    public static String escapeCsv(String text) {
        return escapeCsv(text, ',');
    }

    public static String escapeCsv(String text, char separator) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }

        if (text.contains("\"")) {
            text = text.replace("\"", "\"\"");
        }
        if (StringUtils.contains(text, separator)) {
            //String.format("\"%s\"", text)
            text = new StringBuilder(text.length() + 2)
                .append('"').append(text).append('"').toString();
        }
        return text;
    }

}
