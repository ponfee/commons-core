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
import cn.ponfee.commons.ws.adapter.model.ArrayItem;
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
            return v.from(null);
        } else if (v.getData().getItem() == null) {
            return v.from(Sets.newHashSet());
        }

        Set<T> set = Sets.newHashSet(v.getData().getItem());
        return v.from(set);
    }

    @Override @SuppressWarnings("unchecked")
    public Result<ArrayItem<T>> marshal(Result<Set<T>> v) {
        if (v.getData() == null) {
            return v.from(null);
        }

        T[] array = v.getData().toArray((T[]) Array.newInstance(type, v.getData().size()));
        return v.from(new ArrayItem<>(array));
    }

}
