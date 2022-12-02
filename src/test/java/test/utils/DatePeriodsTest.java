package test.utils;

import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.date.DatePeriods;
import code.ponfee.commons.date.Dates;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class DatePeriodsTest {

    @Test
    public void test1() {
        LocalDateTime date = new LocalDateTime(new Date());
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss SSS");
        System.out.println(date.toString(format));
        System.out.println("-------------------\n");

        System.out.println(date.millisOfDay().withMinimumValue().toString(format));
        System.out.println(date.withMillisOfDay(0).toString(format));
        System.out.println("-------------------\n");

        System.out.println(date.millisOfSecond().withMinimumValue().toString(format));
        System.out.println(date.withMillisOfSecond(0).toString(format));
        System.out.println("-------------------\n");

        System.out.println(date.secondOfMinute().withMinimumValue().toString(format));
        System.out.println(date.withSecondOfMinute(0).toString(format));
        System.out.println("-------------------\n");

        System.out.println(date.minuteOfHour().withMinimumValue().toString(format));
        System.out.println(date.withMinuteOfHour(0).toString(format));
        System.out.println("-------------------\n");

        System.out.println(date.hourOfDay().withMinimumValue().toString(format));
        System.out.println(date.withHourOfDay(0).toString(format));
    }
    
    @Test
    public void test2() {
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        Date origin = Dates.toDate("2018-10-21 12:23:32.000", format);
        Date target = Dates.toDate("2018-10-29 12:23:32.000", format);
        System.out.println();
        
        System.out.println(DatePeriods.HOURLY.next(origin, target, 2, 1));
        System.out.println(DatePeriods.HOURLY.next(origin, Dates.toDate("2018-10-29 14:23:32.000", format), 2, 1));
        System.out.println(DatePeriods.HOURLY.next(Dates.toDate("2018-10-29 14:23:32.000", format), 2, 0));
        System.out.println(DatePeriods.HOURLY.next(origin, Dates.plusMillis(Dates.toDate("2018-10-29 14:23:32.000", format),-1), 2, 1));
        System.out.println(DatePeriods.HOURLY.next(origin, Dates.plusMillis(Dates.toDate("2018-10-29 14:23:32.000", format),1), 2, 1));
        System.out.println(DatePeriods.HOURLY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.DAILY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.WEEKLY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.MONTHLY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.QUARTERLY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.SEMIANNUAL.next(origin, target, 7, 31));
        System.out.println(DatePeriods.ANNUAL.next(origin, target, 7, 31));
        
    }

    @Test
    public void test3() {
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        Date origin = Dates.toDate("2017-10-21 12:23:32.000", format);
        Date begin = Dates.toDate("2018-10-21 11:23:32.000", format);
        int step = 3, next = 1;
        System.out.println(DatePeriods.HOURLY.next(origin, begin, step, 0));
        System.out.println();
        DatePeriods.Segment interval = DatePeriods.HOURLY.next(begin, step, next);
        System.out.println(interval);
        int i = 4;
        while (i-- > 0) {
            begin = interval.begin();
            interval = DatePeriods.HOURLY.next(begin, step, next);
            System.out.println(interval);
        }
    }

    @Test
    public void testDateMax() {
        Date a = Dates.toDate("2020-10-12 12:34:23");
        Date b = Dates.toDate("2020-10-12 12:34:26");

        Assert.assertEquals(null, Dates.max(null, null));
        Assert.assertEquals(null, Dates.min(null, null));

        Assert.assertEquals(a, Dates.max(a, null));
        Assert.assertEquals(a, Dates.max(null, a));

        Assert.assertEquals(a, Dates.min(a, null));
        Assert.assertEquals(a, Dates.min(null, a));

        Assert.assertEquals(b, Dates.max(a, b));
        Assert.assertEquals(b, Dates.max(b, a));

        Assert.assertEquals(a, Dates.min(a, b));
        Assert.assertEquals(a, Dates.min(b, a));
    }

    @Test
    public void testPeriods() {
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        Date origin = Dates.toDate("2018-10-21 06:23:32.000", format);
        Date begin = Dates.toDate("2018-10-21 11:54:12.000", format);
        int step = 3;
        DatePeriods.Segment segment = DatePeriods.HOURLY.next(origin, begin, step, 0);
        Assert.assertEquals("2018-10-21 09:23:32.000 ~ 2018-10-21 12:23:31.999", segment.toString());

        segment = DatePeriods.HOURLY.next(segment.begin(), segment.begin(), step, 0);
        Assert.assertEquals("2018-10-21 09:23:32.000 ~ 2018-10-21 12:23:31.999", segment.toString());

        segment = DatePeriods.HOURLY.next(segment.begin(), step, 0);
        Assert.assertEquals("2018-10-21 09:23:32.000 ~ 2018-10-21 12:23:31.999", segment.toString());

        segment = DatePeriods.HOURLY.next(segment.begin(), step, 1);
        Assert.assertEquals("2018-10-21 12:23:32.000 ~ 2018-10-21 15:23:31.999", segment.toString());

        segment = DatePeriods.HOURLY.next(segment.begin(), step, 1);
        Assert.assertEquals("2018-10-21 15:23:32.000 ~ 2018-10-21 18:23:31.999", segment.toString());
    }

    @Test
    public void test() throws DecoderException {
        String hex = "0cb703fbc86a41b0";
        byte[] bytes = Hex.decodeHex(hex.toCharArray());
        long number = Bytes.toLong(bytes);
        System.out.println(number);
        System.out.println(Long.toHexString(number));
        Assert.assertEquals("cb703fbc86a41b0", Long.toHexString(number));
        Assert.assertEquals("0cb703fbc86a41b0", Bytes.hexEncode(Bytes.toBytes(number)));
    }
}
