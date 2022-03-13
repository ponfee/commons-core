package code.ponfee.commons.collect;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 遍历集合，选取符合条件的元素，用于增强for循环场景
 * <pre>{@code
 *  for (String s : FilterableIterator.of("", null, "a")) {
 *    System.out.println(s);
 *  }
 * }</pre>
 *
 * @param <T> Parameterized Type
 * @author Ponfee
 */
public class FilterableIterator<T> implements Iterable<T>, Iterator<T> {

    private final Predicate<T> predicate;
    private final Iterator<T> iterator;
    private T current;

    private FilterableIterator(Iterator<T> iterator) {
        this(Objects::nonNull, iterator);
    }

    private FilterableIterator(Predicate<T> predicate, Iterator<T> iterator) {
        this.predicate = predicate;
        this.iterator = iterator;
    }

    public static <T> FilterableIterator<T> of(Iterator<T> iterator) {
        return new FilterableIterator<>(iterator);
    }

    public static <T> FilterableIterator<T> of(Predicate<T> predicate, Iterator<T> iterator) {
        return new FilterableIterator<>(predicate, iterator);
    }

    public static <T> FilterableIterator<T> of(Iterable<T> iterable) {
        return new FilterableIterator<>(iterable.iterator());
    }

    public static <T> FilterableIterator<T> of(Predicate<T> predicate, Iterable<T> iterable) {
        return new FilterableIterator<>(predicate, iterable.iterator());
    }

    @SafeVarargs
    public static <T> FilterableIterator<T> of(T... array) {
        return new FilterableIterator<>(new ArrayIterator<>(array));
    }

    @SafeVarargs
    public static <T> FilterableIterator<T> of(Predicate<T> predicate, T... array) {
        return new FilterableIterator<>(predicate, new ArrayIterator<>(array));
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        while (iterator.hasNext()) {
            if (predicate.test(current = iterator.next())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        return current;
    }

    private static class ArrayIterator<T> implements Iterator<T> {
        private final T[] array;
        private int cursor = 0;

        @SafeVarargs
        private ArrayIterator(T... array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return array != null && cursor != array.length;
        }

        @Override
        public T next() {
            return array[cursor++];
        }
    }

}
