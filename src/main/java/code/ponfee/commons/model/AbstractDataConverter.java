package code.ponfee.commons.model;

import static code.ponfee.commons.reflect.GenericUtils.getActualTypeArgument;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cglib.beans.BeanCopier;

import code.ponfee.commons.reflect.BeanMaps;
import code.ponfee.commons.reflect.CglibUtils;
import code.ponfee.commons.util.ObjectUtils;

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

    @SuppressWarnings("unchecked")
    public AbstractDataConverter() {
        targetType = (Class<T>) getActualTypeArgument(getClass(), 1);
        copier = BeanCopier.create(
            getActualTypeArgument(getClass(), 0), targetType, false
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
        return convert(source, targetType, copier);
    }

    // -------------------------------------------------------------final methods
    public final void copyProperties(S source, T target) {
        copy(source, target, copier);
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

        return page.transform(this);
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

        //target = targetType.getConstructor().newInstance();
        T target = ObjectUtils.newInstance(targetType);

        copy(source, target, copier);
        return target;
    }

    public static <S, T> void copy(S source, T target) {
        copy(source, target, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <S, T> void copy(S source, T target, BeanCopier copier) {
        if (source == null || target == null) {
            return;
        }

        // convert
        if (target instanceof Map && source instanceof Map) {
            ((Map) target).putAll((Map<?, ?>) source);
        } else if (target instanceof Map) {
            ((Map) target).putAll(BeanMaps.CGLIB.toMap(source));
        } else if (source instanceof Map) {
            BeanMaps.CGLIB.copyFromMap((Map) source, target);
        } else {
            if (copier != null) {
                copier.copy(source, target, null);
            } else {
                CglibUtils.copyProperties(source, target);
            }
            //org.apache.commons.beanutils.BeanUtils.copyProperties(target, source);
            //org.apache.commons.beanutils.PropertyUtils.copyProperties(target, source);
            //org.springframework.beans.BeanUtils.copyProperties(source, target);
            //org.springframework.cglib.beans.BeanCopier.create(sourceType, targetType, false);
        }
    }

    public static <S, T> T convert(S source, Function<S, T> converter) {
        if (source == null) {
            return null;
        }
        return converter.apply(source);
    }

    public static <S, T> List<T> convert(
        List<S> list, Function<S, T> converter) {
        if (list == null) {
            return null;
        }
        return list.stream().map(converter).collect(Collectors.toList());
    }

    public static <S, T> Page<T> convert(
        Page<S> page, Function<S, T> converter) {
        if (page == null) {
            return null;
        }
        return page.transform(converter);
    }

    public static <S, T> Result<T> convertResultBean(
        Result<S> result, Function<S, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(converter.apply(result.getData()));
    }

    public static <S, T> Result<List<T>> convertResultList(
        Result<List<S>> result, Function<S, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData(), converter));
    }

    public static <S, T> Result<Page<T>> convertResultPage(
        Result<Page<S>> result, Function<S, T> converter) {
        if (result == null) {
            return null;
        }
        return result.copy(convert(result.getData(), converter));
    }

}
