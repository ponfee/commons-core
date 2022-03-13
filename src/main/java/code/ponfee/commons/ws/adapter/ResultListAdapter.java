package code.ponfee.commons.ws.adapter;

import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.ArrayItem;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.lang.reflect.Array;
import java.util.List;

/**
 * Result<List<T>>转换器
 * @param <T>
 * 
 * @author Ponfee
 */
//ParameterizedTypeImpl cannot be cast to TypeVariable
//abstract class ResultListAdapter<T> extends XmlAdapter<Result<Item<T>[]>, Result<List<T>>> {
public abstract class ResultListAdapter<T> extends XmlAdapter<Result<ArrayItem<T>>, Result<List<T>>> {

    protected final Class<T> type;

    protected ResultListAdapter() {
        type = GenericUtils.getActualTypeArgument(this.getClass());
    }

    @Override
    public Result<List<T>> unmarshal(Result<ArrayItem<T>> v) {
        if (v.getData() == null) {
            return v.copy(null);
        } else if (v.getData().getItem() == null) {
            return v.copy(Lists.newArrayList());
        }

        List<T> list = Lists.newArrayList(v.getData().getItem());
        return v.copy(list);
    }

    @Override @SuppressWarnings("unchecked")
    public Result<ArrayItem<T>> marshal(Result<List<T>> v) {
        if (v.getData() == null) {
            return v.copy(null);
        }

        T[] array = v.getData().toArray((T[]) Array.newInstance(type, v.getData().size()));
        return v.copy(new ArrayItem<>(array));
    }

}
