package code.ponfee.commons.exception;

import code.ponfee.commons.model.ResultCode;

/**
 * Unimplemented exception definition
 * 
 * @author Ponfee
 */
public class UnimplementedException extends BasicException {

    private static final long serialVersionUID = -5983398403463732650L;
    private static final int CODE = ResultCode.SERVER_UNSUPPORT.getCode();

    public UnimplementedException() {
        super(CODE);
    }

    public UnimplementedException(String message) {
        super(CODE, message);
    }

    public UnimplementedException(Throwable cause) {
        super(CODE, cause);
    }

    public UnimplementedException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

    public UnimplementedException(String message, Throwable cause, 
                                  boolean enableSuppression, boolean writableStackTrace) {
        super(CODE, message, cause, enableSuppression, writableStackTrace);
    }

}
