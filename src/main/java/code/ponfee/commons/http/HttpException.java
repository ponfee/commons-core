package code.ponfee.commons.http;

/**
 * http exception
 * @author Ponfee
 */
public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 7195686343121118928L;

    public HttpException() {
        super();
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(Throwable cause) {
        super(cause);
    }

    protected HttpException(String message, Throwable cause, 
                            boolean enableSuppression, 
                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
