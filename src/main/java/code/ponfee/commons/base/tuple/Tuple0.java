package code.ponfee.commons.base.tuple;

import code.ponfee.commons.collect.ImmutableArrayList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Tuple0 consisting of empty element.
 *
 * @author Ponfee
 */
public final class Tuple0 extends Tuple {
    private static final long serialVersionUID = -3627925720098458172L;
    private static final Tuple0 INSTANCE = new Tuple0();

    public Tuple0() {
    }

    public static Tuple0 of() {
        return INSTANCE;
    }

    @Override
    public <T> T get(int index) {
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public <T> void set(T value, int index) {
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public Object[] toArray() {
        return ImmutableArrayList.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public String toString() {
        return "()";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Tuple0;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public Tuple0 copy() {
        return INSTANCE;
    }

    @Override
    public List<Object> toList() {
        return Collections.emptyList();
    }

    @Override
    public Iterator<Object> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Spliterator<Object> spliterator() {
        return Spliterators.emptySpliterator();
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
