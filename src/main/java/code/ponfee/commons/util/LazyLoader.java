package code.ponfee.commons.util;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Lazy loader
 *
 * @author Ponfee
 */
public class LazyLoader<T> implements Supplier<T> {

    private final Supplier<T> loader;
    private Holder<T> holder;

    private LazyLoader(Supplier<T> loader) {
        this.loader = loader;
    }

    public static <T> LazyLoader<T> of(Supplier<T> loader) {
        return new LazyLoader<>(loader);
    }

    public static <T, A> LazyLoader<T> of(Function<A, T> loader, A arg) {
        return new LazyLoader<>(() -> loader.apply(arg));
    }

    @Override
    public T get() {
        if (holder == null) {
            holder = Holder.of(loader.get());
        }
        return holder.get();
    }

    public static <K, V> V get(K key, Map<K, V> cache, Function<K, V> mapper) {
        V val = cache.get(key);
        if (val != null) {
            return val;
        }
        synchronized (cache) {
            if ((val = cache.get(key)) == null) {
                if ((val = mapper.apply(key)) != null) {
                    cache.put(key, val);
                }
            }
        }
        return val;
    }

    public static <K, V> V get(K key, Map<K, V> cache, Supplier<V> supplier) {
        V val = cache.get(key);
        if (val != null) {
            return val;
        }
        synchronized (cache) {
            if ((val = cache.get(key)) == null) {
                if ((val = supplier.get()) != null) {
                    cache.put(key, val);
                }
            }
        }
        return val;
    }

}
