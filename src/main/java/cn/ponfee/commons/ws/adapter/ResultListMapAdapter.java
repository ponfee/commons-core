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
import cn.ponfee.commons.ws.adapter.model.MapItemArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Result<List<Map<K,V>>转换器
 * @param <K>
 * @param <V>
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public abstract class ResultListMapAdapter<K, V> extends XmlAdapter<Result<MapItemArray>, Result<List<Map<K, V>>>> {

    protected final Class<K> ktype;
    protected final Class<V> vtype;

    protected ResultListMapAdapter() {
        ktype = GenericUtils.getActualTypeArgument(this.getClass(), 0);
        vtype = GenericUtils.getActualTypeArgument(this.getClass(), 1);
    }

    @Override
    public Result<List<Map<K, V>>> unmarshal(Result<MapItemArray> v) {
        if (v.getData() == null) {
            return v.from(null);
        } else if (v.getData().getItems() == null) {
            return v.from(Lists.newArrayList());
        }

        List<Map<K, V>> list = new ArrayList<>();
        for (MapItem items : v.getData().getItems()) {
            if (items == null) {
                continue;
            }
            Map<K, V> map = Maps.newLinkedHashMap();
            for (MapEntry<K, V> item : items.getItem()) {
                if (item == null) {
                    continue;
                }
                map.put(item.getKey(), item.getValue());
            }
            list.add(map);
        }
        return v.from(list);
    }

    @Override
    public Result<MapItemArray> marshal(Result<List<Map<K, V>>> v) {
        if (v.getData() == null) {
            return v.from(null);
        }

        MapItem[] items = new MapItem[v.getData().size()];
        int i = 0;
        for (Map<K, V> map : v.getData()) {
            if (map == null) {
                continue;
            }
            MapEntry<K, V>[] item = new MapEntry[map.size()];
            int j = 0;
            for (Entry<K, V> entry : map.entrySet()) {
                item[j++] = new MapEntry<>(entry);
            }
            items[i++] = new MapItem(item);
        }
        return v.from(new MapItemArray(items));
    }

}
