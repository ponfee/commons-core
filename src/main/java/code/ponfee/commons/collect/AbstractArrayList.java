package code.ponfee.commons.collect;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.RandomAccess;

import com.google.common.base.Preconditions;

/**
 * The primitive array of abstract list
 * 
 * @author Ponfee
 * @param <E>
 */
public abstract class AbstractArrayList<E> extends AbstractList<E>
    implements RandomAccess, Serializable {

    private static final long serialVersionUID = -964514644899401684L;

    protected final int start;
    protected final int end;
    protected final int size;

    public AbstractArrayList(int start, int end) {
        Preconditions.checkArgument(start >= 0 && start < end);
        this.start = start;
        this.end = end;
        this.size = end - start;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return new ArrayIterator(start, end);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return super.listIterator(start + index);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    private class ArrayIterator implements Iterator<E> {
        int start;
        final int end;

        ArrayIterator(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean hasNext() {
            return start != end;
        }

        @Override
        public E next() {
            return get(start++);
        }
    }

}
