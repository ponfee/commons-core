package code.ponfee.commons.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import java.io.IOException;

/**
 * The Jackson Money Serializer
 *
 * @author Ponfee
 */
public class JacksonMoneySerializer extends JsonSerializer<Money> {

    @Override
    public void serialize(Money money, JsonGenerator generator, SerializerProvider provider) throws IOException {
        if (money == null) {
            return;
        }

        CurrencyUnit currency = money.getCurrency();
        MoneyDTO dto = new MoneyDTO(
            currency.getCurrencyCode(),
            money.getNumberStripped().movePointRight(currency.getDefaultFractionDigits()).longValueExact()
        );
        generator.writeObject(dto);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoneyDTO implements java.io.Serializable {
        private static final long serialVersionUID = 7609966318990085843L;
        private String currency;
        private Long number;
    }

}
