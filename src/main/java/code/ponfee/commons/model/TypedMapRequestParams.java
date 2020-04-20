package code.ponfee.commons.model;

import java.util.Map;

/**
 * 通用Map请求参数类（不能继承Map，否则会被内置Map解析器优先处理）
 * 
 * @author Ponfee
 */
public class TypedMapRequestParams implements TypedDictionary<String, Object>, java.io.Serializable {

    private static final long serialVersionUID = 1898625104491344717L;

    private final Map<String, Object> params;

    public TypedMapRequestParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object put(String key, Object value) {
        return this.params.put(key, value);
    }

    @Override
    public Object getValue(String key) {
        return this.params.get(key);
    }

    @Override
    public Object removeKey(String key) {
        return this.params.remove(key);
    }

    public Map<String, Object> params() {
        return this.params;
    }

}
