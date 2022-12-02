package code.ponfee.commons.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.io.IOException;

/**
 * The Jackson Money Serializer & Deserializer
 *
 * @author Ponfee
 */
public class JacksonMoney {

    public static final JacksonMoney INSTANCE = new JacksonMoney();

    private static final String CURRENCY = "currency";
    private static final String NUMBER = "number";

    private final JsonSerializer<Money> serializer;
    private final JsonDeserializer<Money> deserializer;

    private JacksonMoney() {
        this.serializer = new Serializer();
        this.deserializer = new Deserializer();
    }

    public JsonSerializer<Money> serializer() {
        return this.serializer;
    }

    public JsonDeserializer<Money> deserializer() {
        return this.deserializer;
    }

    private static class Serializer extends JsonSerializer<Money> {
        @Override
        public void serialize(Money money, JsonGenerator generator, SerializerProvider provider) throws IOException {
            if (money == null) {
                return;
            }
            //generator.writeObject(new MoneyDTO(currencyCode, number));
            CurrencyUnit currency = money.getCurrency();
            generator.writeStartObject();
            generator.writeStringField(CURRENCY, currency.getCurrencyCode());
            generator.writeNumberField(NUMBER, money.getNumberStripped().movePointRight(currency.getDefaultFractionDigits()).longValueExact());
            generator.writeEndObject();
        }
    }

    private static class Deserializer extends JsonDeserializer<Money> {
        @Override
        public Money deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            //MoneyDTO dto = parser.readValueAs(MoneyDTO.class);
            BaseJsonNode jsonNode = parser.readValueAsTree();
            if (jsonNode == null || jsonNode instanceof NullNode) {
                return null;
            }
            String currency = jsonNode.required(CURRENCY).textValue();
            long number = jsonNode.required(NUMBER).longValue();
            return Money.ofMinor(Monetary.getCurrency(currency), number);
        }
    }

}
