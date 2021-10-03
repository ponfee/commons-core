package code.ponfee.commons.collect;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 遍历数组时跳过指定的元素，用于增强for循环场景
 * <pre>{@code
 *  for (String s : new FilteredIterable("", null, "a") {
 *    System.out.println(s);
 *  }
 * }</pre>
 *
 * @param <T> Parameterized Type
 * @author Ponfee
 */
public class FilteredIterable<T> implements Iterable<T> {

    private final Predicate<T> predicate;
    private final T[] array;

    @SuppressWarnings("unchecked")
    public FilteredIterable(T... array) {
        this(Objects::isNull, array);
    }

    @SafeVarargs
    public FilteredIterable(Predicate<T> predicate, T... array) {
        this.predicate = predicate;
        this.array = array;
    }

    @Override
    public Iterator<T> iterator() {
        return new SkipNullIterator<>();
    }

    @SuppressWarnings("hiding")
    private class SkipNullIterator<T> implements Iterator<T> {
        private int cur = 0;

        @Override
        public boolean hasNext() {
            if (array == null) {
                return false;
            }
            for (; cur < array.length && predicate.test(array[cur]); cur++) {
                // do noting
            }
            return cur != array.length;
        }

        @Override @SuppressWarnings("unchecked")
        public T next() {
            return (T) array[cur++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
