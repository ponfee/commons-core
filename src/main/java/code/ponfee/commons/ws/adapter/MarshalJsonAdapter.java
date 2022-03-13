package code.ponfee.commons.ws.adapter;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.reflect.ClassUtils;

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
