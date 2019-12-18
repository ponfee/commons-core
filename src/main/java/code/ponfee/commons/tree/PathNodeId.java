package code.ponfee.commons.tree;

import java.io.Serializable;
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

import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections4.CollectionUtils;
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
    private boolean initialized = false;

    public PathNodeId() {} // for json deserialize

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
            return CollectionUtils.isEqualCollection(this, (PathNodeId<?>) obj);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public PathNodeId<T> clone() {
        return (PathNodeId<T>) SerializationUtils.clone(this);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    // --------------------------------------------------------------------------override list methods
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return new UnmodifiableList<>(this.subList(fromIndex, toIndex));
    }

    @Override
    public boolean add(T e) {
        if (initialized) {
            throw new UnsupportedOperationException();
        } else {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (initialized) {
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
        return new ListIterator<T>() {
            private final ListIterator<? extends T> i = listIterator(index);

            public boolean hasNext()     {return i.hasNext();}
            public T next()              {return i.next();}
            public boolean hasPrevious() {return i.hasPrevious();}
            public T previous()          {return i.previous();}
            public int nextIndex()       {return i.nextIndex();}
            public int previousIndex()   {return i.previousIndex();}

            public void remove() {
                throw new UnsupportedOperationException();
            }
            public void set(T e) {
                throw new UnsupportedOperationException();
            }
            public void add(T e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                i.forEachRemaining(action);
            }
        };
    }

    // --------------------------------------------------------------------------unsupported operation
    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
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

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException();
    }

}
