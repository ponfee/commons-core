package code.ponfee.commons.model;

import code.ponfee.commons.json.Jsons;

/**
 * To json string super class
 * 
 * @author Ponfee
 */
public class ToJsonString {
    
    @Override
    public String toString() {
        return Jsons.toJson(this);
    }
}
