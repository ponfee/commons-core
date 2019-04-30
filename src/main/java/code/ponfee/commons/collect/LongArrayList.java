package code.ponfee.commons.collect;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The primitive long array of list
 * @author Ponfee
 */
public class LongArrayList extends AbstractArrayList<Long> {

    private static final long serialVersionUID = -2989143269776168553L;

    private final long[] array;

    public LongArrayList(long[] array) {
        this(array, 0, array.length);
    }

    public LongArrayList(long[] array, int start, int end) {
        super(start, end);
        Objects.requireNonNull(array);
        this.array = array;
    }

    @Override
    public Long get(int index) {
        checkElementIndex(index, size());
        return array[start + index];
    }

    @Override
    public boolean contains(Object target) {
        return (target instanceof Long)
            && indexOf(array, (Long) target, start, end) != -1;
    }

    @Override
    public int indexOf(Object target) {
        if (target instanceof Long) {
            int i = indexOf(array, (Long) target, start, end);
            if (i >= 0) {
                return i - start;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object target) {
        if (target instanceof Long) {
            int i = lastIndexOf(array, (Long) target, start, end);
            if (i >= 0) {
                return i - start;
            }
        }
        return -1;
    }

    @Override
    public Long set(int index, Long element) {
        checkElementIndex(index, size());
        long oldValue = array[start + index];
        array[start + index] = Objects.requireNonNull(element);
        return oldValue;
    }

    @Override
    public List<Long> subList(int fromIndex, int toIndex) {
        int size = size();
        checkPositionIndexes(fromIndex, toIndex, size);
        if (fromIndex == toIndex) {
            return Collections.emptyList();
        }
        return new LongArrayList(array, start + fromIndex, start + toIndex);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof LongArrayList) {
            LongArrayList that = (LongArrayList) object;
            int size = size();
            if (that.size() != size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (array[start + i] != that.array[that.start + i]) {
                    return false;
                }
            }
            return true;
        }
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = start; i < end; i++) {
            result = 31 * result + Long.hashCode(array[i]);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(size() * 10);
        builder.append('[').append(array[start]);
        for (int i = start + 1; i < end; i++) {
            builder.append(",").append(array[i]);
        }
        return builder.append(']').toString();
    }

    public long[] toLongArray() {
        return Arrays.copyOfRange(array, start, end);
    }

    private static int indexOf(long[] array, long target, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    private static int lastIndexOf(long[] array, long target, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }
}
