package code.ponfee.commons.export;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.tree.FlatNode;
import com.google.common.base.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * Console Exporter
 *
 * @author Ponfee
 */
public class ConsoleExporter extends AbstractDataExporter<String> {

    public static final String HORIZON = "\n\n-------------------------------------------------------\n\n";
    public static final String ELLIPSIS_STR = "...";
    public static final int ELLIPSIS_LEN = ELLIPSIS_STR.length();

    protected final Appendable out;
    protected final int maxColumnWidth;
    protected final boolean hasLineSeparator;

    public ConsoleExporter(Appendable out) {
        this(out, 36, false);
    }

    public ConsoleExporter(Appendable out, int maxColumnWidth, boolean rowSeparator) {
        this.out = out;
        this.maxColumnWidth = Math.max(maxColumnWidth, ELLIPSIS_LEN + 1);
        this.hasLineSeparator = rowSeparator;
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

        try {
            // horizon
            horizon();

            // table start
            List<Column> columns = union(
                new Column(new Thead(("#"))),
                getLeafThead(table.getThead()).stream().map(Column::new).collect(Collectors.toList())
            );

            LongAdder rowCount = new LongAdder();
            rollingTbody(table, (data, i) -> {
                // row number
                Column first = columns.get(0);
                String rowNumber = Integer.toString(i + 1);
                first.values.add(rowNumber);
                first.width = Numbers.bounds(rowNumber.length(), first.width, maxColumnWidth);

                // each row
                for (int m = data.length, colIdx = 0; colIdx < m; colIdx++) {
                    Column column = columns.get(colIdx + 1);
                    String value = Objects.toString(data[colIdx], "");
                    column.values.add(value);
                    column.width = Numbers.bounds(value.length(), column.width, maxColumnWidth);
                }
                rowCount.increment();
            });

            // print caption
            int rowWidth = columns.stream().mapToInt(e -> e.width + 3).sum() + 1;
            String caption = Objects.toString(table.getCaption(), "");
            append("+-").append('-', rowWidth - 4).append("-+").newLine();
            append("| ").center(caption, rowWidth - 4).append(" |").newLine();

            String separator = "+-" + columns.stream().map(e -> Strings.repeat("-", e.width)).collect(Collectors.joining("-+-")) + "-+";

            // print thead
            append(separator).newLine();
            for (Column col : columns) {
                append("| ").center(col.getName(), col.width).append(' ');
            }
            append('|').newLine();
            append(separator).newLine();

            // print rows
            for (int n = rowCount.intValue(), rowIdx = 0; rowIdx < n; rowIdx++) {
                if (hasLineSeparator && rowIdx > 0) {
                    append(separator).newLine();
                }
                for (Column col : columns) {
                    append("| ").append(col.values.get(rowIdx), col.width).append(' ');
                }
                append('|').newLine();
            }
            append(separator).newLine();

            nonEmpty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String export() {
        return out.toString();
    }

    // ------------------------------------------------------------private methods
    private ConsoleExporter horizon() throws IOException {
        if (!isEmpty()) {
            out.append(HORIZON);
        }
        return this;
    }

    private ConsoleExporter append(char c) throws IOException {
        out.append(c);
        return this;
    }

    private ConsoleExporter append(char c, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            out.append(c);
        }
        return this;
    }

    private ConsoleExporter append(CharSequence text) throws IOException {
        out.append(text);
        return this;
    }

    private ConsoleExporter append(String text, int width) throws IOException {
        int padding = width - text.length();
        if (padding >= 0) {
            append(text).append(' ', padding);
        } else {
            out.append(text, 0, width - ELLIPSIS_LEN);
            out.append(ELLIPSIS_STR);
        }
        return this;
    }

    private ConsoleExporter center(String text, int width) throws IOException {
        int padding = width - text.length();
        if (padding >= 0) {
            append(' ', padding / 2).append(text).append(' ', (padding + 1) / 2);
        } else {
            out.append(text, 0, width - ELLIPSIS_LEN);
            out.append(ELLIPSIS_STR);
        }
        return this;
    }

    private void newLine() throws IOException {
        out.append('\n');
    }

    private static <T> List<T> union(T first, Collection<T> coll) {
        List<T> list = new ArrayList<>(coll.size() + 1);
        list.add(first);
        list.addAll(coll);
        return list;
    }

    private class Column extends Thead {
        private static final long serialVersionUID = -5764311953058980984L;
        private final List<String> values = new ArrayList<>();
        private int width;

        public Column(Thead th) {
            super(th.getName(), th.getTmeta(), th.getField());
            this.width = Math.min(th.getName().length(), maxColumnWidth);
        }
    }

}
