package code.ponfee.commons.extract;

/**
 * 行处理
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface RowProcessor {

    void process(int rowNumber, String[] rowData);
}
