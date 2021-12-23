package code.ponfee.commons.util;

import java.util.function.Supplier;

/**
 * Lazy loader
 *
 * @author Ponfee
 */
public class LazyLoader<T> {

    private final Supplier<T> provider;
    private Holder<T> holder;

    public LazyLoader(Supplier<T> provider) {
        this.provider = provider;
    }

    public T get() {
        if (holder == null) {
            holder = Holder.of(provider.get());
        }
        return holder.get();
    }
}
