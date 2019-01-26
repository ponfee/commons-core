package test.utils;

import java.util.Date;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import code.ponfee.commons.util.DatePeriods;
import code.ponfee.commons.util.Dates;

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
        Date origin = Dates.toDate("2018-10-21 12:23:32");
        Date target = Dates.toDate("2018-10-29 12:23:32");
        System.out.println();
        
        System.out.println(DatePeriods.HOURLY.next(origin, target, 2, 1));
        System.out.println(DatePeriods.HOURLY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.DAILY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.WEEKLY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.MONTHLY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.QUARTERLY.next(origin, target, 7, 31));
        System.out.println(DatePeriods.SEMIANNUAL.next(origin, target, 7, 31));
        System.out.println(DatePeriods.ANNUAL.next(origin, target, 7, 31));
        
    }
}
