package code.ponfee.commons.extract;

/**
 * 行处理
 * 
 * @author Ponfee
 * @param <T>
 */
@FunctionalInterface
public interface RowProcessor<T> {

    void process(int rowNumber, T rowData);
}
