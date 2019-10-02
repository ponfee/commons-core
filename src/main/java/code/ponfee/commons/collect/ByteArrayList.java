package code.ponfee.commons.collect;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The primitive byte array to list
 * 
 * Error  : Arrays.asList(new byte[] {4,5,6,7})
 * Correct: new ByteArrayList(new byte[] {4,5,6,7})
 * 
 * IntStream.of(new int[] { 1, 2, 3, 4 }).boxed().collect(Collectors.toList())
 * 
 * @author Ponfee
 */
public class ByteArrayList extends AbstractArrayList<Byte> {

    private static final long serialVersionUID = 8638428453599555032L;

    private final byte[] array;

    public ByteArrayList(byte... array) {
        this(array, 0, array.length);
    }

    public ByteArrayList(byte[] array, int start, int end) {
        super(start, end);
        Objects.requireNonNull(array);
        this.array = array;
    }

    @Override
    public Byte get(int index) {
        checkElementIndex(index, size);
        return array[start + index];
    }

    @Override
    public int indexOf(Object target) {
        return (target instanceof Byte) ? indexOf((byte) target) : INDEX_NOT_FOUND;
    }

    @Override
    public int lastIndexOf(Object target) {
        return (target instanceof Byte) ? lastIndexOf((byte) target) : INDEX_NOT_FOUND;
    }

    @Override
    public Byte set(int index, Byte element) {
        checkElementIndex(index, size);
        byte oldValue = array[start + index];
        array[start + index] = Objects.requireNonNull(element);
        return oldValue;
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        checkPositionIndexes(fromIndex, toIndex, size);
        if (fromIndex == toIndex) {
            return Collections.emptyList();
        }
        return new ByteArrayList(array, start + fromIndex, start + toIndex);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ByteArrayList) {
            ByteArrayList that = (ByteArrayList) object;
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
        StringBuilder builder = new StringBuilder(size << 2);
        builder.append('[').append(array[start]);
        for (int i = start + 1; i < end; i++) {
            builder.append(",").append(array[i]);
        }
        return builder.append(']').toString();
    }

    public byte[] getArray() {
        return Arrays.copyOfRange(array, start, end);
    }

    // ----------------------------------------------------------others methods
    public int indexOf(byte target) {
        for (int i = 0; i < size; i++) {
            if (array[i + start] == target) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    public int lastIndexOf(byte target) {
        for (int i = size - 1; i >= 0; i--) {
            if (array[i + start] == target) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

}
