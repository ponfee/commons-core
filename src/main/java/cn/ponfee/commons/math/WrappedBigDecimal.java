/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.math;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 包装BigDecimal，用于lamda方法体内计算
 * 
 * @author Ponfee
 */
public class WrappedBigDecimal {

    private BigDecimal decimal;

    public WrappedBigDecimal(Number num) {
        this.decimal = BigDecimal.valueOf(num.doubleValue());
    }

    public synchronized void add(Number num) {
        this.decimal = this.decimal.add(BigDecimal.valueOf(num.doubleValue()));
    }

    public synchronized void divide(BigDecimal divisor) {
        this.decimal = this.decimal.divide(divisor);
    }

    public synchronized void remainder(BigDecimal divisor) {
        this.decimal = this.decimal.remainder(divisor);
    }

    public synchronized void abs(MathContext mc) {
        this.decimal = this.decimal.abs(mc);
    }

    public double getDouble() {
        return this.decimal.doubleValue();
    }

    public int getInt() {
        return this.decimal.intValue();
    }

    public long getLong() {
        return this.decimal.longValue();
    }

    public float getFloat() {
        return this.decimal.floatValue();
    }

    @Override
    public String toString() {
        return this.decimal.toString();
    }

}
