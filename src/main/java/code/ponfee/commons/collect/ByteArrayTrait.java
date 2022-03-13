package code.ponfee.commons.collect;

/**
 * Represents the class has an byte[] array args constructor and a toByteArray method
 * 
 * For serialize
 * 
 * @author Ponfee
 */
public abstract class ByteArrayTrait {

    public ByteArrayTrait(byte[] array) {}

    /**
     * Returns byte array
     *
     * @return byte array
     */
    public abstract byte[] toByteArray();

}
