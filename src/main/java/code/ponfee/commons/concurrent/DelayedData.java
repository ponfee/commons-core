package code.ponfee.commons.concurrent;

import com.google.common.primitives.Ints;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Delayed data
 *
 * @author Ponfee
 */
public class DelayedData<E> implements Delayed {

    private final long fireTime;
    private final E data;

    public DelayedData(E data, long delayInMilliseconds) {
        this.data = Objects.requireNonNull(data);
        this.fireTime = System.currentTimeMillis() + delayInMilliseconds;
    }

    public static <E> DelayedData<E> of(E data, long delayInMilliseconds) {
        return new DelayedData<>(data, delayInMilliseconds);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = fireTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Ints.saturatedCast(this.fireTime - ((DelayedData<E>) o).fireTime);
    }

    public E getData() {
        return data;
    }

}
