package code.ponfee.commons.schema;

import code.ponfee.commons.json.Jsons;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * {
 *   "columns":[
 *     {"name":"name","type":"STRING", "alias":"姓名"},
 *     {"name":"age", "type":"INTEGER","alias":"年龄"}
 *   ],
 *   "dataset":[
 *     ["alice",10],
 *     ["bob",  18],
 *     ["tom",  31]
 *   ],
 * }
 * 
 * @author Ponfee
 */
public final class TableStructure implements DataStructure {
    private static final long serialVersionUID = 1L;

    private DataColumn[]   columns; // 数据列元数据信息
    private List<Object[]> dataset; // 数据集二维表数据

    public TableStructure() {}

    public TableStructure(DataColumn[] columns, List<Object[]> dataset) {
        this.columns = columns;
        this.dataset = dataset;
    }

    @Override
    public NormalStructure toNormal() {
        NormalStructure list = new NormalStructure();
        for (Object[] row : dataset) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>(row.length);
            for (int i = 0; i < row.length; i++) {
                map.put(columns[i].getName(), row[i]); // columns[i].getType().convert(row[i])
            }
            list.add(map);
        }
        return list;
    }

    @Override
    public TableStructure toTable() {
        return this;
    }

    @Override
    public PlainStructure toPlain() {
        return new PlainStructure(Jsons.toJson(this));
    }

    public static TableStructure of(DataTable table) {
        return new TableStructure(table.getColumns(), table.getDataset());
    }

    // ---------------------------------------------------------------------getter/setter
    public DataColumn[] getColumns() {
        return columns;
    }

    public void setColumns(DataColumn[] columns) {
        this.columns = columns;
    }

    public List<Object[]> getDataset() {
        return dataset;
    }

    public void setDataset(List<Object[]> dataset) {
        this.dataset = dataset;
    }

}
