package code.ponfee.commons.exception;

import code.ponfee.commons.model.CodeMsg;

/**
 * Base checked exception definition
 *
 * @author Ponfee
 */
public abstract class BaseCheckedException extends Exception {
    private static final long serialVersionUID = -1199930172272040396L;

    /**
     * Error code
     */
    private final int code;

    public BaseCheckedException(int code) {
        this(code, null, null);
    }

    public BaseCheckedException(CodeMsg codeMsg) {
        this(codeMsg.getCode(), codeMsg.getMsg(), null);
    }

    /**
     * @param code    error code
     * @param message error message
     */
    public BaseCheckedException(int code, String message) {
        this(code, message, null);
    }

    public BaseCheckedException(CodeMsg codeMsg, Throwable cause) {
        this(codeMsg.getCode(), codeMsg.getMsg(), cause);
    }

    /**
     * @param code    error code
     * @param message error message
     * @param cause   root cause
     */
    public BaseCheckedException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @param code               error code
     * @param message            error message
     * @param cause              root cause
     * @param enableSuppression  the enableSuppression
     * @param writableStackTrace then writableStackTrace
     */
    public BaseCheckedException(int code,
                                String message,
                                Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    /**
     * Returns the error code
     *
     * @return int value of error code
     */
    public int getCode() {
        return code;
    }

}
