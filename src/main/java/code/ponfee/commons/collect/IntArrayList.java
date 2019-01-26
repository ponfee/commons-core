package code.ponfee.commons.collect;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The primitive int array of list
 * @author Ponfee
 */
public class IntArrayList extends AbstractArrayList<Integer> {

    private static final long serialVersionUID = 6521891130670645647L;

    private final int[] array;

    public IntArrayList(int[] array) {
        this(array, 0, array.length);
    }

    /**
     * Construct a IntArrayList
     * @param array
     * @param start inclusive
     * @param end   exclusive
     */
    public IntArrayList(int[] array, int start, int end) {
        super(start, end);
        checkNotNull(array);
        this.array = array;
    }

    @Override
    public Integer get(int index) {
        checkElementIndex(index, size());
        return array[start + index];
    }

    @Override
    public boolean contains(Object target) {
        return (target instanceof Integer)
            && indexOf(array, (Integer) target, start, end) != -1;
    }

    @Override
    public int indexOf(Object target) {
        if (target instanceof Integer) {
            int i = indexOf(array, (Integer) target, start, end);
            if (i >= 0) {
                return i - start;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object target) {
        if (target instanceof Integer) {
            int i = lastIndexOf(array, (Integer) target, start, end);
            if (i >= 0) {
                return i - start;
            }
        }
        return -1;
    }

    @Override
    public Integer set(int index, Integer element) {
        checkElementIndex(index, size());
        int oldValue = array[start + index];
        array[start + index] = checkNotNull(element);
        return oldValue;
    }

    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
        int size = size();
        checkPositionIndexes(fromIndex, toIndex, size);
        if (fromIndex == toIndex) {
            return Collections.emptyList();
        }
        return new IntArrayList(array, start + fromIndex, start + toIndex);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof IntArrayList) {
            IntArrayList that = (IntArrayList) object;
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
            result = 31 * result + array[i];
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(size() * 6);
        builder.append('[').append(array[start]);
        for (int i = start + 1; i < end; i++) {
            builder.append(",").append(array[i]);
        }
        return builder.append(']').toString();
    }

    public int[] toIntArray() {
        return Arrays.copyOfRange(array, start, end);
    }

    private static int indexOf(int[] array, int target, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    private static int lastIndexOf(int[] array, int target, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }
}
