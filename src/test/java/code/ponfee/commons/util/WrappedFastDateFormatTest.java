package code.ponfee.commons.util;

import org.junit.Assert;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

public class WrappedFastDateFormatTest {

    public static void main(String[] args) throws ParseException {
        WrappedFastDateFormat format = WrappedFastDateFormat.DEFAULT;
        Date date = new Date();

        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_11.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_12.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_21.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_22.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_31.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_41.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_32.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_51.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_63.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_13.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_23.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_42.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_52.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN_64.format(date)));
        System.out.println(format.parse(String.valueOf(date.getTime())));
        System.out.println(format.parse(String.valueOf(date.getTime() / 1000)));

        System.out.println("\n------------------------");
        System.out.println(WrappedFastDateFormat.PATTERN_11.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_12.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_21.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_22.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_31.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_41.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_32.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_51.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_63.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_13.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_23.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_42.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_52.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN_64.format(date));

        System.out.println("\n------------------------");
        Assert.assertTrue(WrappedFastDateFormat.DATE_TO_STRING_PATTERN.matcher("Sat Jun 01 22:36:21 CST 2019").matches());
        Assert.assertFalse(WrappedFastDateFormat.DATE_TO_STRING_PATTERN.matcher("Sat Jun 0122:36:21 CST 2019").matches());
        Assert.assertFalse(WrappedFastDateFormat.DATE_TO_STRING_PATTERN.matcher("sat Jun 01 22:36:21 CST 2019").matches());
        Assert.assertFalse(WrappedFastDateFormat.DATE_TO_STRING_PATTERN.matcher("Sat Jun 01 22:36:21 DST 2019").matches());
        Assert.assertFalse(WrappedFastDateFormat.DATE_TO_STRING_PATTERN.matcher("Sat jun 01 22:36:21 CST 2019").matches());
        Assert.assertFalse(WrappedFastDateFormat.DATE_TO_STRING_PATTERN.matcher("Sat Jun 01 22:36:21CST 2019").matches());
        System.out.println(WrappedFastDateFormat.DEFAULT.parse("Sat Jun 01 22:36:21 CST 2019", new ParsePosition(0)));
        System.out.println(format.parse("Sat Jun 01 22:36:21 CST 2019"));
        System.out.println(format.parse("2020-12-01 10:33:06"));
        System.out.println(format.parse("1644894528086"));
        System.out.println(format.parse("1644894528"));
        System.out.println(new WrappedFastDateFormat("yyyy").parse("2022"));
        System.out.println(WrappedFastDateFormat.DEFAULT.format(new Date(0)));
        
        Assert.assertTrue(WrappedFastDateFormat.DATE_TIMESTAMP_PATTERN.matcher("0").matches());
        Assert.assertTrue(WrappedFastDateFormat.DATE_TIMESTAMP_PATTERN.matcher("1").matches());
        Assert.assertTrue(WrappedFastDateFormat.DATE_TIMESTAMP_PATTERN.matcher("9").matches());
        Assert.assertTrue(WrappedFastDateFormat.DATE_TIMESTAMP_PATTERN.matcher("1644894528").matches());
        Assert.assertFalse(WrappedFastDateFormat.DATE_TIMESTAMP_PATTERN.matcher("01644894528").matches());

        System.out.println("\n------------------------");
        System.out.println(WrappedFastDateFormat.PATTERN_41.parse("2122-01-01 00:00:00", new ParsePosition(0)));
        System.out.println(WrappedFastDateFormat.PATTERN_41.parse("2122-01-01 00:00:00", new ParsePosition(1)));
        //System.out.println(WrappedFastDateFormat.PATTERN13.parse("2122-01-01 00:00:00", null));
        //System.out.println(WrappedFastDateFormat.PATTERN13.parse("2122-01-01 00:00:00", new ParsePosition(-1)));
        System.out.println(WrappedFastDateFormat.DEFAULT.parse("x2122-01-01 00:00:00", new ParsePosition(1)));
        System.out.println(WrappedFastDateFormat.DEFAULT.parse("2122-01-01 00:00:00", new ParsePosition(0)));
        System.out.println(WrappedFastDateFormat.DEFAULT.parse("2122-01-01 00:00:00", new ParsePosition(1)));
    }
}
