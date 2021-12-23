/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.util;

import java.text.AttributedCharacterIterator;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

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

    public static final FastDateFormat PATTERN01 = FastDateFormat.getInstance("yyyyMM");
    public static final FastDateFormat PATTERN02 = FastDateFormat.getInstance("yyyyMMdd");
    public static final FastDateFormat PATTERN03 = FastDateFormat.getInstance("yyyyMMddHHmmss");
    public static final FastDateFormat PATTERN04 = FastDateFormat.getInstance("yyyyMMddHHmmssSSS");

    public static final FastDateFormat PATTERN11 = FastDateFormat.getInstance("yyyy-MM");
    public static final FastDateFormat PATTERN12 = FastDateFormat.getInstance("yyyy-MM-dd");
    public static final FastDateFormat PATTERN13 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    public static final FastDateFormat PATTERN14 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");
    public static final FastDateFormat PATTERN15 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static final FastDateFormat PATTERN21 = FastDateFormat.getInstance("yyyy/MM");
    public static final FastDateFormat PATTERN22 = FastDateFormat.getInstance("yyyy/MM/dd");
    public static final FastDateFormat PATTERN23 = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss");
    public static final FastDateFormat PATTERN24 = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss.SSS");
    public static final FastDateFormat PATTERN25 = FastDateFormat.getInstance("yyyy/MM/dd'T'HH:mm:ss.SSSZ");

    public static final FastDateFormat PATTERN31 = FastDateFormat.getInstance("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

    /**
     * The default date format with yyyy-MM-dd HH:mm:ss
     */
    public static final WrappedFastDateFormat DEFAULT = new WrappedFastDateFormat(Dates.DEFAULT_DATE_FORMAT);

    /**
     * 格式化器
     */
    private final FastDateFormat format;

    /**
     * 时间格式是否严格
     */
    private final boolean strict;

    private final int patternLength;
    private final Calendar calendar;
    private final NumberFormat numberFormat;

    public WrappedFastDateFormat(String pattern) {
        this(FastDateFormat.getInstance(pattern), false);
    }

    public WrappedFastDateFormat(String pattern, boolean strict) {
        this(pattern, null, null, strict);
    }

    public WrappedFastDateFormat(String pattern, TimeZone timeZone, Locale locale) {
        this(pattern, timeZone, locale, false);
    }

    public WrappedFastDateFormat(String pattern, TimeZone timeZone, 
                                 Locale locale, boolean strict) {
        this(FastDateFormat.getInstance(pattern, timeZone, locale), strict);
    }

    public WrappedFastDateFormat(FastDateFormat format, boolean strict) {
        this.format = format;
        this.strict = strict;

        this.patternLength = format.getPattern().length();
        this.calendar = Calendar.getInstance(format.getTimeZone(), format.getLocale());
        this.numberFormat = NumberFormat.getIntegerInstance(format.getLocale());
        this.numberFormat.setGroupingUsed(false);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, 
                               FieldPosition fieldPosition) {
        return format.format(date, toAppendTo, fieldPosition);
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

        if (strict) {
            return format.parse(source, pos);
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

        if (strict) {
            return format.parse(source);
        }

        int length = source.length();
        switch (length) {
            case  6: return PATTERN01.parse(source);
            case  7: return (isCrossbar(source) ? PATTERN11 : PATTERN21).parse(source);
            case  8: return PATTERN02.parse(source);
            case 10:
                char c = source.charAt(4);
                if (c == '-') {
                    return PATTERN12.parse(source);
                } else if (c == '/') {
                    return PATTERN22.parse(source);
                } else if (DATE_TIMESTAMP_PATTERN.matcher(source).matches()) {
                    // a long string(length 10) of unix timestamp(1640966400)
                    return new Date(Long.parseLong(source) * 1000);
                }
                break;
            case 13:
                // a long string(length 13) of mills unix timestamp(1640966400000)
                if (DATE_TIMESTAMP_PATTERN.matcher(source).matches()) {
                    return new Date(Long.parseLong(source));
                }
                break;
            case 14: return PATTERN03.parse(source);
            case 19: return (isCrossbar(source) ? PATTERN13 : PATTERN23).parse(source);
            case 17: return PATTERN04.parse(source);
            case 23: return (isCrossbar(source) ? PATTERN14 : PATTERN24).parse(source);
            case 28: return (isCST(source) ? PATTERN31 : isCrossbar(source) ? PATTERN15 : PATTERN25).parse(source);
            default: break;
        }
        if (patternLength == length) {
            return format.parse(source);
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
        return format.hashCode();
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
        return this.format.equals(other.format) && this.strict == other.strict;
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return format.formatToCharacterIterator(obj);
    }

    @Override
    public TimeZone getTimeZone() {
        return format.getTimeZone();
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
        return new WrappedFastDateFormat((FastDateFormat) format.clone(), strict);
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

    private static boolean isCrossbar(String str) {
        return str.charAt(4) == '-';
    }

    private static boolean isCST(String str) {
        return DATE_TO_STRING_PATTERN.matcher(str).matches();
    }
}
