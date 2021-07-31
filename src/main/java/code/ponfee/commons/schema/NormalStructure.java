package code.ponfee.commons.schema;

import code.ponfee.commons.json.Jsons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * [
 *   {"name":"alice", "age":10},
 *   {"name":"bob",   "age":18},
 *   {"name":"tom",   "age":31}
 * ]
 * 
 * @author Ponfee
 */
public final class NormalStructure extends ArrayList<LinkedHashMap<String, Object>> implements DataStructure {

    private static final long serialVersionUID = 9067243551591375987L;

    public NormalStructure() {}

    public NormalStructure(int minCapacity) {
        super(minCapacity);
    }

    @Override
    public NormalStructure toNormal() {
        return this;
    }

    @Override
    public TableStructure toTable() {
        List<Object[]> dataset = new ArrayList<>(this.size());
        int r = 0, c = 0; // r: row index, c: column index

        // first row
        LinkedHashMap<String, Object> map = this.get(r++);
        DataColumn[] columns = new DataColumn[map.size()];
        Object[] row = new Object[map.size()];
        for (Iterator<Entry<String, Object>> iter = map.entrySet().iterator(); iter.hasNext(); c++) {
            Entry<String, Object> entry = iter.next();
            DataType type = DataType.detect(Objects.toString(entry.getValue(), null));
            columns[c] = new DataColumn(entry.getKey(), type, null);
            row[c] = columns[c].getType().convert(entry.getValue());
        }
        dataset.add(row);

        for (int n = this.size(); r < n; r++) {
            map = this.get(r);
            row = new Object[map.size()];
            for (c = 0; c < columns.length; c++) {
                row[c] = map.get(columns[c].getName()); // columns[c].getType().convert(map.get(columns[c].getName()));
            }
            dataset.add(row);
        }

        return new TableStructure(columns, dataset);
    }

    @Override
    public PlainStructure toPlain() {
        return new PlainStructure(Jsons.toJson(this));
    }

}
