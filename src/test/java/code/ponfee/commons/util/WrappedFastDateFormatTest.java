package code.ponfee.commons.util;

import java.text.ParseException;
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
    }
}
