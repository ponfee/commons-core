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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Wrapper the org.apache.commons.lang3.time.FastDateFormat
 * 
 * @author Ponfee
 */
public class WrappedFastDateFormat extends DateFormat {

    private static final long serialVersionUID = 6837172676882367405L;

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

    // thread-safe
    public static final WrappedFastDateFormat DEFAULT = new WrappedFastDateFormat("yyyy-MM-dd HH:mm:ss");

    private final FastDateFormat format;
    private final boolean strict;

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
        this.calendar = Calendar.getInstance(format.getTimeZone(), format.getLocale());

        this.numberFormat = NumberFormat.getIntegerInstance(format.getLocale());
        this.numberFormat.setGroupingUsed(false);
        this.strict = strict;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, 
                               FieldPosition fieldPosition) {
        return this.format.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        if (strict) {
            return this.format.parse(source, pos);
        }

        int length = source.length();
        if (length < 6) {
            return null;
        }
        switch (length) {
            case  6: return PATTERN01.parse(source, pos);
            case  7: return (isCrossbar(source) ? PATTERN11 : PATTERN21).parse(source, pos);
            case  8: return PATTERN02.parse(source, pos);
            case 10: 
                switch (source.charAt(4)) {
                    case '-': return PATTERN12.parse(source, pos);
                    case '/': return PATTERN22.parse(source, pos);
                    default: return new Date(Long.parseLong(source) * 1000); // a long string of seconds unix timestamp 
                }
            case 13: return new Date(Long.parseLong(source));  // a long string of mills unix timestamp 
            case 14: return PATTERN03.parse(source, pos);
            case 19: return (isCrossbar(source) ? PATTERN13 : PATTERN23).parse(source, pos);
            case 17: return PATTERN04.parse(source, pos);
            case 23: return (isCrossbar(source) ? PATTERN14 : PATTERN24).parse(source, pos);
            case 28: return (isCrossbar(source) ? PATTERN15 : PATTERN25).parse(source, pos);
            default: return this.format.parse(source, pos);
        }
    }

    @Override
    public Date parse(String source) throws ParseException {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        if (strict) {
            return this.format.parse(source);
        }

        int length = source.length();
        if (length < 6) {
            throw new ParseException("Invalid data format: " + source, 0);
        }
        switch (length) {
            case  6: return PATTERN01.parse(source);
            case  7: return (isCrossbar(source) ? PATTERN11 : PATTERN21).parse(source);
            case  8: return PATTERN02.parse(source);
            case 10: 
                switch (source.charAt(4)) {
                    case '-': return PATTERN12.parse(source);
                    case '/': return PATTERN22.parse(source);
                    default: return new Date(Long.parseLong(source) * 1000);
                }
            case 13: return new Date(Long.parseLong(source));
            case 14: return PATTERN03.parse(source);
            case 19: return (isCrossbar(source) ? PATTERN13 : PATTERN23).parse(source);
            case 17: return PATTERN04.parse(source);
            case 23: return (isCrossbar(source) ? PATTERN14 : PATTERN24).parse(source);
            case 28: return (isCrossbar(source) ? PATTERN15 : PATTERN25).parse(source);
            default: return this.format.parse(source);
        }
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return this.parse(source, pos);
    }

    @Override
    public Object parseObject(String source) throws ParseException {
        return this.parse(source);
    }

    @Override
    public int hashCode() {
        return this.format.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof WrappedFastDateFormat)) {
            return false;
        }

        return this.format.equals(((WrappedFastDateFormat) obj).format);
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return this.format.formatToCharacterIterator(obj);
    }

    @Override
    public TimeZone getTimeZone() {
        return this.format.getTimeZone();
    }

    @Override
    public Calendar getCalendar() {
        return this.calendar;
    }

    @Override
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    @Override
    public boolean isLenient() {
        return this.calendar.isLenient();
    }

    @Override
    public Object clone() {
        return new WrappedFastDateFormat(
            (FastDateFormat) this.format.clone(), this.strict
        );
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

}
