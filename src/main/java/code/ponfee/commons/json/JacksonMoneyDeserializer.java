package code.ponfee.commons.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.io.IOException;

/**
 * The Jackson Money Deserializer
 *
 * @author Ponfee
 */
public class JacksonMoneyDeserializer extends JsonDeserializer<Money> {

    @Override
    public Money deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JacksonMoneySerializer.MoneyDTO dto = parser.readValueAs(JacksonMoneySerializer.MoneyDTO.class);
        if (dto == null) {
            return null;
        }
        CurrencyUnit currencyUnit = Monetary.getCurrency(dto.getCurrency());
        return Money.ofMinor(currencyUnit, dto.getNumber());
    }

}
