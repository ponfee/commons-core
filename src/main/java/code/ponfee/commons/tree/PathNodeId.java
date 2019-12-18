package code.ponfee.commons.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;

import com.google.common.base.Preconditions;

/**
 * The node id for path
 * 
 * @author Ponfee
 * @param <T> the node id type
 */
public final class PathNodeId<T extends Serializable & Comparable<? super T>> 
    extends ArrayList<T> implements Serializable, Comparable<PathNodeId<T>>, Cloneable {

    private static final long serialVersionUID = 9090552044337950223L;

    private final boolean initialized;

    public PathNodeId() {
        // for json deserialize
        initialized = true;
    }

    @SuppressWarnings("unchecked")
    public PathNodeId(@NotEmpty T... path) {
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(path));
        Collections.addAll(this, path);
        initialized = true;
    }

    public PathNodeId(@NotEmpty List<T> path) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(path));
        this.addAll(path);
        initialized = true;
    }

    public PathNodeId(@NotEmpty PathNodeId<T> parent, T child) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(parent));
        this.addAll(parent);
        this.add(child);
        initialized = true;
    }

    @Override
    public int compareTo(PathNodeId<T> o) {
        int min = Math.min(this.size(), o.size());
        for (int i = 0; i < min; i++) {
            int c = this.get(i).compareTo(o.get(i));
            if (c != 0) {
                return c;
            }
        }

        return this.size() - o.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PathNodeId) {
            return ListUtils.isEqualList(this, (PathNodeId<?>) obj);
        } else {
            return false;
        }
    }

    @Override
    public PathNodeId<T> clone() {
        return SerializationUtils.clone(this);
    }

    // --------------------------------------------------------------------------override list methods
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return new UnmodifiableList<>(this.subList(fromIndex, toIndex));
    }

    @Override
    public boolean add(T e) {
        if (this.initialized) {
            throw new UnsupportedOperationException();
        } else {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (this.initialized) {
            throw new UnsupportedOperationException();
        } else {
            return super.addAll(c);
        }
    }

    @Override
    public final Iterator<T> iterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public final ListIterator<T> listIterator(int index) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        return new UnmodifiableListIterator(index);
    }

    // --------------------------------------------------------------------------inner class
    private class UnmodifiableListIterator implements ListIterator<T> {
        int cursor;

        UnmodifiableListIterator(int cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return size() > 0 && cursor < size();
        }

        @Override
        public T next() {
            return get(cursor++);
        }

        @Override
        public boolean hasPrevious() {
            return size() > 0 && cursor > 0;
        }

        @Override
        public T previous() {
            return get(--cursor);
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T e) {
            throw new UnsupportedOperationException();
        }
    }

    // --------------------------------------------------------------------------unsupported operation
    @Override @Deprecated
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException();
    }

}
