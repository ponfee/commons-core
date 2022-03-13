package code.ponfee.commons.util;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 变量持有，用于lambda方法体内
 * <p>non-thread-safe
 *
 * @param <T> the type T
 * @author Ponfee
 */
public final class Holder<T> {

    private T value;

    private Holder(T value) {
        this.value = value;
    }

    public static <T> Holder<T> empty() {
        return new Holder<>(null);
    }

    public static <T> Holder<T> of(T value) {
        return new Holder<>(value);
    }

    /**
     * Returns the holder value whether null
     *
     * @return a boolean, if {@code true} then the value is null
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * Sets a newly value and return former value
     *
     * @param value the newly value
     * @return then former value
     */
    public T set(T value) {
        T former = this.value;
        this.value = value;
        return former;
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

    public T setIfMatches(T value, Predicate<T> predicate) {
        T former = this.value;
        if (predicate.test(this.value)) {
            this.value = value;
        }
        return former;
    }

    public T setIfMatches(T value, BiPredicate<T, T> predicate) {
        T former = this.value;
        if (predicate.test(this.value, value)) {
            this.value = value;
        }
        return former;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    public <U> Holder<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return isEmpty() ? empty() : of(mapper.apply(value));
    }

    public Holder<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return (isEmpty() || predicate.test(value)) ? this : empty();
    }

    public T get() {
        return value;
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    public T orElseGet(Supplier<T> other) {
        return value != null ? value : other.get();
    }

    public <E extends Throwable> T orElseThrow(Supplier<? extends E> exceptionSupplier) throws E {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return (obj instanceof Holder)
            && Objects.equals(value, ((Holder<?>) obj).value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null ? String.format("Holder(%s)", value) : "Holder.empty";
    }
}
