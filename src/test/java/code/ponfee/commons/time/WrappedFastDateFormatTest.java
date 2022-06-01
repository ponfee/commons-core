package code.ponfee.commons.time;

import code.ponfee.commons.util.WrappedFastDateFormat;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author Ponfee
 */
public class WrappedFastDateFormatTest {

    @Test
    public void test1() throws ParseException {
        String dateString = "2022-07-19T13:44:27.873Z";
        Date date = WrappedFastDateFormat.PATTERN_71.parse(dateString);

        assertEquals("202207", WrappedFastDateFormat.PATTERN_11.format(date));
        assertEquals("2022-07", WrappedFastDateFormat.PATTERN_12.format(date));
        assertEquals("2022/07", WrappedFastDateFormat.PATTERN_13.format(date));

        assertEquals("20220719", WrappedFastDateFormat.PATTERN_21.format(date));
        assertEquals("2022-07-19", WrappedFastDateFormat.PATTERN_22.format(date));
        assertEquals("2022/07/19", WrappedFastDateFormat.PATTERN_23.format(date));

        assertEquals("20220719214427", WrappedFastDateFormat.PATTERN_31.format(date));
        assertEquals("20220719214427873", WrappedFastDateFormat.PATTERN_32.format(date));

        assertEquals("2022-07-19 21:44:27", WrappedFastDateFormat.PATTERN_41.format(date));
        assertEquals("2022/07/19 21:44:27", WrappedFastDateFormat.PATTERN_42.format(date));
        assertEquals("2022-07-19T21:44:27", WrappedFastDateFormat.PATTERN_43.format(date));
        assertEquals("2022/07/19T21:44:27", WrappedFastDateFormat.PATTERN_44.format(date));

        assertEquals("2022-07-19 21:44:27.873", WrappedFastDateFormat.PATTERN_51.format(date));
        assertEquals("2022/07/19 21:44:27.873", WrappedFastDateFormat.PATTERN_52.format(date));
        assertEquals("2022-07-19T21:44:27.873", WrappedFastDateFormat.PATTERN_53.format(date));
        assertEquals("2022/07/19T21:44:27.873", WrappedFastDateFormat.PATTERN_54.format(date));

        assertEquals("2022-07-19 21:44:27.873Z", WrappedFastDateFormat.PATTERN_61.format(date));
        assertEquals("2022/07/19 21:44:27.873Z", WrappedFastDateFormat.PATTERN_62.format(date));
        assertEquals("2022-07-19T21:44:27.873Z", WrappedFastDateFormat.PATTERN_63.format(date));
        assertEquals("2022/07/19T21:44:27.873Z", WrappedFastDateFormat.PATTERN_64.format(date));

        assertEquals("2022-07-19T21:44:27.873+08", WrappedFastDateFormat.PATTERN_71.format(date));
        assertEquals("2022/07/19T21:44:27.873+08", WrappedFastDateFormat.PATTERN_72.format(date));

        assertEquals("Tue Jul 19 21:44:27 CST 2022", WrappedFastDateFormat.PATTERN_81.format(date));
    }

    @Test
    public void test2() throws ParseException {
        WrappedFastDateFormat format = WrappedFastDateFormat.DEFAULT;
        FastDateFormat fastDateFormat = WrappedFastDateFormat.PATTERN_51;

        assertEquals("2022-07-01 00:00:00.000", fastDateFormat.format(format.parse("202207")));
        assertEquals("2022-07-01 00:00:00.000", fastDateFormat.format(format.parse("2022-07")));
        assertEquals("2022-07-01 00:00:00.000", fastDateFormat.format(format.parse("2022/07")));

        assertEquals("2022-07-19 00:00:00.000", fastDateFormat.format(format.parse("20220719")));
        assertEquals("2022-07-19 00:00:00.000", fastDateFormat.format(format.parse("2022-07-19")));
        assertEquals("2022-07-19 00:00:00.000", fastDateFormat.format(format.parse("2022/07/19")));

        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("20220719214427")));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("20220719214427873")));

        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022-07-19 21:44:27")));
        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022/07/19 21:44:27")));
        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022-07-19T21:44:27")));
        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022/07/19T21:44:27")));

        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022-07-19 21:44:27.873")));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022/07/19 21:44:27.873")));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022-07-19T21:44:27.873")));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022/07/19T21:44:27.873")));

        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022-07-19T21:44:27Z")));
        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022-07-19T21:44:27.Z")));
        assertEquals("2022-07-19 21:44:27.008", fastDateFormat.format(format.parse("2022-07-19T21:44:27.8Z")));
        assertEquals("2022-07-19 21:44:27.087", fastDateFormat.format(format.parse("2022-07-19T21:44:27.87Z")));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022-07-19T21:44:27.873Z")));
        
        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022/07/19T21:44:27Z")));
        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022/07/19T21:44:27.Z")));
        assertEquals("2022-07-19 21:44:27.003", fastDateFormat.format(format.parse("2022/07/19T21:44:27.3Z")));
        assertEquals("2022-07-19 21:44:27.073", fastDateFormat.format(format.parse("2022/07/19T21:44:27.73Z")));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022/07/19T21:44:27.873Z")));



        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022-07-19 21:44:27Z")));
        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022-07-19 21:44:27.Z")));
        assertEquals("2022-07-19 21:44:27.008", fastDateFormat.format(format.parse("2022-07-19 21:44:27.8Z")));
        assertEquals("2022-07-19 21:44:27.087", fastDateFormat.format(format.parse("2022-07-19 21:44:27.87Z")));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022-07-19 21:44:27.873Z")));

        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022/07/19 21:44:27Z")));
        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse("2022/07/19 21:44:27.Z")));
        assertEquals("2022-07-19 21:44:27.003", fastDateFormat.format(format.parse("2022/07/19 21:44:27.3Z")));
        assertEquals("2022-07-19 21:44:27.073", fastDateFormat.format(format.parse("2022/07/19 21:44:27.73Z")));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022/07/19 21:44:27.873Z")));
        

        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022-07-19T21:44:27.873+08")));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse("2022/07/19T21:44:27.873+08")));

        assertEquals("2022-07-20 11:44:27.000", fastDateFormat.format(format.parse("Tue Jul 19 21:44:27 CST 2022")));

        String dateString = "2022-07-19T13:44:27.873Z";
        Date date = WrappedFastDateFormat.PATTERN_71.parse(dateString);
        assertEquals("2022-07-19 21:44:27.000", fastDateFormat.format(format.parse(Long.toString(date.getTime()/1000))));
        assertEquals("2022-07-19 21:44:27.873", fastDateFormat.format(format.parse(Long.toString(date.getTime()))));
    }

}
