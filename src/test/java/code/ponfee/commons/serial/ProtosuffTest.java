package code.ponfee.commons.serial;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.export.Tmeta;
import code.ponfee.commons.jedis.spring.ProtostuffRedisSerializer;
import code.ponfee.commons.log.LogInfo;
import code.ponfee.commons.model.Result;

@SuppressWarnings("unchecked")
public class ProtosuffTest {

    @Test
    public void test1() {
        Protostuff ser1 = new Protostuff();
        byte[] b1 = ser1.serialize(Result.success("test123"));

        Protostuff ser2 = new Protostuff();
        byte[] b2 = ser2.serialize(new Tmeta(null, "abc", null, true, "#fff"));

        Assert.assertEquals("abc", ((Tmeta) ser1.deserialize(b2)).getFormat());
        Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(b1)).getData());
    }

    @Test
    public void test2() {
        ProtostuffRedisSerializer<Object> ser1 = new ProtostuffRedisSerializer<>();
        ProtostuffRedisSerializer<Object> ser2 = new ProtostuffRedisSerializer<>();

        Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(ser1.serialize(Result.success("test123")))).getData());
        Assert.assertEquals("abce", ((LogInfo) ser1.deserialize(ser2.serialize(new LogInfo("abce")))).getMethodName());
    }

    // -------------------------------------------------------------------
    @Test // ERRO: non thread safe
    public void test3() {
        Stopwatch watch = Stopwatch.createStarted();
        final Protostuff ser1 = new Protostuff();
        final Protostuff ser2 = new Protostuff();

        // ERROR, FIXME: Protostuff unthread safe
        MultithreadExecutor.execAsync(50, () -> {
            Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(ser1.serialize(Result.success("test123")))).getData());
            Assert.assertEquals("abce", ((LogInfo) ser1.deserialize(ser2.serialize(new LogInfo("abce")))).getMethodName());
        }, 5);
        System.out.println(watch.stop());
    }

    @Test
    public void test40() {
        Stopwatch watch = Stopwatch.createStarted();
        ProtostuffRedisSerializer<Object> ser1 = new ProtostuffRedisSerializer<>();
        ProtostuffRedisSerializer<Object> ser2 = new ProtostuffRedisSerializer<>();

        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(32, () -> {
            Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(ser1.serialize(Result.success("test123")))).getData());
            Assert.assertEquals("abce", ((LogInfo) ser1.deserialize(ser2.serialize(new LogInfo("abce")))).getMethodName());
        }, 5);

        System.out.println(watch.stop());
    }

    @Test
    public void test41() {
        Stopwatch watch = Stopwatch.createStarted();
        ProtostuffSerializer ser1 = new ProtostuffSerializer();
        ProtostuffSerializer ser2 = new ProtostuffSerializer();

        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(33, () -> {
            Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(ser1.serialize(Result.success("test123")), Result.class)).getData());
            Assert.assertEquals("abce", ((LogInfo) ser1.deserialize(ser2.serialize(new LogInfo("abce")), LogInfo.class)).getMethodName());
        }, 5);

        System.out.println(watch.stop());
    }

    @Test
    public void test5() {
        Stopwatch watch = Stopwatch.createStarted();
        Protostuff ser1 = new Protostuff();
        for (int i = 0; i < 10000000; i++) {
            Result<?> result = (Result<String>) ser1.deserialize(ser1.serialize(Result.success("test123")));
        }
        System.out.println(watch.stop());
    }

    @Test
    public void test6() {
        Stopwatch watch = Stopwatch.createStarted();
        ProtostuffRedisSerializer<Object> ser1 = new ProtostuffRedisSerializer<>();
        for (int i = 0; i < 10000000; i++) {
            Result<?> result = (Result<String>) ser1.deserialize(ser1.serialize(Result.success("test123")));
        }
        System.out.println(watch.stop());
    }
}
