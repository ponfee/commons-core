package code.ponfee.commons.parser;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.util.Dates;

/**
 * https://www.cnblogs.com/qcfeng/p/7553500.html
 * 
 * Date user defined functions
 * 
 * @author Ponfee
 */
public final class DateUDF {

    private static final Pattern PATTERN = Pattern.compile("[\\-+]?\\d+[YyMDdHhmWw]");

    // -------------------------------------------------now
    public static String now(String format) {
        return format(now(), format);
    }

    public static String now(String format, String offset) {
        return format(compute(now(), offset), format);
    }

    // -------------------------------------------------day
    public static String startDay(String format) {
        return format(Dates.startOfDay(now()), format);
    }

    public static String startDay(String format, String offset) {
        return format(Dates.startOfDay(compute(now(), offset)), format);
    }

    public static String endDay(String format) {
        return format(Dates.endOfDay(now()), format);
    }

    public static String endDay(String format, String offset) {
        return format(Dates.endOfDay(compute(now(), offset)), format);
    }

    // -------------------------------------------------week
    public static String startWeek(String format) {
        return format(Dates.startOfWeek(now()), format);
    }

    public static String startWeek(String format, String offset) {
        return format(Dates.startOfWeek(compute(now(), offset)), format);
    }

    public static String endWeek(String format) {
        return format(Dates.endOfWeek(now()), format);
    }

    public static String endWeek(String format, String offset) {
        return format(Dates.endOfWeek(compute(now(), offset)), format);
    }

    // -------------------------------------------------month
    public static String startMonth(String format) {
        return format(Dates.startOfMonth(now()), format);
    }

    public static String startMonth(String format, String offset) {
        return format(Dates.startOfMonth(compute(now(), offset)), format);
    }

    public static String endMonth(String format) {
        return format(Dates.endOfMonth(now()), format);
    }

    public static String endMonth(String format, String offset) {
        return format(Dates.endOfMonth(compute(now(), offset)), format);
    }

    // -------------------------------------------------year
    public static String startYear(String format) {
        return format(Dates.startOfYear(now()), format);
    }

    public static String startYear(String format, String offset) {
        return format(Dates.startOfYear(compute(now(), offset)), format);
    }

    public static String endYear(String format) {
        return format(Dates.endOfYear(now()), format);
    }

    public static String endYear(String format, String offset) {
        return format(Dates.endOfYear(compute(now(), offset)), format);
    }

    // -------------------------------------------------private methods
    private static Date now() {
        return new Date();
    }

    private static String format(Date date, String format) {
        if (date == null) {
            return null;
        } else if ("timestamp".equalsIgnoreCase(format)) {
            return String.valueOf(date.getTime());
        } else {
            return Dates.format(date, format);
        }
    }

    private static Date compute(Date dateTime, String offset) {
        if (!PATTERN.matcher(offset).matches()) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }

        int amount = Integer.parseInt(StringUtils.substring(offset, 0, -1));
        switch (offset.charAt(offset.length() - 1)) {
            case 'y':
            case 'Y':
                return Dates.plusYears(dateTime, amount);
            case 'M':
                return Dates.plusMonths(dateTime, amount);
            case 'w':
            case 'W':
                return Dates.plusWeeks(dateTime, amount);
            case 'd':
            case 'D':
                return Dates.plusDays(dateTime, amount);
            case 'H':
            case 'h':
                return Dates.plusHours(dateTime, amount);
            case 'm':
                return Dates.plusMinutes(dateTime, amount);
            default:
                throw new IllegalArgumentException("Invalid offset: " + offset);
        }
    }

}
