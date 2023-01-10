/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import javax.annotation.concurrent.ThreadSafe;
import java.text.AttributedCharacterIterator;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Wrapper the org.apache.commons.lang3.time.FastDateFormat
 * <p>unix timestamp只支持对10位(秒)和13位(毫秒)做解析</p>
 * 
 * @ThreadSafe
 * 
 * @author Ponfee
 */
@ThreadSafe
public class WrappedFastDateFormat extends DateFormat {

    private static final long serialVersionUID = 6837172676882367405L;

    /**
     * For {@link Date#toString()} "EEE MMM dd HH:mm:ss zzz yyyy" format
     */
    public static final Pattern DATE_TO_STRING_PATTERN = Pattern.compile("^(Sun|Mon|Tue|Wed|Thu|Fri|Sat) [A-Z][a-z]{2} \\d{2} \\d{2}:\\d{2}:\\d{2} CST \\d{4}$");

    /**
     * 日期时间戳：秒/毫秒
     */
    public static final Pattern DATE_TIMESTAMP_PATTERN = Pattern.compile("^0|[1-9]\\d*$");

    public static final FastDateFormat PATTERN_11 = FastDateFormat.getInstance("yyyyMM");
    public static final FastDateFormat PATTERN_12 = FastDateFormat.getInstance("yyyy-MM");
    public static final FastDateFormat PATTERN_13 = FastDateFormat.getInstance("yyyy/MM");

    public static final FastDateFormat PATTERN_21 = FastDateFormat.getInstance("yyyyMMdd");
    public static final FastDateFormat PATTERN_22 = FastDateFormat.getInstance("yyyy-MM-dd");
    public static final FastDateFormat PATTERN_23 = FastDateFormat.getInstance("yyyy/MM/dd");

    public static final FastDateFormat PATTERN_31 = FastDateFormat.getInstance("yyyyMMddHHmmss");
    public static final FastDateFormat PATTERN_32 = FastDateFormat.getInstance("yyyyMMddHHmmssSSS");

    public static final FastDateFormat PATTERN_41 = FastDateFormat.getInstance(Dates.DEFAULT_DATE_FORMAT);
    public static final FastDateFormat PATTERN_42 = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss");
    public static final FastDateFormat PATTERN_43 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");
    public static final FastDateFormat PATTERN_44 = FastDateFormat.getInstance("yyyy/MM/dd'T'HH:mm:ss");

    public static final FastDateFormat PATTERN_51 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");
    public static final FastDateFormat PATTERN_52 = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss.SSS");
    public static final FastDateFormat PATTERN_53 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public static final FastDateFormat PATTERN_54 = FastDateFormat.getInstance("yyyy/MM/dd'T'HH:mm:ss.SSS");

    public static final FastDateFormat PATTERN_61 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS'Z'");
    public static final FastDateFormat PATTERN_62 = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss.SSS'Z'");
    public static final FastDateFormat PATTERN_63 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final FastDateFormat PATTERN_64 = FastDateFormat.getInstance("yyyy/MM/dd'T'HH:mm:ss.SSS'Z'");

    public static final FastDateFormat PATTERN_71 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    public static final FastDateFormat PATTERN_72 = FastDateFormat.getInstance("yyyy/MM/dd'T'HH:mm:ss.SSSX");

