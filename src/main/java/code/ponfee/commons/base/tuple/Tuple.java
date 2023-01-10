package code.ponfee.commons.base.tuple;

import code.ponfee.commons.base.Comparators;
import code.ponfee.commons.collect.DelegatedIntSpliterator;
import code.ponfee.commons.collect.ImmutableArrayList;
import code.ponfee.commons.util.ObjectUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;

/**
 * Abstract Tuple type.
 *
 * @author Ponfee
 */
public abstract class Tuple implements Comparable<Object>, Iterable<Object>, Serializable {
    private static final long serialVersionUID = -3292038317953347997L;
    public static final int HASH_FACTOR = 31;

    /**
     * Get the object at the given index.
     *
     * @param index The index of the object to retrieve. Starts at 0.
     * @return The object or {@literal null} if out of bounds.
     */
    public abstract <T> T get(int index);

    /**
     * Set the value at the given index.
     *
     * @param value The object value.
     * @param index The index of the object to retrieve. Starts at 0.
     */
    public abstract <T> void set(T value, int index);

    /**
     * Turn this {@code Tuple} into a plain {@code Object[]}.
     * The array isn't tied to this Tuple but is a <strong>copy</strong>.
     *
     * @return A copy of the tuple as a new {@link Object Object[]}.
     */
    public abstract Object[] toArray();

    /**
     * Override the Object toString method as abstract.
     *
     * @return a string representation of the Tuple.
     */
    @Override
    public abstract String toString();

    /**
     * Override the Object equals method as abstract.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object equals the other.
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Override the Object hashCode method as abstract.
     *
     * @return a hash code value for this object.
     */
    @Override
    public abstract int hashCode();

    /**
     * Returns a copy of this instance.
     *
     * @return a copy of this instance.
     */
    public abstract <T> T copy();

    /**
     * Returns int value of this tuple elements count.
     *
     * @return int value of this tuple elements count.
     */
    public abstract int length();

    /**
     * Turn this {@code Tuple} into a {@link List List&lt;Object&gt;}.
     * The list isn't tied to this Tuple but is a <strong>copy</strong> with limited
     * mutability ({@code add} and {@code remove} are not supported, but {@code set} is).
     *
     * @return A copy of the tuple as a new {@link List List&lt;Object&gt;}.
     */
    public List<Object> toList() {
        return ImmutableArrayList.of(toArray());
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
    public Iterator<Object> iterator() {
        return new TupleIterator<>(); // toList().iterator();
    }

    @Override
    public Spliterator<Object> spliterator() {
        return new DelegatedIntSpliterator<>(0, length(), this::get);
    }

    @Override
    public final int compareTo(Object o) {
        if (this == o) {
            return Comparators.EQ;
        }
        if (!(o instanceof Tuple)) {
            return ObjectUtils.compare(this, o);
        }

        Tuple other = (Tuple) o;
        for (int c, i = 0, n = this.length(); i < n; i++) {
            c = ObjectUtils.compare(this.get(i), other.get(i));
            if (c != Comparators.EQ) {
                return c;
            }
        }

        return Comparators.EQ;
    }

    /**
     * Tuple Iterator
     *
     * @param <T> element type
     */
    private class TupleIterator<T> implements Iterator<T> {
        int position = 0;
        final int size = length();

        @Override
        public boolean hasNext() {
            return position < size;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return get(position++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
