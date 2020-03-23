package code.ponfee.commons.exception;

/**
 * Checked expcetion definition
 * 
 * @author Ponfee
 */
public class CheckedException extends RuntimeException {

    private static final long serialVersionUID = -7059975701644684690L;

    public CheckedException() {
        super();
    }

    public CheckedException(String message) {
        super(message);
    }

    public CheckedException(Throwable cause) {
        super(cause);
    }

    public CheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CheckedException(String message, Throwable cause, 
                            boolean enableSuppression, 
                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
