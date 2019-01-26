package code.ponfee.commons.collect;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The class use in byte array as hash map key
 * 
 * @author Ponfee
 */
public class ByteArrayWrapper implements 
    java.io.Serializable, Comparable<ByteArrayWrapper> {

    private static final long serialVersionUID = -8749483734287105153L;

    private final byte[] array;

    public ByteArrayWrapper(byte[] array) {
        if (array == null) {
            throw new NullPointerException();
        }
        this.array = array;
    }

    public ByteArrayWrapper(Byte[] array) {
        if (array == null) {
            throw new NullPointerException();
        }
        this.array = ArrayUtils.toPrimitive(array);
    }

    public static ByteArrayWrapper create(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return new ByteArrayWrapper(array);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ByteArrayWrapper)) {
            return false;
        }
        return Arrays.equals(array, ((ByteArrayWrapper) other).array);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public int compareTo(ByteArrayWrapper o) {
        if (o == null) {
            return 1;
        }
        return ByteArrayComparator.compareTo(array, o.array);
        /*return new CompareToBuilder().append(array, o.array)
                                     .toComparison();
        return Bytes.toBigInteger(array).compareTo(
            Bytes.toBigInteger(o.array)
        );*/
    }

    /**
     * Returns the byte array
     * 
     * @return a byte array
     */
    public byte[] getArray() {
        return array;
    }

    @Override
    public String toString() {
        return new StringBuilder("ByteArrayWrapper [")
            .append(array.length).append(']').toString();
    }

}
