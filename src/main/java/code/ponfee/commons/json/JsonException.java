package code.ponfee.commons.json;

/**
 * json异常类
 * 
 * @author Ponfee
 */
public class JsonException extends RuntimeException {
    private static final long serialVersionUID = 8109219010796537426L;

    public JsonException() {
        super();
    }

    public JsonException(String message) {
        super(message);
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonException(Throwable cause) {
        super(cause);
    }

    protected JsonException(String message, Throwable cause,
                            boolean enableSuppression,
                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
