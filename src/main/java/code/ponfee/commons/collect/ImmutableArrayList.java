package code.ponfee.commons.collect;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Representing immutable ArrayList
 *
 * @param <T> the element type
 * @author Ponfee
 */
public abstract class ImmutableArrayList<T> extends ArrayList<T> {
    private static final long serialVersionUID = -3263696598948169517L;

    @SuppressWarnings("unchecked")
    public ImmutableArrayList(@NotEmpty T... array) {
        super(array.length);
        for (T e : array) {
            super.add(e);
        }
    }

    public ImmutableArrayList(@NotEmpty T[] array, T last) {
        super(array.length + 1);
        for (T e : array) {
            super.add(e);
        }
        super.add(last);
    }

    public ImmutableArrayList(@NotEmpty List<T> list) {
        super(list.size());
        for (T e : list) {
            super.add(e);
        }
    }

    public ImmutableArrayList(@NotEmpty List<T> list, T last) {
        super(list.size() + 1);
        for (T e : list) {
            super.add(e);
        }
        super.add(last);
    }

    // --------------------------------------------------------------------------override list methods
    @Override
    public final List<T> subList(int fromIndex, int toIndex) {
        return Collections.unmodifiableList(super.subList(fromIndex, toIndex));
    }

    @Override
    public final Iterator<T> iterator() {
        return listIterator(0);
    }

    @Override
    public final ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public final ListIterator<T> listIterator(int index) {
        final ListIterator<T> iter = super.listIterator(index);
        return new ListIterator<T>() {
            public boolean hasNext()     {return iter.hasNext();}
            public T next()              {return iter.next();}
            public boolean hasPrevious() {return iter.hasPrevious();}
            public T previous()          {return iter.previous();}
            public int nextIndex()       {return iter.nextIndex();}
            public int previousIndex()   {return iter.previousIndex();}

            public void remove() { throw new UnsupportedOperationException(); }
            public void set(T e) { throw new UnsupportedOperationException(); }
            public void add(T e) { throw new UnsupportedOperationException(); }

            @Override
            public void forEachRemaining(Consumer<? super T> action) { iter.forEachRemaining(action); }
        };
    }

    // --------------------------------------------------------------------------unsupported operation
    @Override @Deprecated
    public final boolean add(T e) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final boolean addAll(Collection<? extends T> c) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final T set(int index, T element) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final void add(int index, T element) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final T remove(int index) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final boolean remove(Object o) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final void clear() { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final boolean addAll(int index, Collection<? extends T> c) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    protected final void removeRange(int fromIndex, int toIndex) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final boolean removeIf(Predicate<? super T> filter) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final void replaceAll(UnaryOperator<T> operator) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final void sort(Comparator<? super T> c) { throw new UnsupportedOperationException(); }

    @Override @Deprecated
    public final void trimToSize() { throw new UnsupportedOperationException(); }

}
