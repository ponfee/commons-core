package code.ponfee.commons.schema;

import java.io.Serializable;
import java.util.List;

/**
 * Data table structure: a DataColumn array of columns 
 * and two-dimensional array dataset<p>
 * 
 * @author Ponfee
 */
public class DataTable implements Serializable {

    private static final long serialVersionUID = 3710299712677057559L;

    private String            name; // 表名
    private String           alias; // 表别名
    private DataColumn[]   columns; // 数据列元数据信息
    private List<Object[]> dataset; // 数据集

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

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
