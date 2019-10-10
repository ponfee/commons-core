package code.ponfee.commons.collect;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Arrays;

/**
 * The class use in Object array as hash map key
 * 
 * For HashMap key
 * 
 * @author Ponfee
 */
public class ObjectArrayWrapper<T> implements 
    java.io.Serializable, Comparable<ObjectArrayWrapper<T>> {

    private static final long serialVersionUID = -8749483734287105153L;

    private final T[] array;
    private final int hashCode;

    @SuppressWarnings("unchecked")
    public ObjectArrayWrapper(T... array) {
        this.array = array;
        this.hashCode = Arrays.hashCode(array);
    }

    @SafeVarargs
    public static <T> ObjectArrayWrapper<T> of(T... array) {
        return new ObjectArrayWrapper<>(array);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ObjectArrayWrapper) {
            return Arrays.equals(array, ((ObjectArrayWrapper<?>) other).array);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
        /*int result = array[0].hashCode();
        for (int i = 1, n = array.length; i < n; i++) {
            result ^= array[i].hashCode();
        }
        return result;*/
    }

    @Override
    public int compareTo(ObjectArrayWrapper<T> o) {
        return new CompareToBuilder().append(array, o.array).toComparison();
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
        return "ObjectArrayWrapper[" + (array == null ? "null" : array.length) + "]";
    }

}
