package code.ponfee.commons.exception;

import code.ponfee.commons.model.ResultCode;

/**
 * Checked exception definition
 * 
 * @author Ponfee
 */
public class CheckedException extends BasicException {

    private static final long serialVersionUID = -7059975701644684690L;
    private static final int CODE = ResultCode.SERVER_ERROR.getCode();

    public CheckedException() {
        super(CODE);
    }

    public CheckedException(String message) {
        super(CODE, message);
    }

    public CheckedException(Throwable cause) {
        super(CODE, cause);
    }

    public CheckedException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

    public CheckedException(String message, Throwable cause, 
                            boolean enableSuppression, boolean writableStackTrace) {
        super(CODE, message, cause, enableSuppression, writableStackTrace);
    }

}
