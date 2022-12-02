package code.ponfee.commons.schema;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.date.JavaUtilDateFormat;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Data type enum
 * 
 * If the data value cannot convert the target type then fix is null
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public enum DataType {

    BOOLEAN("布尔") {
        @Override
        protected <T> T parseObject0(Object value) {
            if (value instanceof Boolean) {
                return (T) value;
            }
            return (T) BOOLEAN_MAPPING.get(value.toString());
        }
    },
    DECIMAL("小数") {
        @Override
        protected <T> T parseObject0(Object value) {
            return (T) Numbers.toWrapDouble(value);
        }
    },
    INTEGER("整数") {
        @Override
        protected <T> T parseObject0(Object value) {
            return (T) Numbers.toWrapLong(value);
        }
    },
    STRING("字符串") {
        @Override
        protected <T> T parseObject0(Object value) {
            return (T) value.toString();
        }

        @Override
        protected String toString0(Object value) {
            return value.toString();
        }

        @Override
        public boolean test0(Object value) {
            return true;
        }
    },
    DATE("日期") {
        private final JavaUtilDateFormat format = new JavaUtilDateFormat("yyyy-MM-dd");

        @Override
        protected <T> T parseObject0(Object value) {
            return (T) parseToDate(this.format, value);
        }

        @Override
        protected String convert0(Object value) {
            return toString0(value);
        }

        @Override
        protected String toString0(Object value) {
            return dateToString(this.format, value);
        }
    },
    DATE_TIME("日期时间") {

        @Override
        protected <T> T parseObject0(Object value) {
            return (T) parseToDate(JavaUtilDateFormat.DEFAULT, value);
        }

        @Override
        protected String convert0(Object value) {
            return toString0(value);
        }

        @Override
        protected String toString0(Object value) {
            return dateToString(JavaUtilDateFormat.DEFAULT, value);
        }
    },
    TIMESTAMP("时间戳（毫秒）") {
        @Override
        protected <T> T parseObject0(Object value) {
            if (value instanceof Date) {
                return (T) (Long) ((Date) value).getTime();
            }
            return (T) Numbers.toWrapLong(value);
        }
    },

    ;

    private final String description;

    DataType(String description) {
        this.description = description;
    }

    protected abstract <T> T parseObject0(Object value);

    protected String toString0(Object value) {
        return Objects.toString(parseObject0(value), null);
    }

    /**
     * Default is call inner method {@link #parseObject0(Object)}, <P>
     * but {@link #DATE} and {@link #DATE_TIME} override this 
     * method for convert a string with date format<P>
     * 
     * @param value the vlaue
     * @return an object of convert result
     */
    protected <T> T convert0(Object value) {
        return parseObject0(value);
    }

    protected boolean test0(Object value) {
        return parseObject0(value) != null;
    }

    // -----------------------------------------------------------public methods
    public final <T> T parseObject(Object value) {
        return value == null ? null : parseObject0(value);
    }

    public final String toString(Object value) {
        return value == null ? null : toString0(value);
    }

    /**
     * Like parseObject, except {@link #DATE} or {@link #DATE_TIME} convert to date format string
     * 
     * @param value the value
     * @return an target object, except {@link #DATE} or {@link #DATE_TIME} is a string of date format
     */
    public final Object convert(Object value) {
        return value == null ? null : ObjectUtils.defaultIfNull(convert0(value), value);
    }

    public final boolean test(Object value) {
        // allow null(empty) value
        if (value == null) {
            return true;
        }
        if ((value instanceof String) && StringUtils.isEmpty((String) value)) {
            return true;
        }
        return test0(value);
    }

    public String description() {
        return this.description;
    }

    public static DataType of(String name) {
        for (DataType dt : DataType.values()) {
            if (dt.name().equalsIgnoreCase(name)) {
                return dt;
            }
        }
        return STRING;
    }

    // -------------------------------------------------------------------------detect data type
    private static final Map<String, Boolean> BOOLEAN_MAPPING = ImmutableMap.<String, Boolean> builder()
        .put("TRUE", Boolean.TRUE)
        .put("True", Boolean.TRUE)
        .put("true", Boolean.TRUE)
        .put("FALSE", Boolean.FALSE)
        .put("False", Boolean.FALSE)
        .put("false", Boolean.FALSE)
        .build();

    private static final Pattern PATTERN_INTEGER = Pattern.compile("^[-+]?(([1-9]\\d*)|0)$");
    private static final Pattern PATTERN_DECIMAL = Pattern.compile("^[-+]?(([1-9]\\d*)|0)\\.\\d+$");

    public static DataType detect(String value) {
        if (StringUtils.isBlank(value)) {
            return STRING;
        }

        if (BOOLEAN_MAPPING.containsKey(value)) {
            return BOOLEAN;
        }

        if (PATTERN_INTEGER.matcher(value).matches() && INTEGER.parseObject0(value) != null) {
            return INTEGER;
        }

        if (PATTERN_DECIMAL.matcher(value).matches() && DECIMAL.parseObject0(value) != null) {
            return DECIMAL;
        }

        if (value.length() == 10 && DATE.parseObject0(value) != null) {
            return DATE;
        }

        if (value.length() == 19 && DATE_TIME.parseObject0(value) != null) {
            return DATE_TIME;
        }

        return STRING;
    }

    public static DataType ofDatabaseType(int type) {
        switch (type) {

            case Types.DATE:
                return DATE_TIME;
            case Types.TIMESTAMP:
                return TIMESTAMP;

            case Types.BIT:
            case Types.BOOLEAN:
                return BOOLEAN;

            case Types.REAL:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return INTEGER;

            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.DECIMAL:
                return DECIMAL;

            default:
                // VARCHAR, CHAR, NVARCHAR, NCHAR, LONGVARCHAR, LONGNVARCHAR
                return STRING;
        }
    }

    // ---------------------------------------------------------------------private methods
    private static String dateToString(DateFormat format, Object value) {
        if (value instanceof Date) {
            return format.format((Date) value);
        }
        String text = value.toString();
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return format.format(format.parse(text));
        } catch (ParseException ignored) {
            return null;
        }
    }

    private static Date parseToDate(DateFormat format, Object value) {
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        String text = value.toString();
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return format.parse(text);
        } catch (ParseException ignored) {
            return null;
        }
    }

}
