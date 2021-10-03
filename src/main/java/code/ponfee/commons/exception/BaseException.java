package code.ponfee.commons.exception;

/**
 * Basic exception definition
 * 
 * @author Ponfee
 */
public abstract class BaseException extends RuntimeException {

    private static final long serialVersionUID = -5678901285130119481L;

    /** 错误编码 */
    private final int code;

    public BaseException(int code) {
        this(code, null, null);
    }

    /**
     * @param code 错误编码
     * @param message 错误消息
     */
    public BaseException(int code, String message) {
        this(code, message, null);
    }

    /**
     * @param code 错误编码
     * @param cause 异常原因
     */
    public BaseException(int code, Throwable cause) {
        this(code, null, cause);
    }

    /**
     * @param code 错误编码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BaseException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @param code     error code
     * @param message  detail msg
     * @param cause    the cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public BaseException(int code, String message, Throwable cause,
                         boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    /**
     * 取得错误编码
     * 
     * @return
     */
    public int getCode() {
        return code;
    }

}
