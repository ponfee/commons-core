package code.ponfee.commons.json;

import code.ponfee.commons.util.WrappedFastDateFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * The Jackson Date Deserializer, based {@link WrappedFastDateFormat}
 *  
 * @author Ponfee
 */
public class JacksonDateDeserializer extends JsonDeserializer<Date> {

    public static final JacksonDateDeserializer INSTANCE = new JacksonDateDeserializer();

    private final WrappedFastDateFormat format;

    public JacksonDateDeserializer() {
        this(WrappedFastDateFormat.DEFAULT);
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

    public static JacksonDateDeserializer of(WrappedFastDateFormat format) {
        return new JacksonDateDeserializer(format);
    }

}
