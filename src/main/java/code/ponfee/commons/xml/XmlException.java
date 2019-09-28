package code.ponfee.commons.xml;

/**
 * xml文件异常
 * 
 * @author Ponfee
 */
public class XmlException extends RuntimeException {
    private static final long serialVersionUID = 1112070147872432069L;

    public XmlException() {
        super();
    }

    public XmlException(String message) {
        super(message);
    }

    public XmlException(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlException(Throwable cause) {
        super(cause);
    }

    protected XmlException(String message, Throwable cause, 
                           boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
