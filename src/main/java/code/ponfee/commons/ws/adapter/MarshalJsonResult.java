package code.ponfee.commons.ws.adapter;

import code.ponfee.commons.json.Jsons;

/**
 * Market a bean type defined
 * 
 * @author Ponfee
 */
public interface MarshalJsonResult {

    default MarshalJsonResult fromJson(String json) {
        return Jsons.fromJson(json, this.getClass());
    }
}
