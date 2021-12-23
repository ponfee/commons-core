package code.ponfee.commons.util;

import org.junit.Assert;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

public class WrappedFastDateFormatTest {

    public static void main(String[] args) throws ParseException {
        WrappedFastDateFormat format = WrappedFastDateFormat.DEFAULT;
        Date date = new Date();

        System.out.println(format.parse(WrappedFastDateFormat.PATTERN01.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN11.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN02.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN12.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN03.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN13.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN04.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN14.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN15.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN21.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN22.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN23.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN24.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN25.format(date)));
        System.out.println(format.parse(String.valueOf(date.getTime())));
        System.out.println(format.parse(String.valueOf(date.getTime() / 1000)));

        System.out.println("\n------------------------");
        System.out.println(WrappedFastDateFormat.PATTERN01.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN11.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN02.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN12.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN03.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN13.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN04.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN14.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN15.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN21.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN22.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN23.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN24.format(date));
        System.out.println(WrappedFastDateFormat.PATTERN25.format(date));

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
        System.out.println(WrappedFastDateFormat.PATTERN13.parse("2122-01-01 00:00:00", new ParsePosition(0)));
        System.out.println(WrappedFastDateFormat.PATTERN13.parse("2122-01-01 00:00:00", new ParsePosition(1)));
        //System.out.println(WrappedFastDateFormat.PATTERN13.parse("2122-01-01 00:00:00", null));
        //System.out.println(WrappedFastDateFormat.PATTERN13.parse("2122-01-01 00:00:00", new ParsePosition(-1)));
        System.out.println(WrappedFastDateFormat.DEFAULT.parse("x2122-01-01 00:00:00", new ParsePosition(1)));
        System.out.println(WrappedFastDateFormat.DEFAULT.parse("2122-01-01 00:00:00", new ParsePosition(0)));
        System.out.println(WrappedFastDateFormat.DEFAULT.parse("2122-01-01 00:00:00", new ParsePosition(1)));
    }
}
