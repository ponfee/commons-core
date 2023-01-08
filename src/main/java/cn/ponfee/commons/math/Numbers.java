/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.math;

import cn.ponfee.commons.base.tuple.Tuple2;
import com.google.common.base.Strings;
import com.google.common.primitives.Chars;
import org.apache.commons.codec.binary.Hex;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * <pre>
 * Number utility
 * 
 * 十进制：10
 * 二进制：0B10
 * 八进制：010
 * 十六进制：0X10
 * 小数点：1e-9
 * </pre>
 * 
 * @author Ponfee
 */
public final class Numbers {

    public static final int     INT_ZERO     = 0;
    public static final Integer INTEGER_ZERO = INT_ZERO;
    public static final byte    BYTE_ZERO    = 0x00;
    public static final char    CHAR_ZERO    = '\u0000'; // equals '\0'
    public static final double  DOUBLE_ZERO  = 0.0D;
    public static final double  DOUBLE_ONE   = 1.0D;

    // --------------------------------------------------------------character convert
    public static char toChar(Object obj) {
        return toChar(obj, CHAR_ZERO);
    }

    public static char toChar(Object obj, char defaultVal) {
        Character value = toWrapChar(obj);
        return value == null ? defaultVal : value;
    }

    public static Character toWrapChar(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Character) {
            return (Character) obj;
        } else if (obj instanceof Number) {
            return (char) ((Number) obj).intValue();
        } else if (obj instanceof byte[]) {
            return Chars.fromByteArray((byte[]) obj);
        } else if (obj instanceof Boolean) {
            return (char) (((boolean) obj) ? 0xFF : 0x00);
        } else {
            String str = obj.toString();
            return str.length() == 1 ? str.charAt(0) : null;
        }
    }

    // -----------------------------------------------------------------boolean convert
    public static boolean toBoolean(Object obj) {
        return toBoolean(obj, false);
    }

    public static boolean toBoolean(Object obj, boolean defaultVal) {
        Boolean value = toWrapBoolean(obj);
        return value == null ? defaultVal : value;
    }

    public static Boolean toWrapBoolean(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof Number) {
            return ((Number) obj).byteValue() != BYTE_ZERO;
        } else {
            return Boolean.parseBoolean(obj.toString());
        }
    }

    // -----------------------------------------------------------------byte convert
    public static byte toByte(Object obj) {
        return toByte(obj, (byte) 0);
    }

    public static byte toByte(Object obj, byte defaultVal) {
        if (obj instanceof Number) {
            return ((Number) obj).byteValue();
        }
        Long value = parseLong(obj);
        return value == null ? defaultVal : value.byteValue();
    }

    public static Byte toWrapByte(Object obj) {
        if (obj instanceof Byte) {
            return (Byte) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).byteValue();
        }
        Long value = parseLong(obj);
        return value == null ? null : value.byteValue();
    }

    // -----------------------------------------------------------------short convert
    public static short toShort(Object obj) {
        return toShort(obj, (short) 0);
    }

    public static short toShort(Object obj, short defaultVal) {
        if (obj instanceof Number) {
            return ((Number) obj).shortValue();
        }
        Long value = parseLong(obj);
        return value == null ? defaultVal : value.shortValue();
    }

    public static Short toWrapShort(Object obj) {
        if (obj instanceof Short) {
            return (Short) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).shortValue();
        }
        Long value = parseLong(obj);
        return value == null ? null : value.shortValue();
    }

    // -----------------------------------------------------------------int convert
    public static int toInt(Object obj) {
        return toInt(obj, 0);
    }

    public static int toInt(Object obj, int defaultVal) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        Long value = parseLong(obj);
        return value == null ? defaultVal : value.intValue();
    }

    public static Integer toWrapInt(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        Long value = parseLong(obj);
        return value == null ? null : value.intValue();
    }

    // -----------------------------------------------------------------long convert
    public static long toLong(Object obj) {
        return toLong(obj, 0L);
    }

    public static long toLong(Object obj, long defaultVal) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        Long value = parseLong(obj);
        return value == null ? defaultVal : value;
    }

    public static Long toWrapLong(Object obj) {
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return parseLong(obj);
    }

    // -----------------------------------------------------------------float convert
    public static float toFloat(Object obj) {
        return toFloat(obj, 0.0F);
    }

    public static float toFloat(Object obj, float defaultVal) {
        if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        }
        Double value = parseDouble(obj);
        return value == null ? defaultVal : value.floatValue();
    }

    public static Float toWrapFloat(Object obj) {
        if (obj instanceof Float) {
            return (Float) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        }
        Double value = parseDouble(obj);
        return value == null ? null : value.floatValue();
    }

    // -----------------------------------------------------------------double convert
    public static double toDouble(Object obj) {
        return toDouble(obj, 0.0D);
    }

    public static double toDouble(Object obj, double defaultVal) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        Double value = parseDouble(obj);
        return value == null ? defaultVal : value;
    }

    public static Double toWrapDouble(Object obj) {
        if (obj instanceof Double) {
            return (Double) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return parseDouble(obj);
    }

    // ---------------------------------------------------------------------number format
    /**
     * 数字精度化
     *
     * @param value
     * @param scale
     * @return
     */
    public static double scale(Object value, int scale) {
        double val = toDouble(value);

        if (scale < 0) {
            return val;
        }

        return BigDecimal.valueOf(val)
                         .setScale(scale, RoundingMode.HALF_UP)
                         .doubleValue();
    }

    /**
     * 向下转单位
     *
     * @param value
     * @param pow
     * @return
     */
    public static double lower(double value, int pow) {
        return BigDecimal.valueOf(value / Math.pow(10, pow)).doubleValue();
    }

    public static double lower(double value, int pow, int scale) {
        return BigDecimal.valueOf(value / Math.pow(10, pow))
                         .setScale(scale, RoundingMode.HALF_UP)
                         .doubleValue();
    }

    /**
     * 向上转单位
     *
     * @param value
     * @param pow
     * @return
     */
    public static double upper(double value, int pow) {
        return BigDecimal.valueOf(value * Math.pow(10, pow)).doubleValue();
    }

    public static double upper(double value, int pow, int scale) {
        return BigDecimal.valueOf(value * Math.pow(10, pow))
                         .setScale(scale, RoundingMode.HALF_UP)
                         .doubleValue();
    }

    /**
     * 百分比
     *
     * @param numerator
     * @param denominator
     * @param scale
     * @return
     */
    public static String percent(double numerator, double denominator, int scale) {
        if (denominator == 0.0D) {
            return "--";
        }

        return percent(numerator / denominator, scale);
    }

    /**
     * 百分比
     *
     * @param value
     * @param scale
     * @return
     */
    public static String percent(double value, int scale) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "--";
        }

        String format = "#,##0";
        if (scale > 0) {
            // StringUtils.leftPad("", scale, '0'); String.format("%0" + scale + "d", 0);
            format += "." + Strings.repeat("0", scale);
        }
        return new DecimalFormat(format + "%").format(value);
    }

    /**
     * 数字格式化
     *
     * @param obj
     * @return
     */
    public static String format(Object obj) {
        return format(obj, "###,###.###");
    }

    /**
     * 数字格式化
     *
     * @param obj
     * @param format
     * @return
     */
    public static String format(Object obj, String format) {
        NumberFormat fmt = new DecimalFormat(format);
        if (obj instanceof CharSequence) {
            String str = obj.toString().replace(",", "");
            if (str.endsWith("%")) {
                str = str.substring(0, str.length() - 1);
                return fmt.format(Double.parseDouble(str)) + "%";
            } else {
                return fmt.format(Double.parseDouble(str));
            }
        } else {
            return fmt.format(obj);
        }
    }

    /**
     * Returns a string value of double
     * 
     * @param d      the double value
     * @param scale  the scale
     * @return a string
     */
    public static String format(double d, int scale) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(scale);
        nf.setGroupingUsed(false);
        return nf.format(d);
    }

    /**
     * 区间取值
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static int bounds(Integer value, int min, int max) {
        if (value == null || value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public static int sum(Integer a, Integer b) {
        return defaultIfNull(a, 0) + defaultIfNull(b, 0);
    }

    public static long sum(Long a, Long b) {
        return defaultIfNull(a, 0L) + defaultIfNull(b, 0L);
    }

    public static double sum(Double a, Double b) {
        return defaultIfNull(a, 0.0D) + defaultIfNull(b, 0.0D);
    }

    /**
     * 分片
     *
     * <pre>
     *   slice(0 , 2)  ->  [0, 0]
     *   slice(2 , 3)  ->  [1, 1, 0]
     *   slice(3 , 1)  ->  [3]
     *   slice(9 , 3)  ->  [3, 3, 3]
     *   slice(10, 3)  ->  [4, 3, 3]
     *   slice(11, 3)  ->  [4, 4, 3]
     *   slice(12, 3)  ->  [4, 4, 4]
     * </pre>
     *
     * @param quantity
     * @param segment
     * @return
     */
    public static int[] slice(int quantity, int segment) {
        int[] result = new int[segment];
        int quotient = quantity / segment;
        int remainder = quantity % segment;
        int moreValue = quotient + 1;
        Arrays.fill(result, 0, remainder, moreValue);
        Arrays.fill(result, remainder, segment, quotient);
        return result;
    }

    /**
     * Partition the number
     * <pre>
     *   partition( 0, 2)  ->  [(0, 0)]
     *   partition( 2, 3)  ->  [(0, 0), (1, 1)]
     *   partition( 3, 1)  ->  [(0, 2)]
     *   partition( 9, 3)  ->  [(0, 2), (3, 5), (6, 8)]
     *   partition(10, 3)  ->  [(0, 3), (4, 6), (7, 9)]
     *   partition(11, 3)  ->  [(0, 3), (4, 7), (8, 10)]
     *   partition(12, 3)  ->  [(0, 3), (4, 7), (8, 11)]
     * </pre>
     *
     * @param number the number
     * @param size   the size
     * @return array
     */
    public static List<Tuple2<Integer, Integer>> partition(int number, int size) {
        Assert.isTrue(number >= 0, "Number must be greater than 0.");
        Assert.isTrue(size > 0, "Size must be greater than 0.");
        if (number == 0) {
            return Collections.singletonList(Tuple2.of(0, 0));
        }

        List<Tuple2<Integer, Integer>> result = new ArrayList<>(size);
        int last = -1;
        for (int a : slice(number, size)) {
            if (a == 0) {
                break;
            }
            result.add(Tuple2.of(last += 1, last += a - 1));
        }

        return result;
    }

    /**
     * Split the bill for coupon amount<br/>
     * split(new int[]{249, 249, 249, 3}, 748)  -> [249, 249, 248, 2]
     *
     * @param bills the bills
     * @param value the coupon amount value
     * @return split result
     */
    public static int[] split(int[] bills, int value) {
        int total = IntStream.of(bills).sum();
        if (total < value) {
            throw new IllegalArgumentException("Total bill amount[" + total + "] cannot less than coupon amount[" + value + "]");
        }

        int[] result = new int[bills.length];
        if (bills.length == 0 || value == 0) {
            return result;
        }

        float rate;
        int i = 0, n = bills.length - 1;
        for (; i < n; i++) {
            // rate <= 1.0
            rate = value / (float) total;

            // 不能用Math.round：面值为748分钱的券 去平摊账单 [249, 249, 249, 3]，最后金额为3分钱的账单项要平摊掉4分钱
            //result[i] = Math.min(Math.round(bills[i] * rate), value);
            // 因为result[i]是ceil后的结果，所以按比率上来算value减得会更多，即rate只会递减，所以不会出现溢出(后面的费用项不够抵扣)的情况
            result[i] = Math.min((int) Math.ceil(bills[i] * rate), value);
            value -= result[i];
            total -= bills[i];

            if (value == 0) {
                break;
            }
        }

        // the last bill item
        if (i == n) {
            result[i] = value;
        }
        return result;
    }

    /**
     * Returns the Long object is equals the Integer object
     * 
     * @param a the Long a
     * @param b the Integer b
     * @return if is equals then return {@code true}
     */
    public static boolean equals(Long a, Integer b) {
        if (a == null && b == null) {
            return true;
        }
        return a != null && b != null && a.longValue() == b.intValue();
    }

    /**
     * To upper hex string and remove prefix 0
     *
     * @param num the BigInteger
     * @return upper hex string
     */
    public static String toHex(BigInteger num) {
        String hex = Hex.encodeHexString(num.toByteArray(), false);
        if (hex.matches("^0+$")) {
            return "0";
        }
        return hex.replaceFirst("^0*", "");
    }

    // --------------------------------------------------------------------------金额汉化
    private static final String[] CN_UPPER_NUMBER = {
        "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"
    };
    private static final String[] CN_UPPER_MONETARY_UNIT = {
        "分", "角", "元", "拾", "佰", "仟", "万", "拾", "佰",
        "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟"
    };
    private static final BigDecimal MAX_VALUE = new BigDecimal("9999999999999999.995");

    /**
     * 金额汉化（单位元）
     *
     * @param amount
     * @return a string of chineseize amount
     */
    public static String chinesize(BigDecimal amount) {
        if (amount.compareTo(MAX_VALUE) >= 0) {
            throw new IllegalArgumentException("The amount value too large.");
        }
        int signum = amount.signum(); // 正负数：0,1,-1
        if (signum == 0) {
            return "零元整";
        }

        // * 100
        long number = amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP)
                            .abs().longValue();
        int scale = (int) (number % 100), numIndex;
        if (scale == 0) {
            numIndex = 2;
            number = number / 100;
        } else if (scale % 10 == 0) {
            numIndex = 1;
            number = number / 10;
        } else {
            numIndex = 0;
        }
        boolean getZero = numIndex != 0;

        StringBuilder builder = new StringBuilder();
        for (int zeroSize = 0, numUnit; number > 0; number = number / 10, ++numIndex) {
            numUnit = (int) (number % 10); // get the last number
            if (numUnit > 0) {
                if ((numIndex == 9) && (zeroSize >= 3)) {
                    builder.insert(0, CN_UPPER_MONETARY_UNIT[6]);
                }
                if ((numIndex == 13) && (zeroSize >= 3)) {
                    builder.insert(0, CN_UPPER_MONETARY_UNIT[10]);
                }
                builder.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                builder.insert(0, CN_UPPER_NUMBER[numUnit]);
                getZero = false;
                zeroSize = 0;
            } else {
                ++zeroSize;
                if (!getZero) {
                    builder.insert(0, CN_UPPER_NUMBER[numUnit]);
                }
                if (numIndex == 2) {
                    if (number > 0) {
                        builder.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                    }
                } else if ( (((numIndex - 2) & 0x03) == 0) && (number % 1000 > 0) ) {
                    builder.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                }
                getZero = true;
            }
        }

        if (signum == -1) {
            builder.insert(0, "负"); // 负数
        }

        if (scale == 0) {
            builder.append("整"); // 整数
        }
        return builder.toString();
    }

    // -------------------------------------------------------private methods
    private static Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            String val = obj.toString();
            return val.indexOf('.') == -1
                    ? Long.parseLong(val)
                    : (long) Double.parseDouble(val);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Double parseDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
