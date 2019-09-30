package code.ponfee.commons.ws.adapter;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Maps;

import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.MapEntry;
import code.ponfee.commons.ws.adapter.model.MapItem;

/**
 * Result<Map<K,V>>转换器
 * @param <K>
 * @param <V>
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public abstract class ResultMapAdapter<K, V> extends XmlAdapter<Result<MapItem>, Result<Map<K, V>>> {

    protected final Class<K> ktype;
    protected final Class<V> vtype;

    protected ResultMapAdapter() {
        ktype = GenericUtils.getActualTypeArgument(this.getClass(), 0);
        vtype = GenericUtils.getActualTypeArgument(this.getClass(), 1);
    }

    public @Override Result<Map<K, V>> unmarshal(Result<MapItem> v) {
        if (v.getData() == null || v.getData().getItem() == null) {
            return v.copy(null);
        }

        Map<K, V> map = Maps.newLinkedHashMap();
        for (MapEntry<K, V> e : v.getData().getItem()) {
            map.put(e.getKey(), e.getValue());
        }
        return v.copy(map);
    }

    public @Override Result<MapItem> marshal(Result<Map<K, V>> v) {
        if (v.getData() == null) {
            return v.copy(null);
        }

        MapEntry<K, V>[] entries = new MapEntry[v.getData().size()];
        int i = 0;
        for (Map.Entry<K, V> entry : v.getData().entrySet()) {
            entries[i++] = new MapEntry<>(entry);
        }
        return v.copy(new MapItem(entries));
    }

}
