package code.ponfee.commons.exception;

import code.ponfee.commons.model.ResultCode;

/**
 * Unauthorized exception definition
 * 
 * @author Ponfee
 */
public class UnauthorizedException extends BaseUncheckedException {

    private static final long serialVersionUID = -5678901285130119481L;
    private static final int CODE = ResultCode.UNAUTHORIZED.getCode();

    public UnauthorizedException() {
        super(CODE);
    }

    public UnauthorizedException(String message) {
        super(CODE, message);
    }

    public UnauthorizedException(Throwable cause) {
        super(CODE, cause);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

    public UnauthorizedException(String message, Throwable cause, 
                                 boolean enableSuppression, boolean writableStackTrace) {
        super(CODE, message, cause, enableSuppression, writableStackTrace);
    }

}
