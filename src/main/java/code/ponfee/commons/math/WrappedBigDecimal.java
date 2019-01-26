package code.ponfee.commons.math;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 包装BigDecimal，用于lamda方法体内计算
 * @author Ponfee
 */
public class WrappedBigDecimal {

    private BigDecimal decimal;

    public WrappedBigDecimal(Number num) {
        decimal = new BigDecimal(num.doubleValue());
    }

    public synchronized void add(Number num) {
        decimal = decimal.add(new BigDecimal(num.doubleValue()));
    }

    public synchronized void divide(BigDecimal divisor) {
        decimal = decimal.divide(divisor);
    }

    public synchronized void remainder(BigDecimal divisor) {
        decimal = decimal.remainder(divisor);
    }

    public synchronized void abs(MathContext mc) {
        decimal = decimal.abs(mc);
    }

    public double getDouble() {
        return decimal.doubleValue();
    }

    public int getInt() {
        return decimal.intValue();
    }

    public long getLong() {
        return decimal.longValue();
    }

    public float getFloat() {
        return decimal.floatValue();
    }
}
