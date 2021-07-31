package code.ponfee.commons.schema;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Grid table for front view
 * 
 * @author Ponfee
 */
public class GridTable implements Serializable {

    private static final long serialVersionUID = 4900630719709337101L;

    private Columns[]       columns; // 表头
    private NormalStructure dataset; // 表体

    public static GridTable of(TableStructure ts) {
        if (ts == null) {
            return null;
        }

        GridTable table = new GridTable();
        table.setColumns(Columns.convert(ts.getColumns()));
        table.setDataset(ts.toNormal());
        return table;
    }

    public static class Columns implements Serializable {
        private static final long serialVersionUID = 1L;

        private String title;
        private String dataIndex;
        private String key;

        public Columns() {}

        public Columns(String title, String name) {
            this.title = StringUtils.isBlank(title) ? name : title;
            this.dataIndex = name;
            this.key = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDataIndex() {
            return dataIndex;
        }

        public void setDataIndex(String dataIndex) {
            this.dataIndex = dataIndex;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public static Columns convert(DataColumn column) {
            return new Columns(column.getAlias(), column.getName());
        }

        public static Columns[] convert(DataColumn[] columns) {
            if (columns == null) {
                return null;
            }
            return Arrays.stream(columns).map(Columns::convert).toArray(Columns[]::new);
        }
    }

    // ------------------------------------------------------------------------getter/setter
    public Columns[] getColumns() {
        return columns;
    }

    public void setColumns(Columns[] columns) {
        this.columns = columns;
    }

    public NormalStructure getDataset() {
        return dataset;
    }

    public void setDataset(NormalStructure dataset) {
        this.dataset = dataset;
    }

}
