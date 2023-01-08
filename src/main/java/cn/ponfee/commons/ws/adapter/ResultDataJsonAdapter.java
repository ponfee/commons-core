/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.ws.adapter;

import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.model.Result;
import cn.ponfee.commons.reflect.GenericUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Result<data> -> Result<json>
 * @param <T>
 * 
 * @author Ponfee
 */
public abstract class ResultDataJsonAdapter<T> extends XmlAdapter<Result<String>, Result<T>> {

    protected final Class<T> type;

    protected ResultDataJsonAdapter() {
        type = GenericUtils.getActualTypeArgument(this.getClass());
    }

    @Override
    public Result<T> unmarshal(Result<String> v) {
        if (StringUtils.isEmpty(v.getData())) {
            return v.from(null);
        }

        return v.from(Jsons.fromJson(v.getData(), type));
    }

    @Override
    public Result<String> marshal(Result<T> v) {
        if (v.getData() == null) {
            return v.from(null);
        }

        return v.from(Jsons.toJson(v.getData()));
    }

}
