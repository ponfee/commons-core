package code.ponfee.commons.ws.adapter;

import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.ws.adapter.model.MapEntry;
import com.google.common.collect.Maps;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

/**
 * Map<K,V>转换器
 * @param <K>
 * @param <V>
 * 
 * @author Ponfee
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class MapAdapter<K, V> extends XmlAdapter<MapEntry[], Map<K, V>> {

    protected final Class<K> ktype;
    protected final Class<V> vtype;

    protected MapAdapter() {
        ktype = GenericUtils.getActualTypeArgument(this.getClass(), 0);
        vtype = GenericUtils.getActualTypeArgument(this.getClass(), 1);
    }

    @Override
    public MapEntry<K, V>[] marshal(Map<K, V> map) {
        if (map == null) {
            return null;
        }

        MapEntry<K, V>[] entries = new MapEntry[map.size()];
        int i = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            entries[i++] = new MapEntry<>(entry);
        }
        return entries;
    }

    @Override
    public Map<K, V> unmarshal(MapEntry[] entries) {
        if (entries == null) {
            return null;
        }

        Map<K, V> map = Maps.newLinkedHashMap();
        for (MapEntry<K, V> e : entries) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }

}