    public static final FastDateFormat PATTERN_81 = FastDateFormat.getInstance("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

    /**
     * The default date format with yyyy-MM-dd HH:mm:ss
     */
    public static final WrappedFastDateFormat DEFAULT = new WrappedFastDateFormat(Dates.DEFAULT_DATE_FORMAT);

    /**
     * 兜底解析器
     */
    private final FastDateFormat backstopFormatter;

    private final int patternLength;
    private final Calendar calendar;
    private final NumberFormat numberFormat;

    public WrappedFastDateFormat(String pattern) {
        this(pattern, null, null);
    }

    public WrappedFastDateFormat(String pattern, TimeZone timeZone, Locale locale) {
        this(FastDateFormat.getInstance(pattern, timeZone, locale));
    }

    public WrappedFastDateFormat(FastDateFormat format) {
        this.backstopFormatter = format;

        this.patternLength = format.getPattern().length();
        this.calendar = Calendar.getInstance(format.getTimeZone(), format.getLocale());
        this.numberFormat = NumberFormat.getIntegerInstance(format.getLocale());
        this.numberFormat.setGroupingUsed(false);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return backstopFormatter.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        Objects.requireNonNull(pos);
        if (pos.getIndex() < 0) {
            throw new IllegalArgumentException("Invalid parse position: " + pos.getIndex());
        }
        if (StringUtils.isEmpty(source) || source.length() <= pos.getIndex()) {
            return null;
        }

        String date = source.substring(pos.getIndex());
        try {
            return parse(date);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid date format: " + source + ", " + pos.getIndex() + ", " + date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + source + ", " + pos.getIndex() + ", " + date, e);
        }
    }

    @Override
    public Date parse(String source) throws ParseException {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        int length = source.length();
        if (length >= 20 && source.endsWith("Z")) {
            if (length < 24) {
                source = padding(source) + "Z";
            }
            if (hasTSeparator(source)) {
                return (isCrossbar(source) ? PATTERN_63 : PATTERN_64).parse(source);
            } else {
                return (isCrossbar(source) ? PATTERN_61 : PATTERN_62).parse(source);
            }
        }

        switch (length) {
            case  6: return PATTERN_11.parse(source);
            case  7: return (isCrossbar(source) ? PATTERN_12 : PATTERN_13).parse(source);
            case  8: return PATTERN_21.parse(source);
            case 10:
                char separator = source.charAt(4);
                if (separator == '-') {
                    return PATTERN_22.parse(source);
                } else if (separator == '/') {
                    return PATTERN_23.parse(source);
                } else if (DATE_TIMESTAMP_PATTERN.matcher(source).matches()) {
                    // long string(length 10) of unix timestamp(e.g. 1640966400)
                    return new Date(Long.parseLong(source) * 1000);
                }
                break;
            case 13:
                // long string(length 13) of mills unix timestamp(e.g. 1640966400000)
                if (DATE_TIMESTAMP_PATTERN.matcher(source).matches()) {
                    return new Date(Long.parseLong(source));
                }
                break;
            case 14: return PATTERN_31.parse(source);
            case 19:
                if (hasTSeparator(source)) {
                    return (isCrossbar(source) ? PATTERN_43 : PATTERN_44).parse(source);
                } else {
                    return (isCrossbar(source) ? PATTERN_41 : PATTERN_42).parse(source);
                }
            case 17: return PATTERN_32.parse(source);
            case 23:
                if (hasTSeparator(source)) {
                    return (isCrossbar(source) ? PATTERN_53 : PATTERN_54).parse(source);
                } else {
                    return (isCrossbar(source) ? PATTERN_51 : PATTERN_52).parse(source);
                }
            case 26: 
            case 29:
                // 2021-12-31T17:01:01.000+08
                // 2021-12-31T17:01:01.000+08:00
                return (isCrossbar(source) ? PATTERN_71 : PATTERN_72).parse(source);
            case 28:
                if (isCST(source)) {
                    return PATTERN_81.parse(source);
                }
                break;
            default: break;
        }
        if (patternLength == length) {
            return backstopFormatter.parse(source);
        } else {
            throw new IllegalArgumentException("Invalid date format: " + source);
        }
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    @Override
    public Object parseObject(String source) throws ParseException {
        return parse(source);
    }

    @Override
    public int hashCode() {
        return backstopFormatter.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof WrappedFastDateFormat)) {
            return false;
        }

        WrappedFastDateFormat other = (WrappedFastDateFormat) obj;
        return this.backstopFormatter.equals(other.backstopFormatter);
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return backstopFormatter.formatToCharacterIterator(obj);
    }

    @Override
    public TimeZone getTimeZone() {
        return backstopFormatter.getTimeZone();
    }

    @Override
    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    @Override
    public boolean isLenient() {
        return calendar.isLenient();
    }

    @Override
    public Object clone() {
        return new WrappedFastDateFormat((FastDateFormat) backstopFormatter.clone());
    }

    // ------------------------------------------------------------------------unsupported
    @Override
    public void setTimeZone(TimeZone zone) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCalendar(Calendar newCalendar) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNumberFormat(NumberFormat newNumberFormat) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLenient(boolean lenient) {
        throw new UnsupportedOperationException();
    }

    // ------------------------------------------------------------------------package methods
    static boolean isCrossbar(String str) {
        return str.charAt(4) == '-';
    }

    // 'T' literal is the date and time separator
    static boolean hasTSeparator(String str) {
        return str.charAt(10) == 'T';
    }

    static boolean isCST(String str) {
        return DATE_TO_STRING_PATTERN.matcher(str).matches();
    }

    static String padding(String source) {
        // example: 2022/07/18T15:11:11Z, 2022/07/18T15:11:11.Z, 2022/07/18T15:11:11.1Z, 2022/07/18T15:11:11.13Z
        String[] array = source.split("[\\.Z]");
        return array[0] + "." + (array.length == 1 ? "000" : String.format("%03d", Integer.parseInt(array[1])));
    }

}
