package code.ponfee.commons.math;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import com.google.common.primitives.Chars;

/**
 * Number utility
 * 
 * 十进制：10
 * 二进制：0B10
 * 八进制：010
 * 十六进制：0X10
 * 小数点：1e-9
 * 
 * @author Ponfee
 */
public final class Numbers {
    private Numbers() {}

    public static final Integer INTEGER_ZERO = 0;
    public static final int     INT_ZERO     = 0;
    public static final byte    BYTE_ZERO    = 0x00;
    public static final char    CHAR_ZERO    = '\u0000'; // equals '\0'

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
        Long value = toWrapLong(obj);
        return value == null ? defaultVal : value.byteValue();
    }

    public static Byte toWrapByte(Object obj) {
        Long value = toWrapLong(obj);
        return value == null ? null : value.byteValue();
    }

    // -----------------------------------------------------------------short convert
    public static short toShort(Object obj) {
        return toShort(obj, (short) 0);
    }

    public static short toShort(Object obj, short defaultVal) {
        Long value = toWrapLong(obj);
        return value == null ? defaultVal : value.shortValue();
    }

    public static Short toWrapShort(Object obj) {
        Long value = toWrapLong(obj);
        return value == null ? null : value.shortValue();
    }

    // -----------------------------------------------------------------int convert
    public static int toInt(Object obj) {
        return toInt(obj, 0);
    }

    public static int toInt(Object obj, int defaultVal) {
        Long value = toWrapLong(obj);
        return value == null ? defaultVal : value.intValue();
    }

    public static Integer toWrapInt(Object obj) {
        Long value = toWrapLong(obj);
        return value == null ? null : value.intValue();
    }

    // -----------------------------------------------------------------long convert
    public static long toLong(Object obj) {
        return toLong(obj, 0L);
    }

    public static long toLong(Object obj, long defaultVal) {
        Long value = toWrapLong(obj);
        return value == null ? defaultVal : value;
    }

    public static Long toWrapLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
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

    // -----------------------------------------------------------------float convert
    public static float toFloat(Object obj) {
        return toFloat(obj, 0.0F);
    }

    public static float toFloat(Object obj, float defaultVal) {
        Double value = toWrapDouble(obj);
        return value == null ? defaultVal : value.floatValue();
    }

    public static Float toWrapFloat(Object obj) {
        Double value = toWrapDouble(obj);
        return value == null ? null : value.floatValue();
    }

    // -----------------------------------------------------------------double convert
    public static double toDouble(Object obj) {
        return toDouble(obj, 0.0D);
    }

    public static double toDouble(Object obj, double defaultVal) {
        Double value = toWrapDouble(obj);
        return value == null ? defaultVal : value;
    }

    public static Double toWrapDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
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
        Double val = toDouble(value);

        if (scale < 0) {
            return val;
        }

        return BigDecimal.valueOf(val)
                .setScale(scale, BigDecimal.ROUND_HALF_UP)
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
            .setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
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
            .setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
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
            format += "." + StringUtils.leftPad("", scale, '0');
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
     * @param quantity
     * @param segment
     * @return
     */
    public static int[] slice(int quantity, int segment) {
        int[] array = new int[segment];
        int remainder = quantity % segment;
        int quotient = quantity / segment;
        Arrays.fill(array, 0, remainder, quotient + 1);
        Arrays.fill(array, remainder, segment, quotient);
        return array;
    }

    /**
     * Returns the two Long object is equals
     * 
     * @param a the Long a
     * @param b the Long b
     * @return if is equals then return {@code true}
     */
    public static boolean equals(Long a, Long b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * Returns the Long object is equals the Integer object
     * 
     * @param a the Long a
     * @param b the Integer b
     * @return if is equals then return {@code true}
     */
    public static boolean equals(Long a, Integer b) {
        return a != null && b != null 
            && a.longValue() == b.intValue();
    }

    /**
     * Returns the two Integer object is equals
     * 
     * @param a the Integer a
     * @param b the Integer b
     * @return if is equals then return {@code true}
     */
    public static boolean equals(Integer a, Integer b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * Returns the two Double object is equals
     * 
     * @param a the Double a
     * @param b the Double b
     * @return if is equals then return {@code true}
     */
    public static boolean equals(Double a, Double b) {
        return (a == b) || (a != null && a.equals(b));
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
        long number = amount.movePointRight(2).setScale(0, BigDecimal.ROUND_HALF_UP)
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

}
