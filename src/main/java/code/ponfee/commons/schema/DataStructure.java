package code.ponfee.commons.schema;

/**
 * 数据格式标记类：结构化的数据
 * 
 * @author Ponfee
 */
public interface DataStructure extends java.io.Serializable {

    default String structure() {
        return DataStructures.ofType(this.getClass()).name();
    }

    NormalStructure toNormal();

    TableStructure toTable();

    PlainStructure toPlain();

}
