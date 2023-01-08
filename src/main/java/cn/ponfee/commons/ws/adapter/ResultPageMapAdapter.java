/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.ws.adapter;

import cn.ponfee.commons.model.Page;
import cn.ponfee.commons.model.Result;
import cn.ponfee.commons.reflect.GenericUtils;
import cn.ponfee.commons.ws.adapter.model.MapEntry;
import cn.ponfee.commons.ws.adapter.model.MapItem;
import org.apache.commons.collections4.CollectionUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result<Page<Map<K, V>>>转换器
 * @param <K>
 * @param <V>
 * 
 * @see org.springframework.data.domain.jaxb.PageAdapter
 * 
 * @author Ponfee
 */
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
        if (v.getData() == null || CollectionUtils.isEmpty(v.getData().getRows())) {
            return (Result<Page<Map<K, V>>>) ((Result<?>) v);
        }

        return v.from(v.getData().map(items -> {
            if (items == null) {
                return null;
            }
            return Arrays.stream(items.getItem())
                         .collect(Collectors.toMap(MapEntry<K, V>::getKey, MapEntry<K, V>::getValue));
        }));
    }

    @Override
    public Result<Page<MapItem>> marshal(Result<Page<Map<K, V>>> v) {
        if (v.getData() == null || CollectionUtils.isEmpty(v.getData().getRows())) {
            return (Result<Page<MapItem>>) ((Result<?>) v);
        }

        return v.from(v.getData().map(e -> {
            if (e == null) {
                return null;
            }
            return new MapItem(e.entrySet().stream().map(MapEntry::new).toArray(MapEntry[]::new));
        }));
    }

}
