package code.ponfee.commons.model;

import static code.ponfee.commons.reflect.GenericUtils.getActualTypeArgument;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.cglib.beans.BeanCopier;

import code.ponfee.commons.reflect.CglibUtils;

/**
 * Converts model to the data transfer object
 * 
 * @param <F> from(source)
 * @param <T> to  (target)
 * 
 * @author Ponfee
 */
public abstract class AbstractDataConverter<F, T> implements Function<F, T> {

    private final Class<T> toType;
    private volatile BeanCopier copier;

    @SuppressWarnings("unchecked")
    public AbstractDataConverter() {
        toType = (Class<T>) getActualTypeArgument(this.getClass(), 1);
    }

    public T convert(F from) {
        if (from == null) {
            return null;
        }
        return convert(from, toType, this::getCopier);
    }

    public void copyProperties(F from, T to) {
        copy(from, to, this::getCopier);
    }

    public final List<T> convert(List<F> list) {
        if (list == null) {
            return null;
        }

        return list.stream().map(this).collect(Collectors.toList());
    }

    public final Page<T> convert(Page<F> page) {
        if (page == null) {
            return null;
        }

        return page.transform(this);
    }

    public final Result<T> convertResultBean(Result<F> result) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData()));
    }

    public final Result<List<T>> convertResultList(Result<List<F>> result) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData()));
    }

    public final Result<Page<T>> convertResultPage(Result<Page<F>> result) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData()));
    }

    // ----------------------------------------------other methods
    public @Override final T apply(F from) {
        return this.convert(from);
    }

    private BeanCopier getCopier() {
        if (copier == null) {
            synchronized (this) {
                if (copier == null) {
                    copier = BeanCopier.create(getActualTypeArgument(this.getClass(), 0), toType, false);
                }
            }
        }
        return copier;
    }

    // -----------------------------------------------static methods
    public static <T, F> T convert(F from, Class<T> toType) {
        return convert(from, toType, null);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T, F> T convert(F from, Class<T> toType, Supplier<BeanCopier> supplier) {
        if (from == null || toType.isInstance(from)) {
            return (T) from;
        }

        // new instance
        T to;
        try {
            to = toType.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        copy(from, to, supplier);
        return to;
    }

    public static <T, F> void copy(F from, T to) {
        copy(from, to, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, F> void copy(F from, T to, Supplier<BeanCopier> supplier) {
        if (from == null || to == null) {
            return;
        }

        // convert
        if (to instanceof Map && from instanceof Map) {
            ((Map) to).putAll((Map<?, ?>) from);
        } else if (to instanceof Map) {
            //((Map) to).putAll(ObjectUtils.bean2map(from));
            ((Map) to).putAll(CglibUtils.bean2map(from));
        } else if (from instanceof Map) {
            //ObjectUtils.map2bean((Map) from, to);
            CglibUtils.map2bean((Map) from, to);
        } else {
            BeanCopier copier = (supplier != null) ? supplier.get() : null;
            if (copier != null) {
                copier.copy(from, to, null);
            } else {
                CglibUtils.copyProperties(from, to);
            }
            //org.apache.commons.beanutils.BeanUtils.copyProperties(to, from);
            //org.apache.commons.beanutils.PropertyUtils.copyProperties(to, from);
            //org.springframework.beans.BeanUtils.copyProperties(from, to);
            //org.springframework.cglib.beans.BeanCopier.create(source, target, false);
        }
    }

    public static <F, T> T convert(F from, Function<F, T> converter) {
        if (from == null) {
            return null;
        }
        return converter.apply(from);
    }

    public static <F, T> List<T> convert(
        List<F> list, Function<F, T> converter) {
        if (list == null) {
            return null;
        }
        return list.stream().map(converter).collect(Collectors.toList());
    }

    public static <F, T> Page<T> convert(
        Page<F> page, Function<F, T> converter) {
        if (page == null) {
            return null;
        }
        return page.transform(converter);
    }

    public static <F, T> Result<T> convertResultBean(
        Result<F> result, Function<F, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(converter.apply(result.getData()));
    }

    public static <F, T> Result<List<T>> convertResultList(
        Result<List<F>> result, Function<F, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData(), converter));
    }

    public static <F, T> Result<Page<T>> convertResultPage(
        Result<Page<F>> result, Function<F, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData(), converter));
    }

}
