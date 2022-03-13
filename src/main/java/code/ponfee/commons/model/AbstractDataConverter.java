package code.ponfee.commons.model;

import code.ponfee.commons.reflect.BeanCopiers;
import code.ponfee.commons.reflect.BeanMaps;
import org.springframework.cglib.beans.BeanCopier;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static code.ponfee.commons.reflect.GenericUtils.getActualTypeArgument;
import static code.ponfee.commons.util.ObjectUtils.isNotBeanType;
import static code.ponfee.commons.util.ObjectUtils.newInstance;

/**
 * Converts model object to the data transfer object
 * 
 * @param <S> source
 * @param <T> target
 * 
 * @author Ponfee
 */
public abstract class AbstractDataConverter<S, T> implements Function<S, T> {

    private final Class<T> targetType;
    private final BeanCopier copier;

    public AbstractDataConverter() {
        this.copier = createBeanCopier(
            getActualTypeArgument(getClass(), 0), 
            this.targetType = getActualTypeArgument(getClass(), 1)
        );
    }

    /**
     * Returns an target object copy source the argument object<p>
     * 
     * Sub class can override this method<p>
     * 
     * @param source the object
     * @return a target object
     */
    public T convert(S source) {
        if (source == null) {
            return null;
        }
        return convert(source, this.targetType, this.copier);
    }

    // -------------------------------------------------------------final methods
    public final void copyProperties(S source, T target) {
        copy(source, target, this.copier);
    }

    public final List<T> convert(List<S> list) {
        if (list == null) {
            return null;
        }

        return list.stream().map(this).collect(Collectors.toList());
    }

    public final Page<T> convert(Page<S> page) {
        if (page == null) {
            return null;
        }

        return page.map(this);
    }

    public final Result<T> convertResultBean(Result<S> result) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData()));
    }

    public final Result<List<T>> convertResultList(Result<List<S>> result) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData()));
    }

    public final Result<Page<T>> convertResultPage(Result<Page<S>> result) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData()));
    }

    // ----------------------------------------------other methods
    @Override
    public final T apply(S source) {
        return this.convert(source);
    }

    // -----------------------------------------------static methods
    public static <S, T> T convert(S source, Class<T> targetType) {
        return convert(source, targetType, null);
    }

    @SuppressWarnings({ "unchecked" })
    public static <S, T> T convert(S source, Class<T> targetType, BeanCopier copier) {
        if (source == null || targetType.isInstance(source)) {
            return (T) source;
        }

        // convert
        if (Map.class.isAssignableFrom(targetType)) {
            return (T) (source instanceof Map ? source : BeanMaps.CGLIB.toMap(source));
        } else if (source instanceof Map) {
            return BeanMaps.CGLIB.toBean((Map<String, Object>) source, targetType);
        } else {
            T target = newInstance(targetType);
            if (copier != null) {
                copier.copy(source, target, null);
            } else {
                BeanCopiers.copy(source, target);
            }
            return target;
        }
    }

    public static <S, T> void copy(S source, T target) {
        copy(source, target, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <S, T> void copy(S source, T target, BeanCopier copier) {
        if (source == null || target == null) {
            return;
        }

        // convert the source object from source type to target type
        if (target instanceof Map) {
            if (source instanceof Map) {
                ((Map) target).putAll((Map<?, ?>) source);
            } else {
                ((Map) target).putAll(BeanMaps.CGLIB.toMap(source));
            }
        } else if (source instanceof Map) {
            BeanMaps.CGLIB.copyFromMap((Map) source, target);
        } else if (copier != null) {
            copier.copy(source, target, null);
        } else {
            BeanCopiers.copy(source, target);
        }
    }

    public static <S, T> T convert(S source, Function<S, T> converter) {
        if (source == null) {
            return null;
        }
        return converter.apply(source);
    }

    public static <S, T> List<T> convert(List<S> list, Function<S, T> converter) {
        if (list == null) {
            return null;
        }
        return list.stream().map(converter).collect(Collectors.toList());
    }

    public static <S, T> Page<T> convert(Page<S> page, Function<S, T> converter) {
        if (page == null) {
            return null;
        }
        return page.map(converter);
    }

    public static <S, T> Result<T> convertResultBean(Result<S> result, Function<S, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(converter.apply(result.getData()));
    }

    public static <S, T> Result<List<T>> convertResultList(Result<List<S>> result, Function<S, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData(), converter));
    }

    public static <S, T> Result<Page<T>> convertResultPage(Result<Page<S>> result, Function<S, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData(), converter));
    }

    // -----------------------------------------------------------------------------------private methods
    private static BeanCopier createBeanCopier(Class<?> sourceType, Class<?> targetType) {
        if (isNotBeanType(sourceType) || isNotBeanType(targetType)) {
            return null;
        }

        try {
            return BeanCopiers.get(sourceType, targetType);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Create BeanCopier occur error.", e);
        }
    }

}
