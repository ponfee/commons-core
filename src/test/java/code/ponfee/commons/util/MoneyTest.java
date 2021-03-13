package code.ponfee.commons.util;

import java.util.Locale;

import org.junit.Test;

import code.ponfee.commons.serial.JdkSerializer;
import code.ponfee.commons.serial.Serializer;

/**
 * @author Ponfee
 */
public class MoneyTest {

    @Test
    public void test1() {
        Money money = new Money(Currencys.USD.currency(), 53243234);
        System.out.println(money.toString());

        System.out.println("----------------------");
        System.out.println(Currencys.JPY.currencyCode());
        System.out.println(Currencys.JPY.countryCode());
        System.out.println(Currencys.JPY.currencySymbol());

        System.out.println("----------------------");
        System.out.println(Currencys.JPY.currency().getCurrencyCode());
        System.out.println(Currencys.JPY.currency().getNumericCode());
        System.out.println(Currencys.JPY.currency().getSymbol());
        System.out.println(Currencys.JPY.currency().getSymbol(Locale.CHINA));
    }

    @Test
    public void test2() {
        for (Currencys c : Currencys.values()) {
            if (!c.countryCode().equals(String.format("%03d", c.currency().getNumericCode()))) {
                System.out.println(c.countryCode() + " != " + c.currency().getNumericCode());
            }
        }
    }

    @Test
    public void test3() {
        Money money = new Money(Currencys.USD.currency(), 53243234);

        //Serializer ser = new KryoSerializer();
        Serializer ser = new JdkSerializer();
        byte[] data = ser.serialize(money);
        System.out.println(ser.deserialize(data, Money.class));
    }

}
