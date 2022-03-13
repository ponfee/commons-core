package code.ponfee.commons.util;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.stream.LongStream;

/**
 * 包含币种、金额（最小单位）
 *
 * @author Ponfee
 */
public class Money implements Serializable, Comparable<Money>, Cloneable {

    private static final long serialVersionUID = -2081951919841752223L;

    /**
     * 缺省的取整模式，为<code>BigDecimal.ROUND_HALF_EVEN
     * （银行家舍入法: 四舍六入，当小数为0.5时，则取最近的偶数）
     */
    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * 币种单位换算率
     */
    private static final int[] FACTORS = { 1, 10, 100, 1000, 10000, 100000 };

    /**
     * the currency
     */
    private final Currency currency;

    /**
     * the amount
     */
    private long amount;

    /**
     * Creates a MultiCurrencyMoney with majorUnitAmount, minorUnitAmount and currency
     *
     * @param currency the currency
     * @param majorUnitAmount the majorUnitAmount
     * @param minorUnitAmount the minorUnitAmount
     */
    public Money(Currency currency, long majorUnitAmount, int minorUnitAmount) {
        // check minorUnitAmount whether overflow
        int factor = getFactor();
        if (minorUnitAmount >= factor) {
            throw new RuntimeException("Minor[" + minorUnitAmount + "] must less than factor[" + factor + "].");
        }

        this.currency = currency;
        this.amount = majorUnitAmount * factor + minorUnitAmount;
    }

    public Money(Currency currency, long amount) {
        this.currency = currency;
        this.amount = amount;
    }

    // -------------------------------------------------------------------------------------of methods
    public static Money of(Currency currency, long amount) {
        return new Money(currency, amount);
    }

    public Money ofMajor(Currency currency, String majorUnitAmount, RoundingMode roundingMode) {
        return ofMajor(currency, new BigDecimal(majorUnitAmount), roundingMode);
    }

    /**
     * 创建一个具有金额<code>amount</code>和指定币种的货币对象。
     * 如果金额不能转换为整数分，则使用指定的取整模式<code>roundingMode</code>取整。
     *
     * @param currency 币种
     * @param majorUnitAmount 金额，以元为单位。
     * @param roundingMode 取整方式有以下值：
     *                          BigDecimal.ROUND_UP
     *                          BigDecimal.ROUND_DOWN
     *                          BigDecimal.ROUND_CEILING
     *                          BigDecimal.ROUND_FLOOR
     *                          BigDecimal.ROUND_HALF_UP
     *                          BigDecimal.ROUND_HALF_DOWN
     *                          BigDecimal.ROUND_HALF_EVEN （银行家舍入法）
     *                          BigDecimal.ROUND_UNNECESSARY
     */
    public static Money ofMajor(Currency currency, BigDecimal majorUnitAmount, RoundingMode roundingMode) {
        long amount = rounding(majorUnitAmount.movePointRight(currency.getDefaultFractionDigits()), roundingMode);
        return new Money(currency, amount);
    }

    // -------------------------------------------------------------------------------------getter/setter methods
    /**
     * 获取本货币对象代表的币种。
     *
     * @return 本货币对象所代表的币种。
     */
    public Currency getCurrency() {
        return currency;
    }

    public long getAmount() {
        return amount;
    }

    /**
     * Set amount
     *
     * @param amount the amount
     */
    public void setAmount(long amount) {
        this.amount = amount;
    }

    /**
     * 获取本货币币种的元/分换算比率。
     *
     * @return 本货币币种的元/分换算比率。
     */
    public final int getFactor() {
        return FACTORS[currency.getDefaultFractionDigits()];
    }

    /**
     * 取得本货币对象的币种代码
     *
     * @return 币种代码
     */
    public String getCurrencyCode() {
        return this.currency.getCurrencyCode();
    }

    // -------------------------------------------------------------------------------------to methods
    /**
     * 获取本货币对象代表的金额数。
     *
     * @return 金额数，以元为单位。
     */
    public BigDecimal toMajorAmount() {
        return BigDecimal.valueOf(amount, currency.getDefaultFractionDigits());
    }

    /**
     * 获取本货币对象代表的金额值字符串（单位是元，比如：USD$1.23本方法输出的是1.23）。
     *
     * @return 金额值字符串，以元为单位（比如：USD$1.23将返回1.23）。
     */
    public String toMajorString() {
        return toMajorAmount().toString();
    }

    // -------------------------------------------------------------------------------------基本对象方法

