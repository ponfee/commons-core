package code.ponfee.commons.collect;

import java.util.Iterator;

/**
 * 遍历数组时跳过为null的元素，用于增强for循环场景
 *
 * @param <T>
 * @author Ponfee
 */
public class SkipNullIterable<T> implements Iterable<T> {

    private final T[] array;

    @SuppressWarnings("unchecked")
    public SkipNullIterable(T... array) {
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
            for (; cur < array.length && array[cur] == null; cur++) {
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
