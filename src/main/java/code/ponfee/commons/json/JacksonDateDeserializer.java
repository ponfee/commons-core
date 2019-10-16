package code.ponfee.commons.json;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import code.ponfee.commons.util.WrappedFastDateFormat;

/**
 * The TypeReference holder
 *  
 * @author Ponfee
 */
public class JacksonDateDeserializer extends JsonDeserializer<Date> {

    private final WrappedFastDateFormat format;

    public JacksonDateDeserializer(String pattern) {
        this(pattern, false);
    }

    public JacksonDateDeserializer(String pattern, boolean strict) {
        this(new WrappedFastDateFormat(pattern, strict));
    }

    public JacksonDateDeserializer(WrappedFastDateFormat format) {
        this.format = format;
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (StringUtils.isBlank(text)) {
            return null;
        }

        try {
            return format.parse(text);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + text);
        }
    }

    public static JacksonDateDeserializer of(String pattern) {
        return new JacksonDateDeserializer(pattern);
    }

    public static JacksonDateDeserializer of(WrappedFastDateFormat format) {
        return new JacksonDateDeserializer(format);
    }

}
