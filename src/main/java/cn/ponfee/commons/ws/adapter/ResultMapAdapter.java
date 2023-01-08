/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.ws.adapter;

import cn.ponfee.commons.model.Result;
import cn.ponfee.commons.reflect.GenericUtils;
import cn.ponfee.commons.ws.adapter.model.MapEntry;
import cn.ponfee.commons.ws.adapter.model.MapItem;
import com.google.common.collect.Maps;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

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

    @Override
    public Result<Map<K, V>> unmarshal(Result<MapItem> v) {
        if (v.getData() == null || v.getData().getItem() == null) {
            return v.from(null);
        }

        Map<K, V> map = Maps.newLinkedHashMap();
        for (MapEntry<K, V> e : v.getData().getItem()) {
            map.put(e.getKey(), e.getValue());
        }
        return v.from(map);
    }

    @Override
    public Result<MapItem> marshal(Result<Map<K, V>> v) {
        if (v.getData() == null) {
            return v.from(null);
        }

        MapEntry<K, V>[] entries = new MapEntry[v.getData().size()];
        int i = 0;
        for (Map.Entry<K, V> entry : v.getData().entrySet()) {
            entries[i++] = new MapEntry<>(entry);
        }
        return v.from(new MapItem(entries));
    }

}
