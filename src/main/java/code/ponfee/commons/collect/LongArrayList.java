package code.ponfee.commons.collect;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The primitive long array to list
 * 
 * @author Ponfee
 */
public class LongArrayList extends AbstractArrayList<Long> {

    private static final long serialVersionUID = 1648346699558392015L;

    private final long[] array;

    public LongArrayList(long... array) {
        this(array, 0, array.length);
    }

    public LongArrayList(long[] array, int start, int end) {
        super(start, end);
        this.array = Objects.requireNonNull(array);
    }

    @Override
    public Long get(int index) {
        checkElementIndex(index, size);
        return array[start + index];
    }

    @Override
    public int indexOf(Object target) {
        return (target instanceof Long) ? indexOf((long) target) : INDEX_NOT_FOUND;
    }

    @Override
    public int lastIndexOf(Object target) {
        return (target instanceof Long) ? lastIndexOf((long) target) : INDEX_NOT_FOUND;
    }

    @Override
    public Long set(int index, Long element) {
        checkElementIndex(index, size);
        long oldValue = array[start + index];
        array[start + index] = Objects.requireNonNull(element);
        return oldValue;
    }

    @Override
    public List<Long> subList(int fromIndex, int toIndex) {
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
            if (this.size != that.size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (this.array[this.start + i] != that.array[that.start + i]) {
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
            result = 31 * result + (int) array[i];
        }
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }

    public long[] getArray() {
        return Arrays.copyOfRange(array, start, end);
    }

    // ----------------------------------------------------------others methods
    public int indexOf(long target) {
        for (int i = 0; i < size; i++) {
            if (array[i + start] == target) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    public int lastIndexOf(long target) {
        for (int i = size - 1; i >= 0; i--) {
            if (array[i + start] == target) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

}
