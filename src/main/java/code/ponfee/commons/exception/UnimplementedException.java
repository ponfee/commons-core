package code.ponfee.commons.exception;

/**
 * 未实现（方法）的异常
 * 
 * @author Ponfee
 */
public class UnimplementedException extends BasicException {
    private static final long serialVersionUID = 8031675925487573509L;

    public UnimplementedException() {
        super();
    }

    public UnimplementedException(int code) {
        super(code);
    }

    public UnimplementedException(String message) {
        super(message);
    }

    public UnimplementedException(Throwable cause) {
        super(cause);
    }

    public UnimplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnimplementedException(int code, String message) {
        super(code, message);
    }

    public UnimplementedException(int code, Throwable cause) {
        super(code, cause);
    }

    public UnimplementedException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
