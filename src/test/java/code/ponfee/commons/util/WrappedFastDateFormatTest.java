package code.ponfee.commons.util;

import java.text.ParseException;
import java.util.Date;

public class WrappedFastDateFormatTest {

    public static void main(String[] args) throws ParseException {
        WrappedFastDateFormat format = WrappedFastDateFormat.DEFAULT;
        Date date = new Date();

        System.out.println(format.parse(WrappedFastDateFormat.PATTERN02A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN03A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN04A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN05A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN06A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN07A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN08A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN09A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN10A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN11A.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN03B.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN05B.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN07B.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN09B.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN10B.format(date)));
        System.out.println(format.parse(WrappedFastDateFormat.PATTERN11B.format(date)));
        System.out.println(format.parse(String.valueOf(date.getTime())));
        System.out.println(format.parse(String.valueOf(date.getTime() / 1000)));
    }
}
