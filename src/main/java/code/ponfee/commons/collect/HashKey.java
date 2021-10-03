package code.ponfee.commons.collect;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Arrays;

/**
 * The class use in Object array as hash map key
 * <p>Use for HashMap key
 *
 * @author Ponfee
 */
public final class HashKey implements java.io.Serializable, Comparable<HashKey> {

    private static final long serialVersionUID = -8749483734287105153L;

    private final Object[] key;
    private final int hashCode;

    public HashKey(Object... key) {
        this.key = key;
        this.hashCode = Arrays.hashCode(key);
    }

    public static HashKey of(Object... key) {
        return new HashKey(key);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return (other instanceof HashKey) ? Arrays.equals(key, ((HashKey) other).key) : false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public int compareTo(HashKey o) {
        return new CompareToBuilder().append(key, o.key).toComparison();
    }

}