    /**
     * 判断本货币对象与另一对象是否相等。
     *
     * <p>
     * 本货币对象与另一对象相等的充分必要条件是：<br>
     *  <ul>
     *   <li>另一对象也属货币对象类。
     *   <li>金额相同。
     *   <li>币种相同。
     *  </ul>
     *
     * @param other 待比较的另一对象。
     * @return <code>true</code>表示相等，<code>false</code>表示不相等。
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof Money) && equals((Money) other);
    }

    /**
     * 判断本货币对象与另一货币对象是否相等。
     *
     * <p>
     * 本货币对象与另一货币对象相等的充分必要条件是：<br>
     *  <ul>
     *   <li>金额相同。
     *   <li>币种相同。
     *  </ul>
     *
     * @param other 待比较的另一货币对象。
     * @return <code>true</code>表示相等，<code>false</code>表示不相等。
     */
    public boolean equals(Money other) {
        if (other == null) {
            return false;
        }
        return currency.equals(other.currency) && (amount == other.amount);
    }

    /**
     * 计算本货币对象的杂凑值。
     *
     * @return 本货币对象的杂凑值。
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(currency.getCurrencyCode()).append(amount).toHashCode();
    }

    /**
     * 克隆一个本货币对象的副本。
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Money clone() {
        return new Money(this.currency, this.amount);
    }

    /**
     * 转为字段串
     *
     * @return 转为字段串，如：$1.23
     */
    @Override
    public String toString() {
        return Currencys.of(this.currency).symbol() + toMajorAmount().toString();
    }

    /**
     * 货币比较。
     *
     * <p>
     * 比较本货币对象与另一货币对象的大小。
     * 如果待比较的两个货币对象的币种不同，则抛出<code>java.lang.IllegalArgumentException</code>。
     * 如果本货币对象的金额少于待比较货币对象，则返回-1。
     * 如果本货币对象的金额等于待比较货币对象，则返回0。
     * 如果本货币对象的金额大于待比较货币对象，则返回1。
     *
     * @param other 另一对象。
     * @return -1表示小于，0表示等于，1表示大于。
     *
     * @exception IllegalArgumentException 待比较货币对象与本货币对象的币种不同。
     */
    @Override
    public int compareTo(Money other) {
        assertSameCurrency(other);
        return Long.compare(amount, other.amount);
    }

    /**
     * 货币比较。
     *
     * <p>
     * 判断本货币对象是否大于另一货币对象。
     * 如果待比较的两个货币对象的币种不同，则抛出<code>java.lang.IllegalArgumentException</code>。
     * 如果本货币对象的金额大于待比较货币对象，则返回true，否则返回false。
     *
     * @param other 另一对象。
     * @return true表示大于，false表示不大于（小于等于）。
     *
     * @exception IllegalArgumentException 待比较货币对象与本货币对象的币种不同。
     */
    public boolean greaterThan(Money other) {
        return compareTo(other) > 0;
    }

    // -------------------------------------------------------------------------------------货币算术

    /**
     * 货币加法。
     *
     * <p>
     * 如果两货币币种相同，则返回一个新的相同币种的货币对象，其金额为
     * 两货币对象金额之和，本货币对象的值不变。
     * 如果两货币对象币种不同，抛出<code>java.lang.IllegalArgumentException</code>。
     *
     * @param other 作为加数的货币对象。
     *
     * @exception IllegalArgumentException 如果本货币对象与另一货币对象币种不同。
     *
     * @return 相加后的结果。
     */
    public Money add(Money other) {
        assertSameCurrency(other);
        return copy(amount + other.amount);
    }

    /**
     * 货币累加。
     *
     * <p>
     * 如果两货币币种相同，则本货币对象的金额等于两货币对象金额之和，并返回本货币对象的引用。
     * 如果两货币对象币种不同，抛出<code>java.lang.IllegalArgumentException</code>。
     *
     * @param other 作为加数的货币对象。
     *
     * @exception IllegalArgumentException 如果本货币对象与另一货币对象币种不同。
     *
     * @return 累加后的本货币对象。
     */
    public Money addTo(Money other) {
        assertSameCurrency(other);
        this.amount += other.amount;
        return this;
    }

    /**
     * 货币减法。
     *
     * <p>
     * 如果两货币币种相同，则返回一个新的相同币种的货币对象，其金额为
     * 本货币对象的金额减去参数货币对象的金额。本货币对象的值不变。
     * 如果两货币币种不同，抛出<code>java.lang.IllegalArgumentException</code>。
     *
     * @param other 作为减数的货币对象。
     *
     * @exception IllegalArgumentException 如果本货币对象与另一货币对象币种不同。
     *
     * @return 相减后的结果。
     */
    public Money subtract(Money other) {
        assertSameCurrency(other);
        return copy(amount - other.amount);
    }

