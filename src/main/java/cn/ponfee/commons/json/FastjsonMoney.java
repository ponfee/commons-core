/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.json;

import cn.ponfee.commons.reflect.GenericUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import org.javamoney.moneta.Money;

import javax.money.Monetary;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * The Fastjson Money Serializer & Deserializer
 *
 * @author Ponfee
 */
public class FastjsonMoney implements ObjectSerializer, ObjectDeserializer {

    private static final String CURRENCY = "currency";
    private static final String NUMBER = "number";

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter writer = serializer.getWriter();
        if (object == null) {
            serializer.writeNull();
        } else {
            Money money = (Money) object;
            writer.write("{\"" + CURRENCY + "\":\"");
            writer.write(money.getCurrency().getCurrencyCode());
            writer.write("\",\"" + NUMBER + "\":");
            writer.writeLong(money.getNumberStripped().movePointRight(money.getCurrency().getDefaultFractionDigits()).longValueExact());
            writer.write("}");
        }
    }

    @Override
    public Money deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        if (GenericUtils.getRawType(type) != Money.class) {
            throw new UnsupportedOperationException("Cannot supported deserialize type: " + type);
        }
        JSONObject jsonObject = parser.parseObject();
        String currencyCode = jsonObject.getString(CURRENCY);
        long number = jsonObject.getLongValue(NUMBER);
        return Money.ofMinor(Monetary.getCurrency(currencyCode), number);
    }

    @Override
    public int getFastMatchToken() {
        return 0 /*JSONToken.RBRACKET*/;
    }

}
