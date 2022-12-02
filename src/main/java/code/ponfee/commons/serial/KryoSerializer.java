package code.ponfee.commons.serial;

import code.ponfee.commons.io.Closeables;
import code.ponfee.commons.io.ExtendedGZIPOutputStream;
import code.ponfee.commons.io.Files;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * kryo序例化
 * 
 * the bean class must include default no-arg constructor
 * 
 * @author Ponfee
 */
public class KryoSerializer extends Serializer {

    private static final Logger LOG = LoggerFactory.getLogger(KryoSerializer.class);
    public static final KryoSerializer INSTANCE = new KryoSerializer();

    //private static final ThreadLocal<Kryo> KRYO_HOLDER = ThreadLocal.withInitial(Kryo::new);

    // Pool constructor arguments: thread safe, soft references, maximum capacity
    private static final Pool<Kryo> KRYO_POOL = new Pool<Kryo>(true, false, 32) {
        @Override
        protected Kryo create () {
            Kryo kryo = new Kryo();
            // Configure the Kryo instance.
            kryo.setRegistrationRequired(false);
            //kryo.register(A.class, B.class);
            //kryo.register(B.class, new com.esotericsoftware.kryo.serializers.JavaSerializer());
            //kryo.addDefaultSerializer(A.class, ASerializer.class);
            return kryo;
        }
    };

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        GZIPOutputStream gzout = null;
        Output output = null;
        Kryo kryo = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
            if (compress) {
                gzout = new ExtendedGZIPOutputStream(baos);
                output = new ByteBufferOutput(gzout, Files.BUFF_SIZE);
            } else {
                output = new ByteBufferOutput(baos, Files.BUFF_SIZE);
            }
            (kryo = obtain()).writeObject(output, obj);
            output.close();
            output = null;
            if (gzout != null) {
                gzout.close();
                gzout = null;
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            free(kryo);
            Closeables.log(output, "close Output exception");
            Closeables.log(gzout, "close GZIPOutputStream exception");
        }
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        GZIPInputStream gzin = null;
        Input input = null;
        Kryo kryo = null;
        try {
            if (compress) {
                gzin = new GZIPInputStream(new ByteArrayInputStream(bytes));
                input = new ByteBufferInput(gzin);
            } else {
                input = new ByteBufferInput(bytes);
            }
            return (kryo = obtain()).readObject(input, clazz);
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            free(kryo);
            Closeables.log(input, "close Input exception");
            Closeables.log(gzin, "close GZIPInputStream exception");
        }
    }

    private Kryo obtain() {
        return KRYO_POOL.obtain();
    }

    private void free(Kryo kryo) {
        if (kryo == null) {
            return;
        }
        try {
            KRYO_POOL.free(kryo);
        } catch (Throwable t) {
            LOG.error("release kryo occur error", t);
        }
    }

}
