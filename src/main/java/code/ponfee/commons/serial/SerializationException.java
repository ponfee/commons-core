package code.ponfee.commons.serial;

/**
 * 序例化异常类
 * @author fupf
 */
public class SerializationException extends RuntimeException {

    private static final long serialVersionUID = -5285807406910063551L;

    public SerializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
