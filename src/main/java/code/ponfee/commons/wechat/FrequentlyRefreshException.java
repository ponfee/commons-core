package code.ponfee.commons.wechat;

/**
 * 频繁刷新异常
 * @author Ponfee
 */
public class FrequentlyRefreshException extends Exception {
    private static final long serialVersionUID = -6061724344458773583L;

    public FrequentlyRefreshException() {
        super();
    }

    public FrequentlyRefreshException(String message) {
        super(message);
    }

    public FrequentlyRefreshException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrequentlyRefreshException(Throwable cause) {
        super(cause);
    }
}
