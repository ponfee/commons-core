package code.ponfee.commons.collect;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The class use in byte array as hash map key
 * 
 * For HashMap key
 * 
 * @author Ponfee
 * @see org.springframework.data.redis.connection.util.ByteArrayWrapper
 */
public class ByteArrayWrapper implements 
    java.io.Serializable, Comparable<ByteArrayWrapper> {

    private static final long serialVersionUID = -8749483734287105153L;

    private final byte[] array;
    private final int hashCode;

    public ByteArrayWrapper(Byte[] array) {
        this(ArrayUtils.toPrimitive(array));
    }

    public ByteArrayWrapper(byte[] array) {
        this.array = array;
        this.hashCode = Arrays.hashCode(this.array);
    }

    public static ByteArrayWrapper of(byte[] array) {
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
        return hashCode;
    }

    @Override
    public int compareTo(ByteArrayWrapper o) {
        if (o == null) {
            return 1;
        }
        return ByteArrayComparator.compareTo(array, o.array);
        /*return Bytes.toBigInteger(array).compareTo(
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
