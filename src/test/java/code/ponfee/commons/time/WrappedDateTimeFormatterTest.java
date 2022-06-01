package code.ponfee.commons.time;

import code.ponfee.commons.util.WrappedDateTimeFormatter;
import code.ponfee.commons.util.WrappedFastDateFormat;
import org.junit.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Ponfee
 */
public class WrappedDateTimeFormatterTest {

    @Test
    public void test1() {
        String dateString = "2022-07-19T13:44:27.873";
        LocalDateTime date = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        WrappedDateTimeFormatter format = WrappedDateTimeFormatter.DEFAULT;
        assertEquals(dateString, date.toString());

        assertEquals("20220719134427", WrappedDateTimeFormatter.PATTERN_01.format(date));
        assertEquals("2022-07-19 13:44:27", WrappedDateTimeFormatter.PATTERN_11.format(date));
        assertEquals("2022/07/19 13:44:27", WrappedDateTimeFormatter.PATTERN_12.format(date));
        assertEquals("2022-07-19T13:44:27", WrappedDateTimeFormatter.PATTERN_13.format(date));
        assertEquals("2022/07/19T13:44:27", WrappedDateTimeFormatter.PATTERN_14.format(date));
        assertEquals("2022-07-19 13:44:27.873", WrappedDateTimeFormatter.PATTERN_21.format(date));
        assertEquals("2022/07/19 13:44:27.873", WrappedDateTimeFormatter.PATTERN_22.format(date));
        assertEquals("2022-07-19T13:44:27.873", WrappedDateTimeFormatter.PATTERN_23.format(date));
        assertEquals("2022/07/19T13:44:27.873", WrappedDateTimeFormatter.PATTERN_24.format(date));

        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", WrappedDateTimeFormatter.PATTERN_01.parse(WrappedDateTimeFormatter.PATTERN_01.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", WrappedDateTimeFormatter.PATTERN_11.parse(WrappedDateTimeFormatter.PATTERN_11.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", WrappedDateTimeFormatter.PATTERN_12.parse(WrappedDateTimeFormatter.PATTERN_12.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", WrappedDateTimeFormatter.PATTERN_13.parse(WrappedDateTimeFormatter.PATTERN_13.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27", WrappedDateTimeFormatter.PATTERN_14.parse(WrappedDateTimeFormatter.PATTERN_14.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27.873", WrappedDateTimeFormatter.PATTERN_21.parse(WrappedDateTimeFormatter.PATTERN_21.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27.873", WrappedDateTimeFormatter.PATTERN_22.parse(WrappedDateTimeFormatter.PATTERN_22.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27.873", WrappedDateTimeFormatter.PATTERN_23.parse(WrappedDateTimeFormatter.PATTERN_23.format(date)).toString());
        assertEquals("{},ISO resolved to 2022-07-19T13:44:27.873", WrappedDateTimeFormatter.PATTERN_24.parse(WrappedDateTimeFormatter.PATTERN_24.format(date)).toString());

        assertEquals("2022-07-19T13:44:27", format.parse(WrappedDateTimeFormatter.PATTERN_01.format(date)).toString());
        assertEquals("2022-07-19T13:44:27", format.parse(WrappedDateTimeFormatter.PATTERN_11.format(date)).toString());
        assertEquals("2022-07-19T13:44:27", format.parse(WrappedDateTimeFormatter.PATTERN_12.format(date)).toString());
        assertEquals("2022-07-19T13:44:27", format.parse(WrappedDateTimeFormatter.PATTERN_13.format(date)).toString());
        assertEquals("2022-07-19T13:44:27", format.parse(WrappedDateTimeFormatter.PATTERN_14.format(date)).toString());
        assertEquals("2022-07-19T13:44:27.873", format.parse(WrappedDateTimeFormatter.PATTERN_21.format(date)).toString());
        assertEquals("2022-07-19T13:44:27.873", format.parse(WrappedDateTimeFormatter.PATTERN_22.format(date)).toString());
        assertEquals("2022-07-19T13:44:27.873", format.parse(WrappedDateTimeFormatter.PATTERN_23.format(date)).toString());
        assertEquals("2022-07-19T13:44:27.873", format.parse(WrappedDateTimeFormatter.PATTERN_24.format(date)).toString());
    }

    @Test
    public void test2() throws ParseException {
        WrappedDateTimeFormatter format = WrappedDateTimeFormatter.DEFAULT;
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

        assertEquals("2022-07-18T07:45:59", format.parse("1658130359").toString());
        assertEquals("2022-07-18T07:45:59", format.parse("1658130359000").toString());
        assertEquals("2001-09-10T13:59:19", format.parse("1000130359").toString());
        assertEquals("2001-09-10T13:59:19", format.parse("1000130359000").toString());

        assertEquals("2022-07-18T15:11:11", format.parse("2022-07-18T15:11:11Z").toString());
        assertEquals("2022-07-18T15:11:11", format.parse("2022-07-18T15:11:11.Z").toString());
        assertEquals("2022-07-18T15:11:11.100", format.parse("2022-07-18T15:11:11.1Z").toString());
        assertEquals("2022-07-18T15:11:11.130", format.parse("2022-07-18T15:11:11.13Z").toString());
        assertEquals("2022-07-18T15:11:11.133", format.parse("2022-07-18T15:11:11.133Z").toString());

        assertEquals("2022-07-18T15:11:11", format.parse("2022/07/18T15:11:11Z").toString());
        assertEquals("2022-07-18T15:11:11", format.parse("2022/07/18T15:11:11.Z").toString());
        assertEquals("2022-07-18T15:11:11.001", format.parse("2022/07/18T15:11:11.1Z").toString());
        assertEquals("2022-07-18T15:11:11.013", format.parse("2022/07/18T15:11:11.13Z").toString());
        assertEquals("2022-07-18T15:11:11.133", format.parse("2022/07/18T15:11:11.133Z").toString());

        assertThrows(DateTimeParseException.class, () -> format.parse("2022-07-18T1:1:1Z"));


        String dateString = "2022-07-19T13:44:27.873Z";
        Date date = WrappedFastDateFormat.PATTERN_71.parse(dateString);
        System.out.println(date.getTime());
            
        assertEquals("2022-07-19T13:44:27", format.parse(Long.toString(date.getTime() / 1000)).toString());
        assertEquals("2022-07-19T13:44:27.873", format.parse(Long.toString(date.getTime())).toString());
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
        //System.out.println(WrappedDateTimeFormatter.DEFAULT.parse(text));
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

        WrappedDateTimeFormatter format = WrappedDateTimeFormatter.DEFAULT;
        /*System.out.println(format.parse("2022/07/18T15:11:11.1Z").toString());
        System.out.println(format.parse("2022/07/18T15:11:11Z").toString());
        System.out.println(format.parse("2022/07/18T15:11:11.13Z").toString());
        System.out.println(format.parse("2022/07/18T15:11:11.133Z").toString());*/
    }
}
