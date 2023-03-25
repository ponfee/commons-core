/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import cn.ponfee.commons.collect.Comparators;
import cn.ponfee.commons.reflect.GenericUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.stream.LongStream;

/**
 * Money definition based on {@link Long} numeric for minor currency unit representation.
 *
 * @author Ponfee
 */
@JSONType(serializer = Money.Fastjson.class, deserializer = Money.Fastjson.class) // fastjson
@JsonSerialize(using = Money.JacksonSerializer.class)     // jackson
@JsonDeserialize(using = Money.JacksonDeserializer.class) // jackson
public class Money implements Serializable, Comparable<Money>, Cloneable {
    private static final long serialVersionUID = 7743331479636754564L;

    public static final String FIELD_NAME_CURRENCY = "currency";

    public static final String FIELD_NAME_NUMBER = "number";

    /**
     * scaling mode, default<code>RoundingMode.HALF_EVEN</code>
     * <p>银行家舍入法: 四舍六入，当小数为0.5时，则取最近的偶数
     */
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * Current unit scaling factor
     */
    private static final int[] FACTORS = {1, 10, 100, 1000, 10000, 100000};

    /**
     * the currency
     */
    private final Currency currency;

    /**
     * the number
     */
    private long number;

    /**
     * Creates a MultiCurrencyMoney with majorUnitNumber, minorUnitNumber and currency
     *
     * @param currency        the currency
     * @param majorUnitNumber the majorUnitNumber
     * @param minorUnitNumber the minorUnitNumber
     */
    public Money(Currency currency, long majorUnitNumber, int minorUnitNumber) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot null.");
        }
        // check minorUnitNumber whether overflow
        int factor = getFactor();
        if (minorUnitNumber >= factor) {
            throw new RuntimeException("Minor[" + minorUnitNumber + "] must less than factor[" + factor + "].");
        }

        this.currency = currency;
        this.number = majorUnitNumber * factor + minorUnitNumber;
    }

    public Money(Currency currency, long number) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot null.");
        }
        this.currency = currency;
        this.number = number;
    }

    // -------------------------------------------------------------------------------------of methods
    public static Money of(Currency currency, long number) {
        return new Money(currency, number);
    }

    public static Money of(CurrencyEnum currencyEnum, long number) {
        return new Money(currencyEnum.currency(), number);
    }

    public static Money of(String currencyCode, long number) {
        return new Money(Currency.getInstance(currencyCode), number);
    }

    public Money ofMajor(CurrencyEnum currencyEnum, String majorUnitNumber, RoundingMode roundingMode) {
        return ofMajor(currencyEnum.currency(), new BigDecimal(majorUnitNumber), roundingMode);
    }

    public Money ofMajor(String currencyCode, String majorUnitNumber, RoundingMode roundingMode) {
        return ofMajor(Currency.getInstance(currencyCode), new BigDecimal(majorUnitNumber), roundingMode);
    }

    public Money ofMajor(Currency currency, String majorUnitNumber, RoundingMode roundingMode) {
        return ofMajor(currency, new BigDecimal(majorUnitNumber), roundingMode);
    }

    public static Money ofMajor(CurrencyEnum currencyEnum, BigDecimal majorUnitNumber, RoundingMode roundingMode) {
        return ofMajor(currencyEnum.currency(), majorUnitNumber, roundingMode);
    }

    public static Money ofMajor(String currencyCode, BigDecimal majorUnitNumber, RoundingMode roundingMode) {
        return ofMajor(Currency.getInstance(currencyCode), majorUnitNumber, roundingMode);
    }

    /**
     * Creates a new Money instance with the specified currency, major number and rounding mode.
     *
     * @param currency        the currency
     * @param majorUnitNumber the major unit number
     * @param roundingMode    the rounding mode
     */
    public static Money ofMajor(Currency currency, BigDecimal majorUnitNumber, RoundingMode roundingMode) {
        long number = rounding(majorUnitNumber.movePointRight(currency.getDefaultFractionDigits()), roundingMode);
        return new Money(currency, number);
    }

    /**
     * Obtains an instance of {@code Money} representing zero at a specific currency.
     * <p>
     * For example, {@code zero(USD)} creates the instance {@code USD 0.00}.
     *
     * @param currency the currency, not null
     * @return the instance representing zero, never null
     */
    public static Money zero(Currency currency) {
        return new Money(currency, 0);
    }

    public static Money zero(CurrencyEnum currencyEnum) {
        return zero(currencyEnum.currency());
    }

    public static Money zero(String currencyCode) {
        return zero(Currency.getInstance(currencyCode));
    }

    // -------------------------------------------------------------------------------------getter/setter methods

    /**
     * Returns currency
     *
     * @return {@code java.util.Currency} object
     */
    public Currency getCurrency() {
        return currency;
    }

    public long getNumber() {
        return number;
    }

    /**
     * Returns currency code
     *
     * @return currency code
     */
    public final String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    /**
     * Set number
     *
     * @param number the number
     */
    public void setNumber(long number) {
        this.number = number;
    }

    /**
     * Returns currency scaling factor
     *
     * @return scaling factor
     */
    public final int getFactor() {
        return FACTORS[currency.getDefaultFractionDigits()];
    }

    // -------------------------------------------------------------------------------------to methods

    /**
     * Returns BigDecimal of major number string
     *
     * @return major number BigDecimal
     */
    public BigDecimal toMajorNumber() {
        return BigDecimal.valueOf(number, currency.getDefaultFractionDigits());
    }

    /**
     * Returns major number string
     *
     * @return major number string(e.g. USD$1.23 -> 1.23)
     */
    public String toMajorString() {
        return toMajorNumber().toString();
    }

    // -------------------------------------------------------------------------------------基本对象方法

    /**
     * Returns the specified object is the same currency and has same amount
     *
     * @param other the other object
     * @return {@code true} if equals
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof Money) && equals((Money) other);
    }

    /**
     * Returns boolean of equals other money object
     *
     * @param other the other money
     * @return {@code true} if equals
     */
    public boolean equals(Money other) {
        if (other == null) {
            return false;
        }
        return currency.equals(other.currency) && (number == other.number);
    }

    /**
     * Return this object's hash code
     *
     * @return int value of hash code
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(currency).append(number).toHashCode();
    }

    /**
     * Returns this object's clone
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Money clone() {
        return new Money(currency, number);
    }

    /**
     * Returns the string representation of this Money.
     *
     * @return money as string(e.g. $1.23)
     */
    @Override
    public String toString() {
        return CurrencyEnum.ofCurrency(currency).currencySymbol() + toMajorString();
    }

    /**
     * Compares with other money object.
     *
     * @param other the other money.
     * @return -1: less, 0: equals, 1: greater
     */
    @Override
    public int compareTo(Money other) {
        assertSameCurrency(other);
        return Long.compare(number, other.number);
    }

    /**
     * Compares is greater than other money.
     *
     * @param other the other money.
     * @return {@code true} if greater than other
     */
    public boolean greaterThan(Money other) {
        return compareTo(other) > Comparators.EQ;
    }

    // -------------------------------------------------------------------------------------货币算术

    /**
     * Returns new Money object of the two money addition
     *
     * @param other the other money.
     * @return new Money object of the two money addition
     */
    public Money add(Money other) {
        assertSameCurrency(other);
        return create(number + other.number);
    }

    /**
     * This money addition other money
     *
     * @param other the other money
     * @return the caller money object(chain program)
     */
    public Money addTo(Money other) {
        assertSameCurrency(other);
        this.number += other.number;
        return this;
    }

    /**
     * Returns new Money object of the two money subtraction
     *
     * @param other the other money.
     * @return new Money object of the two money subtraction
     */
    public Money subtract(Money other) {
        assertSameCurrency(other);
        return create(number - other.number);
    }

    /**
     * This money subtraction other money
     *
     * @param other the other money
     * @return the caller money object(chain program)
     */
    public Money subtractFrom(Money other) {
        assertSameCurrency(other);
        this.number -= other.number;
        return this;
    }

    /**
     * Returns new Money object of this money multiply value
     *
     * @param val the value
     * @return new Money object of multiply result
     */
    public Money multiply(long val) {
        return create(number * val);
    }

    /**
     * This money multiply value factor
     *
     * @param val the value
     * @return the caller money object(chain program)
     */
    public Money multiplyBy(long val) {
        this.number *= val;
        return this;
    }

    /**
     * Returns new Money object of this money multiply value
     *
     * @param val the value
     * @return new Money object of multiply result
     */
    public Money multiply(BigDecimal val) {
        return multiply(val, DEFAULT_ROUNDING_MODE);
    }

    /**
     * This money multiply value factor
     *
     * @param val the value
     * @return the caller money object(chain program)
     */
    public Money multiplyBy(BigDecimal val) {
        return multiplyBy(val, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Returns new Money object of this money multiply value
     *
     * @param val          the value
     * @param roundingMode the rounding mode
     * @return new Money object of multiply result
     */
    public Money multiply(BigDecimal val, RoundingMode roundingMode) {
        return create(rounding(BigDecimal.valueOf(number).multiply(val), roundingMode));
    }

    /**
     * This money multiply value factor
     *
     * @param val          the value
     * @param roundingMode the rounding mode
     * @return the caller money object(chain program)
     */
    public Money multiplyBy(BigDecimal val, RoundingMode roundingMode) {
        this.number = rounding(BigDecimal.valueOf(number).multiply(val), roundingMode);
        return this;
    }

    public Money divide(BigDecimal val) {
        return divide(val, DEFAULT_ROUNDING_MODE);
    }

    public Money divide(BigDecimal val, RoundingMode roundingMode) {
        return create(BigDecimal.valueOf(number).divide(val, roundingMode).longValue());
    }

    public Money divideBy(BigDecimal val) {
        return divideBy(val, DEFAULT_ROUNDING_MODE);
    }

    public Money divideBy(BigDecimal val, RoundingMode roundingMode) {
        this.number = BigDecimal.valueOf(number).divide(val, roundingMode).longValue();
        return this;
    }

    // -------------------------------------------------------------------------------------slice

    /**
     * Average slice this money to segment part
     *
     * @param segment the segment
     * @return Money array
     */
    public Money[] slice(int segment) {
        Money[] results = new Money[segment];

        long low = number / segment, high = low + 1;
        int remainder = (int) number % segment;

        for (int i = 0; i < remainder; i++) {
            results[i] = create(high);
        }

        for (int i = remainder; i < segment; i++) {
            results[i] = create(low);
        }

        return results;
    }

    /**
     * Slice this money with specified ratios
     *
     * @param ratios the ratio
     * @return Money array
     */
    public Money[] slice(long[] ratios) {
        Money[] results = new Money[ratios.length];

        long total = LongStream.of(ratios).sum();
        long remainder = number;
        for (int i = 0; i < results.length; i++) {
            results[i] = create((number * ratios[i]) / total);
            remainder -= results[i].number;
        }

        for (int i = 0; i < remainder; i++) {
            results[i].number++;
        }

        return results;
    }

    // -------------------------------------------------------------------------------------private methods

    private void assertSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Money math currency mismatch.");
        }
    }

    private Money create(long number) {
        return new Money(currency, number);
    }

    /**
     * Returns BigDecimal to long value with specified rounding mode.
     *
     * @param val          the BigDecimal value
     * @param roundingMode the rounding mode
     * @return long value
     */
    private static long rounding(BigDecimal val, RoundingMode roundingMode) {
        return val.setScale(0, roundingMode).longValue();
    }

    // -------------------------------------------------------------------------------------custom fastjson deserialize

    /**
     * Custom deserialize Money based fastjson.
     */
    public static class Fastjson implements ObjectSerializer, ObjectDeserializer {
        @Override
        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
            SerializeWriter writer = serializer.getWriter();
            if (object == null) {
                serializer.writeNull();
            } else {
                Money money = (Money) object;
                writer.write("{\"" + FIELD_NAME_CURRENCY + "\":\"");
                writer.write(money.getCurrency().getCurrencyCode());
                writer.write("\",\"" + FIELD_NAME_NUMBER + "\":");
                writer.writeLong(money.getNumber());
                writer.write("}");
            }
        }

        @Override
        public Money deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            if (GenericUtils.getRawType(type) != Money.class) {
                throw new UnsupportedOperationException("Cannot supported deserialize type: " + type);
            }
            JSONObject jsonObject = parser.parseObject();
            String currencyCode = jsonObject.getString(FIELD_NAME_CURRENCY);
            long number = jsonObject.getLongValue(FIELD_NAME_NUMBER);
            return new Money(CurrencyEnum.ofCurrencyCode(currencyCode).currency(), number);
        }

        @Override
        public int getFastMatchToken() {
            return 0 /*JSONToken.RBRACKET*/;
        }
    }

    // -------------------------------------------------------------------------------------custom jackson serializer & deserialize

    /**
     * Custom serialize Money based jackson.
     */
    public static class JacksonSerializer extends JsonSerializer<Money> {
        @Override
        public void serialize(Money money, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
            if (money == null) {
                generator.writeNull();
            } else {
                generator.writeStartObject();
                generator.writeStringField(FIELD_NAME_CURRENCY, money.getCurrencyCode());
                generator.writeNumberField(FIELD_NAME_NUMBER, money.getNumber());
                generator.writeEndObject();
            }
        }
    }

    /**
     * Custom deserialize Money based jackson.
     */
    public static class JacksonDeserializer extends JsonDeserializer<Money> {
        @Override
        public Money deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
            BaseJsonNode jsonNode = parser.readValueAsTree();
            if (jsonNode == null || jsonNode instanceof NullNode) {
                return null;
            }
            String currencyCode = jsonNode.required(FIELD_NAME_CURRENCY).textValue();
            long number = jsonNode.required(FIELD_NAME_NUMBER).longValue();
            return new Money(CurrencyEnum.ofCurrencyCode(currencyCode).currency(), number);
        }
    }

}
