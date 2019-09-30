package code.ponfee.commons.exception;

/**
 * 异常基础类
 * 
 * @author Ponfee
 */
public class BasicException extends RuntimeException {
    private static final long serialVersionUID = -5678901285130119481L;

    /** 错误编码 */
    private final Integer code;

    public BasicException() {
        this(null, null, null);
    }

    public BasicException(Integer code) {
        this(code, null, null);
    }

    /**
     * @param message 错误消息
     */
    public BasicException(String message) {
        this(null, message, null);
    }

    /**
     * @param cause 异常
     */
    public BasicException(Throwable cause) {
        this(null, null, cause);
    }

    /**
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BasicException(String message, Throwable cause) {
        this(null, message, cause);
    }

    /**
     * @param code 错误编码
     * @param message 错误消息
     */
    public BasicException(Integer code, String message) {
        this(code, message, null);
    }

    /**
     * @param code 错误编码
     * @param cause 异常原因
     */
    public BasicException(Integer code, Throwable cause) {
        this(code, null, cause);
    }

    /**
     * @param code 错误编码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BasicException(Integer code, String message, Throwable cause) {
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
    public BasicException(Integer code, String message, Throwable cause, 
                          boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    /**
     * 取得错误编码
     * @return
     */
    public Integer getCode() {
        return code;
    }

}
