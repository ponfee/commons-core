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
    private static final FastDateFormat PARSER01 = FastDateFormat.getInstance("yyyy");
    private static final FastDateFormat PARSER02 = FastDateFormat.getInstance("yyyyMM");
    private static final FastDateFormat PARSER03 = FastDateFormat.getInstance("yyyy-MM");
    private static final FastDateFormat PARSER04 = FastDateFormat.getInstance("yyyyMMdd");
    private static final FastDateFormat PARSER05 = FastDateFormat.getInstance("yyyy-MM-dd");
    private static final FastDateFormat PARSER06 = FastDateFormat.getInstance("yyyyMMddHHmmss");
    private static final FastDateFormat PARSER07 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    private static final FastDateFormat PARSER08 = FastDateFormat.getInstance("yyyyMMddHHmmssSSS");
    private static final FastDateFormat PARSER09 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");
    private static final FastDateFormat PARSER10 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final FastDateFormat PARSER11 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

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
        if (StringUtils.isBlank(source)) {
            return null;
        }

        if (strict) {
            return this.format.parse(source, pos);
        }

        switch (source.length()) {
            case  4: return PARSER01.parse(source, pos);
            case  6: return PARSER02.parse(source, pos);
            case  7: return PARSER03.parse(source, pos);
            case  8: return PARSER04.parse(source, pos);
            case 10: return PARSER05.parse(source, pos);
            case 14: return PARSER06.parse(source, pos);
            case 19: return PARSER07.parse(source, pos);
            case 17: return PARSER08.parse(source, pos);
            case 23: return PARSER09.parse(source, pos);
            case 28: return PARSER10.parse(source, pos);
            case 24: return PARSER11.parse(source, pos);
            default: return this.format.parse(source, pos);
        }
    }

    @Override
    public Date parse(String source) throws ParseException {
        if (StringUtils.isBlank(source)) {
            return null;
        }

        if (strict) {
            return this.format.parse(source);
        }

        switch (source.length()) {
            case  4: return PARSER01.parse(source);
            case  6: return PARSER02.parse(source);
            case  7: return PARSER03.parse(source);
            case  8: return PARSER04.parse(source);
            case 10: return PARSER05.parse(source);
            case 14: return PARSER06.parse(source);
            case 19: return PARSER07.parse(source);
            case 17: return PARSER08.parse(source);
            case 23: return PARSER09.parse(source);
            case 28: return PARSER10.parse(source);
            case 24: return PARSER11.parse(source);
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
        return this.format.equals(obj);
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
        return this; // return new WrappedFastDateFormat((FastDateFormat) this.format.clone());
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

}
