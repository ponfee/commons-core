package code.ponfee.commons.exception;

import code.ponfee.commons.model.ResultCode;

/**
 * Unimplemented exception definition
 *
 * @author Ponfee
 */
public class UnimplementedException extends BaseUncheckedException {
    private static final long serialVersionUID = -5983398403463732650L;

    private static final int CODE = ResultCode.SERVER_UNSUPPORTED.getCode();

    public UnimplementedException() {
        super(CODE, null, null);
    }

    public UnimplementedException(String message) {
        super(CODE, message, null);
    }

    public UnimplementedException(Throwable cause) {
        super(CODE, null, cause);
    }

    public UnimplementedException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

    public UnimplementedException(String message,
                                  Throwable cause,
                                  boolean enableSuppression,
                                  boolean writableStackTrace) {
        super(CODE, message, cause, enableSuppression, writableStackTrace);
    }

}
