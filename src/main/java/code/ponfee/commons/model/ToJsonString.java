package code.ponfee.commons.model;

import code.ponfee.commons.json.Jsons;

/**
 * Override {@code Object#toString()} method, implemented to json string.
 * 
 * @author Ponfee
 */
public abstract class ToJsonString {

    @Override
    public String toString() {
        return Jsons.toJson(this);
    }
}
