package code.ponfee.commons.ws.adapter;

import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.ArrayItem;
import com.google.common.collect.Sets;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.lang.reflect.Array;
import java.util.Set;

/**
 * Result<Set<T>>转换器
 * @param <T>
 * 
 * @author Ponfee
 */
public abstract class ResultSetAdapter<T> extends XmlAdapter<Result<ArrayItem<T>>, Result<Set<T>>> {

    protected final Class<T> type;

    protected ResultSetAdapter() {
        type = GenericUtils.getActualTypeArgument(this.getClass());
    }

    @Override
    public Result<Set<T>> unmarshal(Result<ArrayItem<T>> v) {
        if (v.getData() == null) {
            return v.copy(null);
        } else if (v.getData().getItem() == null) {
            return v.copy(Sets.newHashSet());
        }

        Set<T> set = Sets.newHashSet(v.getData().getItem());
        return v.copy(set);
    }

    @Override @SuppressWarnings("unchecked")
    public Result<ArrayItem<T>> marshal(Result<Set<T>> v) {
        if (v.getData() == null) {
            return v.copy(null);
        }

        T[] array = v.getData().toArray((T[]) Array.newInstance(type, v.getData().size()));
        return v.copy(new ArrayItem<>(array));
    }

}
