package code.ponfee.commons.collect;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Arrays;

/**
 * The class use in Object array as hash map key
 * 
 * @author Ponfee
 */
public class ObjectArrayWrapper<T> implements 
    java.io.Serializable, Comparable<ObjectArrayWrapper<T>> {

    private static final long serialVersionUID = -8749483734287105153L;

    private final T[] array;

    public ObjectArrayWrapper(T[] array) {
        if (array == null) {
            throw new NullPointerException();
        }
        this.array = array;
    }

    @SafeVarargs
    public static <T> ObjectArrayWrapper<T> create(T... array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return new ObjectArrayWrapper<>(array);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ObjectArrayWrapper)) {
            return false;
        }
        return Arrays.equals(array, ((ObjectArrayWrapper<?>) other).array);

        //return new EqualsBuilder().append(array, ((ObjectArrayWrapper<?>) other).array).isEquals();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);

        //return new HashCodeBuilder().append(array).toHashCode();
        /*int result = array[0].hashCode();
        for (int i = 1, n = array.length; i < n; i++) {
            result ^= array[i].hashCode();
        }
        return result;*/
    }

    @Override
    public int compareTo(ObjectArrayWrapper<T> o) {
        if (o == null) {
            return 1;
        }
        return new CompareToBuilder().append(array, o.array)
                                     .toComparison();
    }

    /**
     * Returns the byte array
     * 
     * @return a byte array
     */
    public T[] getArray() {
        return array;
    }

    @Override
    public String toString() {
        return "ObjectArrayWrapper [" + 
                (array == null ? "null" : "array.length=" + array.length)
             + "]";
    }

}
