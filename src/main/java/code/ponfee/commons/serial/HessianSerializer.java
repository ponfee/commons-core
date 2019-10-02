package code.ponfee.commons.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;

import code.ponfee.commons.io.Closeables;
import code.ponfee.commons.io.ExtendedGZIPOutputStream;

/**
 * hessian序例化
 * 
 * @author Ponfee
 */
public class HessianSerializer extends Serializer {

    private static Logger logger = LoggerFactory.getLogger(HessianSerializer.class);

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        GZIPOutputStream gzout = null;
        HessianSerializerOutput hessian = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
            if (compress) {
                gzout = new ExtendedGZIPOutputStream(baos);
                hessian = new HessianSerializerOutput(gzout);
            } else {
                hessian = new HessianSerializerOutput(baos);
            }
            hessian.writeObject(obj);
            hessian.close();
            hessian = null;
            if (gzout != null) {
                gzout.close();
                gzout = null;
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            if (hessian != null) {
                try {
                    hessian.close();
                } catch (IOException e) {
                    logger.error("close hessian exception", e);
                }
            }
            Closeables.log(gzout, "close GZIPOutputStream exception");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        GZIPInputStream gzin = null;
        HessianSerializerInput hessian = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            if (compress) {
                gzin = new GZIPInputStream(bais);
                hessian = new HessianSerializerInput(gzin);
            } else {
                hessian = new HessianSerializerInput(bais);
            }
            T t = (T) hessian.readObject();
            if (t != null && !ClassUtils.isAssignable(t.getClass(), clazz)) {
                throw new ClassCastException(
                    ClassUtils.getName(t.getClass()) + " can't be cast to " + ClassUtils.getName(clazz)
                );
            }
            return t;
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            if (hessian != null) {
                try {
                    hessian.close();
                } catch (Exception e) {
                    logger.error("close hessian exception", e);
                }
            }
            Closeables.log(gzin, "close GZIPInputStream exception");
        }
    }

}