    /**
     * 货币累减。
     *
     * <p>
     * 如果两货币币种相同，则本货币对象的金额等于两货币对象金额之差，并返回本货币对象的引用。
     * 如果两货币币种不同，抛出<code>java.lang.IllegalArgumentException</code>。
     *
     * @param other 作为减数的货币对象。
     *
     * @exception IllegalArgumentException 如果本货币对象与另一货币对象币种不同。
     *
     * @return 累减后的本货币对象。
     */
    public Money subtractFrom(Money other) {
        assertSameCurrency(other);
        this.amount -= other.amount;
        return this;
    }

    /**
     * 货币乘法。
     *
     * <p>
     * 返回一个新的货币对象，币种与本货币对象相同，金额为本货币对象的金额乘以乘数。
     * 本货币对象的值不变。
     *
     * @param val 乘数
     *
     * @return 乘法后的结果。
     */
    public Money multiply(long val) {
        return copy(amount * val);
    }

    /**
     * 货币累乘。
     *
     * <p>
     * 本货币对象金额乘以乘数，并返回本货币对象。
     *
     * @param val 乘数
     *
     * @return 累乘后的本货币对象。
     */
    public Money multiplyBy(long val) {
        this.amount *= val;
        return this;
    }

    /**
     * 货币乘法（默认以银行家舍入法来进行取整：BigDecimal.ROUND_HALF_EVEN）。
     *
     * <p>
     * 返回一个新的货币对象，币种与本货币对象相同，金额为本货币对象的金额乘以乘数。
     * 本货币对象的值不变。如果相乘后的金额不能转换为整数分，使用缺省的取整模式
     * <code>DEFUALT_ROUNDING_MODE</code>进行取整。
     *
     * @param val 乘数
     *
     * @return 相乘后的结果。
     */
    public Money multiply(BigDecimal val) {
        return multiply(val, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 货币累乘（默认以银行家舍入法来进行取整：BigDecimal.ROUND_HALF_EVEN）。
     *
     * <p>
     * 本货币对象金额乘以乘数，并返回本货币对象。
     * 如果相乘后的金额不能转换为整数分，使用缺省的取整方式
     * <code>DEFUALT_ROUNDING_MODE</code>进行取整。
     *
     * @param val 乘数
     *
     * @return 累乘后的结果。
     */
    public Money multiplyBy(BigDecimal val) {
        return multiplyBy(val, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 货币乘法。
     *
     * <p>
     * 返回一个新的货币对象，币种与本货币对象相同，金额为本货币对象的金额乘以乘数。
     * 本货币对象的值不变。如果相乘后的金额不能转换为整数分，使用指定的取整方式
     * <code>roundingMode</code>进行取整。
     *
     * @param val 乘数
     * @param roundingMode 取整方式
     *
     * @return 相乘后的结果。
     */
    public Money multiply(BigDecimal val, RoundingMode roundingMode) {
        return copy(rounding(BigDecimal.valueOf(amount).multiply(val), roundingMode));
    }

    /**
     * 货币累乘。
     *
     * <p>
     * 本货币对象金额乘以乘数，并返回本货币对象。
     * 如果相乘后的金额不能转换为整数分，使用指定的取整方式
     * <code>roundingMode</code>进行取整。
     *
     * @param val 乘数
     * @param roundingMode 取整方式
     *
     * @return 累乘后的结果。
     */
    public Money multiplyBy(BigDecimal val, RoundingMode roundingMode) {
        this.amount = rounding(BigDecimal.valueOf(amount).multiply(val), roundingMode);
        return this;
    }

    /**
     * 货币除法（默认以银行家舍入法来进行取整：BigDecimal.ROUND_HALF_EVEN）。
     *
     * <p>
     * 返回一个新的货币对象，币种与本货币对象相同，金额为本货币对象的金额除以除数。
     * 本货币对象的值不变。如果相除后的金额不能转换为整数分，使用缺省的取整模式
     * <code>DEFAULT_ROUNDING_MODE</code>进行取整。
     *
     * @param val 除数
     *
     * @return 相除后的结果。
     */
    public Money divide(BigDecimal val) {
        return divide(val, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 货币除法。
     *
     * <p>
     * 返回一个新的货币对象，币种与本货币对象相同，金额为本货币对象的金额除以除数。
     * 本货币对象的值不变。如果相除后的金额不能转换为整数分，使用指定的取整模式
     * <code>roundingMode</code>进行取整。
     *
     * @param val 除数
     * @param roundingMode 取整方式
     *
     * @return 相除后的结果。
     */
    public Money divide(BigDecimal val, RoundingMode roundingMode) {
        return copy(BigDecimal.valueOf(amount).divide(val, roundingMode).longValue());
    }

    /**
     * 货币累除（默认以银行家舍入法来进行取整：BigDecimal.ROUND_HALF_EVEN）。
     *
     * <p>
     * 本货币对象金额除以除数，并返回本货币对象。
     * 如果相除后的金额不能转换为整数分，使用缺省的取整模式
     * <code>DEFAULT_ROUNDING_MODE</code>进行取整。
     *
     * @param val 除数
     *
     * @return 累除后的结果。
     */
    public Money divideBy(BigDecimal val) {
        return divideBy(val, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 货币累除。
     *
     * <p>
     * 本货币对象金额除以除数，并返回本货币对象。
     * 如果相除后的金额不能转换为整数分，使用指定的取整模式
     * <code>roundingMode</code>进行取整。
     *
     * @param val 除数
     * @param roundingMode 取整方式
     *
     * @return 累除后的结果。
     */
    public Money divideBy(BigDecimal val, RoundingMode roundingMode) {
        this.amount = BigDecimal.valueOf(amount).divide(val, roundingMode).longValue();
        return this;
    }

    // -------------------------------------------------------------------------------------slice
    /**
     * 货币分配。
     *
     * <p>
     * 将本货币对象尽可能平均分配成<code>segment</code>份。
     * 如果不能平均分配尽，则将零头放到开始的若干份中。分配
     * 运算能够确保不会丢失金额零头。
     *
     * @param segment 待分配的份数
     *
     * @return 货币对象数组，数组的长度与分配份数相同，数组元素
     *         从大到小排列，所有货币对象的金额最多只相差1分。
     */
    public Money[] slice(int segment) {
        Money[] results = new Money[segment];

        long low = amount / segment, hight = low + 1;
        int remainder = (int) amount % segment;

        for (int i = 0; i < remainder; i++) {
            results[i] = copy(hight);
        }

        for (int i = remainder; i < segment; i++) {
            results[i] = copy(low);
        }

        return results;
    }

    /**
     * 货币分配。
     *
     * <p>
     * 将本货币对象按照规定的比例分配成若干份。分配所剩的零头
     * 从第一份开始顺序分配。分配运算确保不会丢失金额零头。
     *
     * @param ratios 分配比例数组，每一个比例是一个长整型，代表
     *               相对于总数的相对数。
     *
     * @return 货币对象数组，数组的长度与分配比例数组的长度相同。
     */
    public Money[] slice(long[] ratios) {
        Money[] results = new Money[ratios.length];

        long total = LongStream.of(ratios).sum();
        long remainder = amount;
        for (int i = 0; i < results.length; i++) {
            results[i] = copy((amount * ratios[i]) / total);
            remainder -= results[i].amount;
        }

        for (int i = 0; i < remainder; i++) {
            results[i].amount++;
        }

        return results;
    }

    // -------------------------------------------------------------------------------------private methods

    /**
     * 断言本货币对象与另一货币对象是否具有相同的币种。
     *
     * <p>
     * 如果本货币对象与另一货币对象具有相同的币种，则方法返回。
     * 否则抛出运行时异常<code>java.lang.IllegalArgumentException</code>。
     *
     * @param other 另一货币对象
     *
     * @exception IllegalArgumentException 如果本货币对象与另一货币对象币种不同。
     */
    private void assertSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Money math currency mismatch.");
        }
    }

    /**
     * 创建一个币种相同，具有指定金额的货币对象。
     *
     * @param amount 金额，以分为单位（即最小货币单位的值）
     *
     * @return 一个新建的币种相同，具有指定金额的货币对象
     */
    private Money copy(long amount) {
        return new Money(currency, amount);
    }

    /**
     * 对BigDecimal型的值按指定取整方式取整。
     *
     * @param val 待取整的BigDecimal值
     * @param roundingMode 取整方式
     *
     * @return 取整后的long型值
     */
    private static long rounding(BigDecimal val, RoundingMode roundingMode) {
        return val.setScale(0, roundingMode).longValue();
    }

}
