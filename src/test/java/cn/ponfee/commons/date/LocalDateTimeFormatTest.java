package cn.ponfee.commons.date;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * LocalDateTimeFormatTest
 *
 * @author Ponfee
 */
public class LocalDateTimeFormatTest {

    @Test
    public void test9() {
        LocalDateTime localDateTime = LocalDateTime.of(2022, 01, 02, 03, 04, 05, 123000000);
        Date date = Dates.toDate(localDateTime);

        assertEquals("2022-01-02T03:04:05.123", localDateTime.toString());
        assertEquals("2022-01-02 03:04:05", Dates.format(date).toString());
        assertEquals("2022-01-02T07:04:05.123", Dates.zoneConvert(localDateTime, ZoneId.of("UTC+4"), ZoneId.of("UTC+8")).toString());
        assertEquals("2022-01-02 11:04:05", Dates.format(Dates.zoneConvert(date, ZoneId.of("UTC+0"), ZoneId.of("UTC+8"))));
        assertEquals("2022-01-01 19:04:05", Dates.format(Dates.zoneConvert(date, ZoneId.of("UTC+8"), ZoneId.of("UTC+0"))));

        LocalDateTime originLocalDateTime = LocalDateTime.of(1970, 01, 01, 00, 00, 00, 0);
        Date originDate = Dates.toDate(originLocalDateTime); // defaultZone: UTC+8
        assertEquals("1970-01-01T08:00", Dates.zoneConvert(originLocalDateTime, ZoneId.of("UTC+0"), ZoneId.of("UTC+8")).toString());
        assertEquals("1969-12-31T16:00", Dates.zoneConvert(originLocalDateTime, ZoneId.of("UTC+8"), ZoneId.of("UTC+0")).toString());
        assertEquals("1970-01-01 08:00:00", Dates.format(Dates.zoneConvert(originDate, ZoneId.of("UTC+0"), ZoneId.of("UTC+8"))));
        assertEquals("1969-12-31 16:00:00", Dates.format(Dates.zoneConvert(originDate, ZoneId.of("UTC+8"), ZoneId.of("UTC+0"))));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Dates.DATETIME_PATTERN);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC+0")));
        assertEquals("1969-12-31 16:00:00", simpleDateFormat.format(originDate));

