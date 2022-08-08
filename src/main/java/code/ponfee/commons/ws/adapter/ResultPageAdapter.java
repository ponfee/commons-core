package code.ponfee.commons.ws.adapter;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.TransitPage;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Result<Page<T>>转换器
 * @param <T>
 * 
 * @see org.springframework.data.domain.jaxb.PageAdapter
 * 
 * @author Ponfee
 */
public abstract class ResultPageAdapter<T> extends XmlAdapter<Result<TransitPage<T>>, Result<Page<T>>> {

    protected final Class<T> type;

    protected ResultPageAdapter() {
        type = GenericUtils.getActualTypeArgument(this.getClass());
    }

    @Override
    public Result<Page<T>> unmarshal(Result<TransitPage<T>> v) {
        if (v.getData() == null) {
            return v.from(null);
        } else if (   v.getData().getRows() == null 
                   || v.getData().getRows().getItem() == null) {
            return v.from(new Page<>());
        }

        return v.from(TransitPage.recover(v.getData()));
    }

    @Override
    public Result<TransitPage<T>> marshal(Result<Page<T>> v) {
        if (v.getData() == null) {
            return v.from(null);
        } else if (v.getData().getRows() == null) {
            return v.from(new TransitPage<>());
        }

        return v.from(TransitPage.transform(v.getData(), type));
    }

}
