package code.ponfee.commons.collect;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The primitive byte array of list
 * 
 * @author Ponfee
 */
public class ByteArrayList extends AbstractArrayList<Byte> {

    private static final long serialVersionUID = 8638428453599555032L;

    private final byte[] array;

    public ByteArrayList(byte[] array) {
        this(array, 0, array.length);
    }

    public ByteArrayList(byte[] array, int start, int end) {
        super(start, end);
        checkNotNull(array);
        this.array = array;
    }

    @Override
    public Byte get(int index) {
        checkElementIndex(index, size());
        return array[start + index];
    }

    @Override
    public boolean contains(Object target) {
        return (target instanceof Byte)
            && indexOf(array, (Byte) target, start, end) != -1;
    }

    @Override
    public int indexOf(Object target) {
        if (target instanceof Byte) {
            int i = indexOf(array, (Byte) target, start, end);
            if (i >= 0) {
                return i - start;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object target) {
        if (target instanceof Byte) {
            int i = lastIndexOf(array, (Byte) target, start, end);
            if (i >= 0) {
                return i - start;
            }
        }
        return -1;
    }

    @Override
    public Byte set(int index, Byte element) {
        checkElementIndex(index, size());
        byte oldValue = array[start + index];
        array[start + index] = checkNotNull(element);
        return oldValue;
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        int size = size();
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
            result = 31 * result + (int) array[i];
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(size() << 2);
        builder.append('[').append(array[start]);
        for (int i = start + 1; i < end; i++) {
            builder.append(",").append(array[i]);
        }
        return builder.append(']').toString();
    }

    public byte[] toByteArray() {
        return Arrays.copyOfRange(array, start, end);
    }

    private static int indexOf(byte[] array, byte target, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    private static int lastIndexOf(byte[] array, byte target, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

}