        assertEquals("1970-01-01 08:00:00", Dates.zoneConvert(Dates.toDate("1970-01-01 00:00:00"), ZoneId.of("UTC+0"), ZoneId.of("UTC+8")));
        assertEquals("1970-01-01 00:00:00", Dates.zoneConvert(Dates.toDate("1970-01-01 08:00:00"), ZoneId.of("UTC+8"), ZoneId.of("UTC+0")));
    }

    @Test
    public void test8() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Dates.toDate(localDateTime);

        System.out.println(localDateTime.toString());
        System.out.println(Dates.format(date).toString());

        System.out.println("\n-------toDate");
        System.out.println(Dates.toDate(localDateTime));
        System.out.println(Dates.toDate(localDateTime));

        System.out.println("\n-------toLocalDateTime");
        System.out.println(Dates.toLocalDateTime(date));
        System.out.println(Dates.toLocalDateTime(date));

        System.out.println("\n-------zoneConvert1");
        LocalDateTime targetLocalDateTime = Dates.zoneConvert(localDateTime, ZoneId.of("UTC+8"), ZoneId.of("UTC+0"));
        Date targetDate = Dates.zoneConvert(date, ZoneId.of("UTC+8"), ZoneId.of("UTC+0"));
        System.out.println(targetLocalDateTime);
        System.out.println(targetDate);

        System.out.println("\n-------zoneConvert2");
        targetLocalDateTime = Dates.zoneConvert(localDateTime, ZoneId.of("UTC+0"), ZoneId.of("UTC+8"));
        targetDate = Dates.zoneConvert(date, ZoneId.of("UTC+0"), ZoneId.of("UTC+8"));
        System.out.println(targetLocalDateTime);
        System.out.println(targetDate);
    }

    @Test
    public void test0() throws ParseException {
        LocalDateTime localDateTime = LocalDateTime.of(2022, 01, 02, 03, 05, 05, 123000000);
        assertEquals("2022-01-02T03:05:05.123", localDateTime.toString());

        assertEquals("20220102030505",          LocalDateTimeFormat.PATTERN_01.format(localDateTime));
        assertEquals("2022-01-02 03:05:05",     LocalDateTimeFormat.PATTERN_11.format(localDateTime));
        assertEquals("2022/01/02 03:05:05",     LocalDateTimeFormat.PATTERN_12.format(localDateTime));
        assertEquals("2022-01-02T03:05:05",     LocalDateTimeFormat.PATTERN_13.format(localDateTime));
        assertEquals("2022/01/02T03:05:05",     LocalDateTimeFormat.PATTERN_14.format(localDateTime));
        assertEquals("2022-01-02 03:05:05.123", LocalDateTimeFormat.PATTERN_21.format(localDateTime));
        assertEquals("2022/01/02 03:05:05.123", LocalDateTimeFormat.PATTERN_22.format(localDateTime));
        assertEquals("2022-01-02T03:05:05.123", LocalDateTimeFormat.PATTERN_23.format(localDateTime));
        assertEquals("2022/01/02T03:05:05.123", LocalDateTimeFormat.PATTERN_24.format(localDateTime));

        assertEquals("{},ISO resolved to 2022-01-02T03:05:05", LocalDateTimeFormat.PATTERN_01.parse("20220102030505").toString());
        assertEquals("{},ISO resolved to 2022-01-02T03:05:05", LocalDateTimeFormat.PATTERN_11.parse("2022-01-02 03:05:05").toString());
        assertEquals("{},ISO resolved to 2022-01-02T03:05:05", LocalDateTimeFormat.PATTERN_12.parse("2022/01/02 03:05:05").toString());
        assertEquals("{},ISO resolved to 2022-01-02T03:05:05", LocalDateTimeFormat.PATTERN_13.parse("2022-01-02T03:05:05").toString());
        assertEquals("{},ISO resolved to 2022-01-02T03:05:05", LocalDateTimeFormat.PATTERN_14.parse("2022/01/02T03:05:05").toString());
        assertEquals("{},ISO resolved to 2022-01-02T03:05:05.123", LocalDateTimeFormat.PATTERN_21.parse("2022-01-02 03:05:05.123").toString());
        assertEquals("{},ISO resolved to 2022-01-02T03:05:05.123", LocalDateTimeFormat.PATTERN_22.parse("2022/01/02 03:05:05.123").toString());
        assertEquals("{},ISO resolved to 2022-01-02T03:05:05.123", LocalDateTimeFormat.PATTERN_23.parse("2022-01-02T03:05:05.123").toString());
        assertEquals("{},ISO resolved to 2022-01-02T03:05:05.123", LocalDateTimeFormat.PATTERN_24.parse("2022/01/02T03:05:05.123").toString());


        assertEquals("2022-01-02T03:05:05", LocalDateTimeFormat.DEFAULT.parse("20220102030505").toString());
        assertEquals("2022-01-02T03:05:05", LocalDateTimeFormat.DEFAULT.parse("2022-01-02 03:05:05").toString());
        assertEquals("2022-01-02T03:05:05", LocalDateTimeFormat.DEFAULT.parse("2022/01/02 03:05:05").toString());
        assertEquals("2022-01-02T03:05:05", LocalDateTimeFormat.DEFAULT.parse("2022-01-02T03:05:05").toString());
        assertEquals("2022-01-02T03:05:05", LocalDateTimeFormat.DEFAULT.parse("2022/01/02T03:05:05").toString());
        assertEquals("2022-01-02T03:05:05.123", LocalDateTimeFormat.DEFAULT.parse("2022-01-02 03:05:05.123").toString());
        assertEquals("2022-01-02T03:05:05.123", LocalDateTimeFormat.DEFAULT.parse("2022/01/02 03:05:05.123").toString());
        assertEquals("2022-01-02T03:05:05.123", LocalDateTimeFormat.DEFAULT.parse("2022-01-02T03:05:05.123").toString());
        assertEquals("2022-01-02T03:05:05.123", LocalDateTimeFormat.DEFAULT.parse("2022/01/02T03:05:05.123").toString());


        assertEquals("2022-01-02T03:05:05.123", LocalDateTimeFormat.DEFAULT.parse("2022-01-02T03:05:05.123Z").toString());
        assertEquals("2022-01-02T03:05:05.123", LocalDateTimeFormat.DEFAULT.parse("2022/01/02T03:05:05.123Z").toString());

        Date date = Dates.toDate(localDateTime);
        assertEquals("Sun Jan 02 03:05:05 CST 2022", date.toString());
        assertEquals("2022-01-02T03:05:05.123", LocalDateTimeFormat.DEFAULT.parse(String.valueOf(date.getTime())).toString());
        assertEquals("2022-01-02T03:05:05", LocalDateTimeFormat.DEFAULT.parse(String.valueOf(date.getTime() / 1000)).toString());
    }

    @Test
    public void test1() {
        String dateString = "2022-07-19T13:44:27.873";
        LocalDateTime date = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTimeFormat format = LocalDateTimeFormat.DEFAULT;
        assertEquals(dateString, date.toString());

        assertEquals("20220719134427", LocalDateTimeFormat.PATTERN_01.format(date));
        assertEquals("2022-07-19 13:44:27", LocalDateTimeFormat.PATTERN_11.format(date));
        assertEquals("2022/07/19 13:44:27", LocalDateTimeFormat.PATTERN_12.format(date));
        assertEquals("2022-07-19T13:44:27", LocalDateTimeFormat.PATTERN_13.format(date));
        assertEquals("2022/07/19T13:44:27", LocalDateTimeFormat.PATTERN_14.format(date));
        assertEquals("2022-07-19 13:44:27.873", LocalDateTimeFormat.PATTERN_21.format(date));
        assertEquals("2022/07/19 13:44:27.873", LocalDateTimeFormat.PATTERN_22.format(date));
        assertEquals("2022-07-19T13:44:27.873", LocalDateTimeFormat.PATTERN_23.format(date));
        assertEquals("2022/07/19T13:44:27.873", LocalDateTimeFormat.PATTERN_24.format(date));

        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", LocalDateTimeFormat.PATTERN_01.parse(LocalDateTimeFormat.PATTERN_01.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", LocalDateTimeFormat.PATTERN_11.parse(LocalDateTimeFormat.PATTERN_11.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", LocalDateTimeFormat.PATTERN_12.parse(LocalDateTimeFormat.PATTERN_12.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", LocalDateTimeFormat.PATTERN_13.parse(LocalDateTimeFormat.PATTERN_13.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", LocalDateTimeFormat.PATTERN_14.parse(LocalDateTimeFormat.PATTERN_14.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27.873", LocalDateTimeFormat.PATTERN_21.parse(LocalDateTimeFormat.PATTERN_21.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27.873", LocalDateTimeFormat.PATTERN_22.parse(LocalDateTimeFormat.PATTERN_22.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27.873", LocalDateTimeFormat.PATTERN_23.parse(LocalDateTimeFormat.PATTERN_23.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27.873", LocalDateTimeFormat.PATTERN_24.parse(LocalDateTimeFormat.PATTERN_24.format(date)).toString());

        assertEquals("2022-07-19T13:44:27", format.parse(LocalDateTimeFormat.PATTERN_01.format(date)).toString());
        assertEquals("2022-07-19T13:44:27", format.parse(LocalDateTimeFormat.PATTERN_11.format(date)).toString());
        assertEquals("2022-07-19T13:44:27", format.parse(LocalDateTimeFormat.PATTERN_12.format(date)).toString());
        assertEquals("2022-07-19T13:44:27", format.parse(LocalDateTimeFormat.PATTERN_13.format(date)).toString());
        assertEquals("2022-07-19T13:44:27", format.parse(LocalDateTimeFormat.PATTERN_14.format(date)).toString());
        assertEquals("2022-07-19T13:44:27.873", format.parse(LocalDateTimeFormat.PATTERN_21.format(date)).toString());
        assertEquals("2022-07-19T13:44:27.873", format.parse(LocalDateTimeFormat.PATTERN_22.format(date)).toString());
        assertEquals("2022-07-19T13:44:27.873", format.parse(LocalDateTimeFormat.PATTERN_23.format(date)).toString());
        assertEquals("2022-07-19T13:44:27.873", format.parse(LocalDateTimeFormat.PATTERN_24.format(date)).toString());
    }

    @Test
    public void test2() throws ParseException {
        LocalDateTimeFormat format = LocalDateTimeFormat.DEFAULT;
        assertEquals("2022-07-18T00:00", format.parse("20220718").toString());
        assertEquals("2022-07-18T00:00", format.parse("2022-07-18").toString());
        assertEquals("2022-07-18T00:00", format.parse("2022/07/18").toString());

        assertEquals("2022-07-18T15:45:59", format.parse("20220718154559").toString());
        assertEquals("2022-07-18T15:45:59", format.parse("2022-07-18 15:45:59").toString());
        assertEquals("2022-07-18T15:45:59", format.parse("2022/07/18 15:45:59").toString());

        assertEquals("2022-07-18T15:45:59", format.parse("2022-07-18T15:45:59").toString());
        assertEquals("2022-07-18T15:45:59", format.parse("2022/07/18T15:45:59").toString());

        assertEquals("2022-07-18T15:45:59.414", format.parse("2022-07-18 15:45:59.414").toString());
        assertEquals("2022-07-18T15:45:59.414", format.parse("2022/07/18 15:45:59.414").toString());

        assertEquals("2022-07-18T15:45:59.414", format.parse("2022-07-18T15:45:59.414").toString());
        assertEquals("2022-07-18T15:45:59.414", format.parse("2022/07/18T15:45:59.414").toString());

        assertEquals("2022-07-18T15:45:59", format.parse("1658130359").toString());
        assertEquals("2022-07-18T15:45:59", format.parse("1658130359000").toString());
        assertEquals("2001-09-10T21:59:19", format.parse("1000130359").toString());
        assertEquals("2001-09-10T21:59:19", format.parse("1000130359000").toString());

        assertEquals("2022-07-18T15:11:11", format.parse("2022-07-18T15:11:11Z").toString());
        assertEquals("2022-07-18T15:11:11", format.parse("2022-07-18T15:11:11.Z").toString());
        assertEquals("2022-07-18T15:11:11.100", format.parse("2022-07-18T15:11:11.1Z").toString());
        assertEquals("2022-07-18T15:11:11.130", format.parse("2022-07-18T15:11:11.13Z").toString());
        assertEquals("2022-07-18T15:11:11.133", format.parse("2022-07-18T15:11:11.133Z").toString());

        assertEquals("2022-07-18T15:11:11", format.parse("2022/07/18T15:11:11Z").toString());
        assertEquals("2022-07-18T15:11:11", format.parse("2022/07/18T15:11:11.Z").toString());
        assertEquals("2022-07-18T15:11:11.100", format.parse("2022/07/18T15:11:11.1Z").toString());
        assertEquals("2022-07-18T15:11:11.130", format.parse("2022/07/18T15:11:11.13Z").toString());
        assertEquals("2022-07-18T15:11:11.133", format.parse("2022/07/18T15:11:11.133Z").toString());

        assertThrows(Exception.class, () -> format.parse("2022-07-18T1:1:1Z"));

    }

    @Test
    public void test3() {
        String string1 = "2022-07-18T15:11:11.133";
        assertEquals("2022-07-18T15:11:11.133", LocalDateTime.parse(string1, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString());
        String string2 = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.parse(string1, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals(string1, string2);

        String dateString = "2022-07-18T15:11:11.133Z";
        LocalDateTime date = LocalDateTime.ofInstant(Instant.parse(dateString), ZoneOffset.UTC);
        assertEquals(date.toString(), "2022-07-18T15:11:11.133");

        date = LocalDateTime.ofInstant(Instant.parse(dateString), ZoneOffset.ofHours(8));
        assertEquals(date.toString(), "2022-07-18T23:11:11.133");

        assertThrows(
            DateTimeParseException.class,
            () -> LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
        );
    }

    @Test
    public void test4() {
        String text = "2022/07/18T15:11:11.133Z";
        //System.out.println(JavaTimeDateFormat.DEFAULT.parse(text));
        //LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss.SSSZ"));
        //LocalDateTime.ofInstant(DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss.SSSZ").parse(text, Instant::from), ZoneOffset.UTC);

        /*
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .appendInstant()
            .toFormatter();
        */

        LocalDateTimeFormat format = LocalDateTimeFormat.DEFAULT;
        /*System.out.println(format.parse("2022/07/18T15:11:11.1Z").toString());
        System.out.println(format.parse("2022/07/18T15:11:11Z").toString());
        System.out.println(format.parse("2022/07/18T15:11:11.13Z").toString());
        System.out.println(format.parse("2022/07/18T15:11:11.133Z").toString());*/
    }
}
