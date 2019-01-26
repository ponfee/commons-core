package code.ponfee.commons.ws.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;

/**
 * Result<data> -> Result<json>
 * @param <T>
 * 
 * @author fupf
 */
public abstract class ResultDataJsonAdapter<T> extends XmlAdapter<Result<String>, Result<T>> {

    protected final Class<T> type;

    protected ResultDataJsonAdapter() {
        type = GenericUtils.getActualTypeArgument(this.getClass());
    }

    @Override
    public Result<T> unmarshal(Result<String> v) {
        if (StringUtils.isEmpty(v.getData())) {
            return v.copy(null);
        }

        return v.copy(Jsons.fromJson(v.getData(), type));
    }

    @Override
    public Result<String> marshal(Result<T> v) {
        if (v.getData() == null) {
            return v.copy(null);
        }

        return v.copy(Jsons.toJson(v.getData()));
    }

}
