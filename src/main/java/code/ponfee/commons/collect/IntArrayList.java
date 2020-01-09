package code.ponfee.commons.collect;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The primitive int array to list
 * 
 * @author Ponfee
 */
public class IntArrayList extends AbstractArrayList<Integer> {

    private static final long serialVersionUID = -1601389928083241185L;

    private final int[] array;

    public IntArrayList(int... array) {
        this(array, 0, array.length);
    }

    public IntArrayList(int[] array, int start, int end) {
        super(start, end);
        this.array = Objects.requireNonNull(array);
    }

    @Override
    public Integer get(int index) {
        checkElementIndex(index, size);
        return array[start + index];
    }

    @Override
    public int indexOf(Object target) {
        return (target instanceof Integer) ? indexOf((int) target) : INDEX_NOT_FOUND;
    }

    @Override
    public int lastIndexOf(Object target) {
        return (target instanceof Integer) ? lastIndexOf((int) target) : INDEX_NOT_FOUND;
    }

    @Override
    public Integer set(int index, Integer element) {
        checkElementIndex(index, size);
        int oldValue = array[start + index];
        array[start + index] = Objects.requireNonNull(element);
        return oldValue;
    }

    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
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
            result = 31 * result + array[i];
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(size << 2);
        builder.append('[').append(array[start]);
        for (int i = start + 1; i < end; i++) {
            builder.append(",").append(array[i]);
        }
        return builder.append(']').toString();
    }

    public int[] getArray() {
        return Arrays.copyOfRange(array, start, end);
    }

    // ----------------------------------------------------------others methods
    public int indexOf(int target) {
        for (int i = 0; i < size; i++) {
            if (array[i + start] == target) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    public int lastIndexOf(int target) {
        for (int i = size - 1; i >= 0; i--) {
            if (array[i + start] == target) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

}
