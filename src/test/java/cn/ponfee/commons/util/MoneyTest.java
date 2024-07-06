package cn.ponfee.commons.util;

import cn.ponfee.commons.serial.JdkSerializer;
import cn.ponfee.commons.serial.Serializer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

/**
 * @author Ponfee
 */
public class MoneyTest {

    
    @Test
    public void testx() {
        Assert.assertEquals("MXV", new String(new char[]{0x4d, 0x58, 0x56}));
    }

    @Test
    public void test0() {
        System.out.println(Arrays.toString(Strings.hexadecimal(CurrencyEnum.CZK.currencySymbol())));
        System.out.println(Bytes.encodeHex(CurrencyEnum.CZK.currencySymbol().substring(1,2).getBytes(StandardCharsets.UTF_8)));
        System.out.println();
        for (char x = 'A'; x <= 'Z'; x++) {
            for (char y = 'A'; y <= 'Z'; y++) {
                for (char z = 'A'; z <= 'Z'; z++) {
                    try {
                        Currency instance = Currency.getInstance(new String(new char[]{x, y, z}));
                        if (CurrencyEnum.ofCurrencyCode(instance.getCurrencyCode()) == null && CurrencyEnum.ofNumericCode(String.format("%03d", instance.getNumericCode())) == null) {
                            String text = String.format(
                                "/** %s [%s] */\n%s(new char[]{%s}),",
                                instance.getDisplayName(Locale.CHINA),
                                instance.getSymbol(Locale.CHINA),
                                instance.getCurrencyCode(),
                                Strings.join(Arrays.asList(Strings.hexadecimal(instance.getSymbol(Locale.CHINA))), ", ")
                            );
                            System.out.println(text);
                            System.out.println();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    /*
    @Test
    public void testa() {
        for (CurrencyEnum1 c1 : CurrencyEnum1.values()) {
            CurrencyEnum c0 = CurrencyEnum.ofCurrencyCode(c1.currencyCode());
            if (c0 == null) {
                System.err.println("not found: "+c1.currencyCode());
                continue;
            }
            if (c1.currencySymbol().equals(c0.currencySymbol())) {
                continue;
            }
            System.out.println(c1.currencyCode()+", "+c0.currencySymbol()+", "+c1.currencySymbol());
        }
    }
    */
    
    @Test
    public void test1() {
        System.out.println(CurrencyEnum.INR.currencySymbol());
        System.out.println(CurrencyEnum.UAH.currencySymbol());
        System.out.println(CurrencyEnum.CHF.currencySymbol());
        System.out.println(CurrencyEnum.AZN.currencySymbol());
        System.out.println(CurrencyEnum.GHS.currencySymbol());
        System.out.println(CurrencyEnum.TRY.currencySymbol());
        Money money = new Money(CurrencyEnum.USD.currency(), 53243234);
        System.out.println(money);

        System.out.println("----------------------");
        System.out.println(CurrencyEnum.JPY.currencyCode());
        System.out.println(CurrencyEnum.JPY.numericCode());
        System.out.println(CurrencyEnum.JPY.currencySymbol());

        System.out.println("----------------------");
        System.out.println(CurrencyEnum.JPY.currency().getCurrencyCode());
        System.out.println(CurrencyEnum.JPY.currency().getNumericCode());
        System.out.println(CurrencyEnum.JPY.currency().getSymbol());
        System.out.println(CurrencyEnum.JPY.currency().getSymbol(Locale.CHINA));
    }

    @Test
    public void test2() {
        System.out.println(CurrencyEnum.CNY.currency().getNumericCode());
        for (CurrencyEnum c : CurrencyEnum.values()) {
            System.out.println(c.currency().getSymbol(Locale.CHINA));
            if (!c.numericCode().equals(String.format("%03d", c.currency().getNumericCode()))) {
                System.out.println(c.numericCode() + " != " + c.currency().getNumericCode());
            }
        }
    }

    @Test
    public void test3() {
        Money money = new Money(CurrencyEnum.USD.currency(), 53243234);

        //Serializer ser = KryoSerializer.INSTANCE;
        Serializer ser = new JdkSerializer();
        byte[] data = ser.serialize(money);
        System.out.println(ser.deserialize(data, Money.class));
    }

}
