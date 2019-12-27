package code.ponfee.commons.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 变量持有，用于lambda方法体内
 * non-thread-safe
 * 
 * @author Ponfee
 * @param <T>
 */
public final class Holder<T> {

    private T value;

    private Holder(T value) {
        this.value = value;
    }

    public static <T> Holder<T> empty() {
        return new Holder<>(null);
    }

    public static <T> Holder<T> of(T t) {
        return new Holder<>(t);
    }

    /**
     * Returns the holder value whether null
     * 
     * @return a boolean, if {@code true} then the value is null
     */
    public boolean isEmpty() {
        return this.value == null;
    }

    /**
     * Sets a new value if former value is null
     * 
     * @param value the new value
     */
    public void setIfAbsent(T value) {
        if (this.value == null) {
            this.value = value;
        }
    }

    /**
     * Replaces value if former value is not null, 
     * and return former value
     * 
     * @param value the newly value
     * @return then former value
     */
    public T setIfPresent(T value) {
        T former = this.value;
        if (this.value != null) {
            this.value = value;
        }
        return former;
    }

    /**
     * Sets a newly value and return former value
     * 
     * @param value the newly value
     * @return  then former value
     */
    public T getAndSet(T value) {
        T former = this.value;
        this.value = value;
        return former;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (this.value != null) {
            consumer.accept(this.value);
        }
    }

    public T get() {
        return this.value;
    }

    public T orElse(T other) {
        return this.value != null ? this.value : other;
    }

    public T orElseGet(Supplier<T> other) {
        return this.value != null ? this.value : other.get();
    }

    public <E extends Throwable> T orElseThrow(
        Supplier<? extends E> exceptionSupplier) throws E {
        if (this.value != null) {
            return this.value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Holder)) {
            return false;
        }

        return Objects.equals(this.value, ((Holder<?>) obj).value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }

    @Override
    public String toString() {
        return this.value != null
            ? String.format("Holder[%s]", this.value)
            : "Holder.empty";
    }
}
