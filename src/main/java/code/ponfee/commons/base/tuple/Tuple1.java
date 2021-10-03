package code.ponfee.commons.base.tuple;

import java.io.Serializable;
import java.util.*;

/**
 * Tuple1 consisting of an element.
 * 
 * @author Ponfee
 * @param <A> the type A
 */
public class Tuple1<A> implements Iterable<Object>, Serializable {
    private static final long serialVersionUID = -3627925720098458172L;

    public A a;

    public Tuple1(A a) {
        this.a = a;
    }

    public static <A> Tuple1<A> of(A a) {
        return new Tuple1<>(a);
    }

    /**
     * Get the object at the given index.
     *
     * @param index The index of the object to retrieve. Starts at 0.
     * @return The object or {@literal null} if out of bounds.
     */
    public Object get(int index) {
        switch (index) {
            case  0: return a;
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    /**
     * Turn this {@code Tuple} into a plain {@code Object[]}.
     * The array isn't tied to this Tuple but is a <strong>copy</strong>.
     *
     * @return A copy of the tuple as a new {@link Object Object[]}.
     */
    public Object[] toArray() {
        return new Object[]{a};
    }

    /**
     * Turn this {@code Tuple} into a {@link List List&lt;Object&gt;}.
     * The list isn't tied to this Tuple but is a <strong>copy</strong> with limited
     * mutability ({@code add} and {@code remove} are not supported, but {@code set} is).
     *
     * @return A copy of the tuple as a new {@link List List&lt;Object&gt;}.
     */
    public final List<Object> toList() {
        return Arrays.asList(toArray());
    }

    /**
     * Return an <strong>immutable</strong> {@link Iterator Iterator&lt;Object&gt;} around
     * the content of this {@code Tuple}.
     *
     * @return An unmodifiable {@link Iterator} over the elements in this Tuple.
     * @implNote As an {@link Iterator} is always tied to its {@link Iterable} source by
     * definition, the iterator cannot be mutable without the iterable also being mutable.
     */
    @Override
    public final Iterator<Object> iterator() {
        return Collections.unmodifiableList(toList()).iterator();
    }

    @Override
    public String toString() {
        return "(" + a + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(a, ((Tuple1<?>) o).a);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a);
    }

}
