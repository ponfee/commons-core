package code.ponfee.commons.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

public class FastJsonUtils {

    /*private static final SerializeConfig mapping = new SerializeConfig();
    static {
        mapping.put(Date.class, new CustomDateFormatSerializer());
    }*/

    private static final String[] DATA_PATTERS = { "yyyy-MM-dd HH:mm:ss,SSS", "yyyy-MM-dd HH:mm:ss" };

    public static class CustomDateFormatSerializer implements ObjectSerializer {
        @Override
        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
            if (object == null) {
                serializer.out.writeNull();
                return;
            }
            serializer.write(FastDateFormat.getInstance(DATA_PATTERS[new Random().nextInt(2)]).format((Date) object));
        }
    }

    public static class CustomDateFormatDeserializer implements ObjectDeserializer {
        private static final Date DEFAULT_DATE = new Date(0L);
        private static final DateParser FORMAT1 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        private static final DateParser FORMAT2 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss,SSS");

        @Override
        @SuppressWarnings("unchecked")
        public Date deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            String dateString = parser.getLexer().stringVal();
            try {
                return (dateString.length() == 19 ? FORMAT1 : FORMAT2).parse(dateString);
            } catch (ParseException e) {
                return DEFAULT_DATE;
            }
        }

        @Override
        public int getFastMatchToken() {
            return 0;
        }
    }

    public static class DateBean {
        @JSONField(serializeUsing = CustomDateFormatSerializer.class, deserializeUsing = CustomDateFormatDeserializer.class)
        private Date date;

        public DateBean() {}

        public DateBean(Date date) {
            this.date = date;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return "DateBean [date=" + FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss SSS").format(date) + "]";
        }
    }

    public static void main(String[] args) {
        DateBean bean = new DateBean(new Date(System.currentTimeMillis()));

        for (int i = 0; i < 100; i++) {
            String json = JSON.toJSONString(bean);
            bean = JSON.parseObject(json, DateBean.class);
            System.out.println("json: " + json + ", bean: " + bean);
        }
    }

}
