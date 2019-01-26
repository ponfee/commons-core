package code.ponfee.commons.jedis.spring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

/**
 * Kryo redis serialize utility
 * 
 * @param <T>
 * 
 * @author Ponfee
 */
public class KryoRedisSerializer<T> implements RedisSerializer<T> {

    private static Logger logger = LoggerFactory.getLogger(KryoRedisSerializer.class);

    //private static final ThreadLocal<Kryo> KRYOS = ThreadLocal.withInitial(Kryo::new);
    private static final KryoPool KRYO_POOL =
        new KryoPool.Builder(Kryo::new).softReferences().build();

    @Override
    public byte[] serialize(T obj) throws SerializationException {
        if (obj == null) {
            return null;
        }

        Kryo kryo = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)
        ) {
            kryo = getKryo();
            kryo.writeClassAndObject(output, obj);
            output.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Kryo serialize occur error.", e);
        } finally {
            releaseKryo(kryo);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        Kryo kryo = null;
        try (Input input = new Input(bytes)) {
            kryo = getKryo();
            return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            throw new SerializationException("Kryo deserialize occur error.", e);
        } finally {
            releaseKryo(kryo);
        }
    }

    private static Kryo getKryo() {
        Kryo kryo = KRYO_POOL.borrow();
        kryo.setReferences(false);
        return kryo;
    }

    private static void releaseKryo(Kryo kryo) {
        if (kryo != null) {
            try {
                KRYO_POOL.release(kryo);
            } catch (Throwable t) {
                logger.error("Release kryo occur error.", t);
            }
        }
    }

}
