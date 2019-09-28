package code.ponfee.commons.ws.adapter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.MapEntry;
import code.ponfee.commons.ws.adapter.model.MapItem;

/**
 * Result<Page<Map<K, V>>>转换器
 * @param <K>
 * @param <V>
 * 
 * @see org.springframework.data.domain.jaxb.PageAdapter
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public abstract class ResultPageMapAdapter<K, V> 
    extends XmlAdapter<Result<Page<MapItem>>, Result<Page<Map<K, V>>>> {

    protected final Class<K> ktype;
    protected final Class<V> vtype;

    protected ResultPageMapAdapter() {
        ktype = GenericUtils.getActualTypeArgument(this.getClass(), 0);
        vtype = GenericUtils.getActualTypeArgument(this.getClass(), 1);
    }

    @Override
    public Result<Page<Map<K, V>>> unmarshal(Result<Page<MapItem>> v) {
        if (v.getData() == null) {
            return v.copy();
        } else if (v.getData().getRows() == null) {
            return v.copy(Page.empty());
        }

        return v.copy(v.getData().transform(items -> {
            if (items == null) {
                return null;
            }
            return Arrays.stream(items.getItem()).collect(
                Collectors.toMap(MapEntry<K, V>::getKey, MapEntry<K, V>::getValue)
            );
        }));
    }

    @Override
    public Result<Page<MapItem>> marshal(Result<Page<Map<K, V>>> v) {
        if (v.getData() == null) {
            return v.copy();
        } else if (v.getData().getRows() == null) {
            return v.copy(Page.empty());
        }

        return v.copy(v.getData().transform(map -> {
            if (map == null) {
                return null;
            }
            return new MapItem(
                map.entrySet().stream().map(MapEntry::new).toArray(MapEntry[]::new)
            );
        }));
    }

}
