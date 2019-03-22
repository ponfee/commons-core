package code.ponfee.commons.exception;

/**
 * Exception for unauthorized
 * 
 * @author Ponfee
 */
public class UnauthorizedException extends BasicException {
    private static final long serialVersionUID = -5678901285130119481L;

    public UnauthorizedException() {
        super(401);
    }

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public UnauthorizedException(Throwable cause) {
        super(401, cause);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(401, message, cause);
    }

}
