/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.ws.adapter;

import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.reflect.ClassUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * MarshalJsonResult -> MarshalJsonXml
 * 
 * `@XmlJavaTypeAdapter(MarshalJsonAdapter.class)
 * 
 * @author Ponfee
 * 
 * @param <T>
 */
public class MarshalJsonAdapter<T> extends XmlAdapter<MarshalJsonXml, Object> {

    @Override
    public MarshalJsonXml marshal(Object v) {
        if (v == null) {
            return null;
        }

        return new MarshalJsonXml(ClassUtils.getClassName(v.getClass()), Jsons.toJson(v));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object unmarshal(MarshalJsonXml v) throws Exception {
        if (v == null) {
            return null;
        }

        Class<?> type = Class.forName(v.getType());
        if (!MarshalJsonResult.class.isAssignableFrom(type)) {
            return Jsons.fromJson(v.getData(), type);
        } else {
            // must has default no args construct
            return ((Class<MarshalJsonResult>) type).newInstance().fromJson(v.getData());
        }
    }

}
