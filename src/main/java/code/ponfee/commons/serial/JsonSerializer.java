package code.ponfee.commons.serial;

import code.ponfee.commons.io.Closeables;
import code.ponfee.commons.io.ExtendedGZIPOutputStream;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * json序例化
 * 
 * @author Ponfee
 */
public class JsonSerializer extends Serializer {

    /** json object mapper */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        GZIPOutputStream gzout = null;
        try {
            byte[] data = MAPPER.writeValueAsBytes(obj);
            if (compress) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
                gzout = new ExtendedGZIPOutputStream(baos);
                gzout.write(data, 0, data.length);
                gzout.close();
                gzout = null;
                data = baos.toByteArray();
            }
            return data;
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            Closeables.closeLog(gzout, "close GZIPOutputStream exception");
        }
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        GZIPInputStream gzin = null;
        try {
            if (compress) {
                gzin = new GZIPInputStream(new ByteArrayInputStream(bytes));
                bytes =  IOUtils.toByteArray(gzin);
            }
            return MAPPER.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            Closeables.closeLog(gzin, "close GZIPInputStream exception");
        }
    }

}
