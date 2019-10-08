package code.ponfee.commons.json;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import code.ponfee.commons.model.Result;

public class JacksonIgnore {

    public static void main(String[] args) throws Exception {
        SimpleBeanPropertyFilter fieldFilter = SimpleBeanPropertyFilter.serializeAllExcept("code", "b", "c");
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter("fieldFilter", fieldFilter);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(filterProvider).addMixIn(Map.class, FieldFilterMixIn.class);

        System.out.println(mapper.writeValueAsString(Result.SUCCESS));
    }

    @JsonFilter("fieldFilter")
    interface FieldFilterMixIn {
    }

}
