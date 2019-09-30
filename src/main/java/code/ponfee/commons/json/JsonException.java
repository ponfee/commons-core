package code.ponfee.commons.json;

import code.ponfee.commons.exception.BasicException;

/**
 * json异常类
 * 
 * @author Ponfee
 */
public class JsonException extends BasicException {
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

}
