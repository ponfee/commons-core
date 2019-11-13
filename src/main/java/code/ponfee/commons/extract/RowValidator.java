package code.ponfee.commons.extract;

/**
 * Verify row data
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface RowValidator {

    /**
     * If Returns null or blank string then verify success
     * else verify fail
     * 
     * @param rowNumber the row number
     * @param rowData the row data
     * @return a string, if null or blank string then verify success
     */
    String verify(int rowNumber, String[] rowData);
}
