package test.jce.ecc2;

/**
 * An exception for reporting unsupported bases encountered when
 * marshalling/unmarshalling byte arrays from encoded strings.
 */
public class UnsupportedBaseException extends Exception {
    private static final long serialVersionUID = -8963563995880472365L;

    public UnsupportedBaseException(String message) {
        super(message);
    }
}
