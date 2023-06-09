/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.io.IOException;

/**
 * The Jackson Currency unit Serializer & Deserializer
 *
 * @author Ponfee
 */
public class JacksonCurrencyUnit {

    public static final JacksonCurrencyUnit INSTANCE = new JacksonCurrencyUnit();

    private final JsonSerializer<CurrencyUnit> serializer;
    private final JsonDeserializer<CurrencyUnit> deserializer;

    private JacksonCurrencyUnit() {
        this.serializer = new Serializer();
        this.deserializer = new Deserializer();
    }

    public JsonSerializer<CurrencyUnit> serializer() {
        return this.serializer;
    }

    public JsonDeserializer<CurrencyUnit> deserializer() {
        return this.deserializer;
    }

    private static class Serializer extends JsonSerializer<CurrencyUnit> {
        @Override
        public void serialize(CurrencyUnit currencyUnit, JsonGenerator generator, SerializerProvider provider) throws IOException {
            if (currencyUnit == null) {
                generator.writeNull();
            } else {
                generator.writeString(currencyUnit.getCurrencyCode());
            }
        }
    }

    private static class Deserializer extends JsonDeserializer<CurrencyUnit> {
        @Override
        public CurrencyUnit deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String currencyCode = parser.getValueAsString();
            if (StringUtils.isBlank(currencyCode)) {
                return null;
            }
            return Monetary.getCurrency(currencyCode);
        }
    }

}
