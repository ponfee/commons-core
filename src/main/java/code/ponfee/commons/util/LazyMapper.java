package code.ponfee.commons.util;

import java.util.function.Function;

/**
 * Lazy Mapper
 *
 * @author Ponfee
 */
public class LazyMapper<S, T> {

    private final Function<S, T> mapper;
    private Holder<T> holder;

    public LazyMapper(Function<S, T> mapper) {
        this.mapper = mapper;
    }

    public T get(S source) {
        if (holder == null) {
            holder = Holder.of(mapper.apply(source));
        }
        return holder.get();
    }
}
