package code.ponfee.commons.exception;

import code.ponfee.commons.model.ResultCode;

/**
 * Checked exception definition
 * 
 * @author Ponfee
 */
public class BaseCheckedException extends BaseUncheckedException {

    private static final long serialVersionUID = -7059975701644684690L;
    private static final int CODE = ResultCode.SERVER_ERROR.getCode();

    public BaseCheckedException() {
        super(CODE);
    }

    public BaseCheckedException(String message) {
        super(CODE, message);
    }

    public BaseCheckedException(Throwable cause) {
        super(CODE, cause);
    }

    public BaseCheckedException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

    public BaseCheckedException(String message, Throwable cause,
                                boolean enableSuppression, boolean writableStackTrace) {
        super(CODE, message, cause, enableSuppression, writableStackTrace);
    }

}
