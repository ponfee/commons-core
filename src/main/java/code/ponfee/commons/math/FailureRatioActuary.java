package code.ponfee.commons.math;

import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Failure ratio
 *
 * @author Ponfee
 */
public class FailureRatioActuary {

    private final BitSet bitset;
    private final int size;
    private int position = 0;

    public FailureRatioActuary(int size) {
        this.size = (size + 63) / 64 * 64;
        this.bitset = new BitSet(size);
        IntStream.range(0, size).forEach(bitset::set);
    }

    public int size() {
        //Assert.state(size == bitset.size(), "Illegal size, except: " + size + ", actual: " + bitset.size());
        return size;
    }

    public void set(boolean value) {
        bitset.set(position++, value);
        if (position == size) {
            position = 0;
        }
    }

    public void set(Boolean value) {
        bitset.set(position++, value != null && value);
        if (position == size) {
            position = 0;
        }
    }

    public <T> double ratio(T[] array, ToBooleanFunction mapper) {
        for (T val : array) {
            set(mapper.apply(val));
        }
        return ratio();
    }

    public <T> double set(List<T> array, ToBooleanFunction mapper) {
        for (T val : array) {
            set(mapper.apply(val));
        }
        return ratio();
    }

    public double ratio(boolean[] array) {
        for (boolean val : array) {
            set(val);
        }
        return ratio();
    }

    public double ratio() {
        return ((double) (size - bitset.cardinality())) / size;
    }

    @Override
    public String toString() {
        return "(" + position + ", " + bitset + ")";
    }

    @FunctionalInterface
    public interface ToBooleanFunction<T> {
        boolean apply(T value);
    }

}
