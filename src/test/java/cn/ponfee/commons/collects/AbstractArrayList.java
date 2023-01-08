package cn.ponfee.commons.collects;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * The primitive array of abstract list
 * 
 * primitive array convert to list
 * 
 * @author Ponfee
 * @param <E>
 */
public abstract class AbstractArrayList<E> extends AbstractList<E>
    implements RandomAccess, Serializable {

    private static final long serialVersionUID = -964514644899401684L;
    static final int INDEX_NOT_FOUND = -1;

    protected final int start;
    protected final int end;
    protected final int size;

    public AbstractArrayList(int start, int end) {
        Preconditions.checkArgument(start >= 0 && start <= end);
        this.start = start;
        this.end = end;
        this.size = end - start;
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public final boolean isEmpty() {
        return size == 0;
    }

    @Override
    public final boolean contains(Object target) {
        return indexOf(target) != INDEX_NOT_FOUND;
    }

    @Override
    public final Iterator<E> iterator() {
        return new ArrayIterator(0);
    }

    @Override
    public final ListIterator<E> listIterator(int index) {
        return super.listIterator(index);
    }

    private class ArrayIterator implements Iterator<E> {
        int cursor;

        ArrayIterator(int cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public E next() {
            return get(cursor++);
        }
    }

    // --------------------------------------------------------deprecated methods
    @Override @Deprecated
    public final boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public final void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public final E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public final boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    protected final void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public final boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public final boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public final boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public final boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

}
