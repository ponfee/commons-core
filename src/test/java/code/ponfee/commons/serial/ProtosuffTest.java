package code.ponfee.commons.serial;

import org.junit.Test;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.export.Tmeta;
import code.ponfee.commons.jedis.spring.ProtostuffRedisSerializer;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.log.LogInfo;
import code.ponfee.commons.model.Result;

public class ProtosuffTest {

    @Test
    public void test1() {
        Result<String> obj1 = Result.success("test123");
        Protostuff ser1 = new Protostuff();
        byte[] b1 = ser1.serialize(obj1);

        Tmeta obj2 = new Tmeta(null, "abc", null, true, "#fff");
        Protostuff ser2 = new Protostuff();
        byte[] b2 = ser2.serialize(obj2);

        System.out.println(Jsons.toJson(ser1.deserialize(b2)));
        System.out.println(Jsons.toJson(ser2.deserialize(b1)));
    }

    @Test
    public void test2() {
        Result<String> obj1 = Result.success("test123");
        ProtostuffRedisSerializer<Object> ser1 = new ProtostuffRedisSerializer<>();
        byte[] b1 = ser1.serialize(obj1);

        Tmeta obj2 = new Tmeta(null, "abc", null, true, "#fff");
        ProtostuffRedisSerializer<Object> ser2 = new ProtostuffRedisSerializer<>();
        byte[] b2 = ser2.serialize(obj2);

        System.out.println(Jsons.toJson(ser1.deserialize(b2)));
        System.out.println(Jsons.toJson(ser2.deserialize(b1)));
    }

    // -------------------------------------------------------------------
    @Test
    public void test3() {
        Stopwatch watch = Stopwatch.createStarted();
        final Protostuff ser1 = new Protostuff();
        final Protostuff ser2 = new Protostuff();

        Result<String> obj1 = Result.success("test123");
        //Tmeta obj2 = new Tmeta(null, "abc", null, true, "#fff");
        LogInfo obj2 = new LogInfo("abce");

        try {
            MultithreadExecutor.execAsync(50, () -> {
                System.out.println(Jsons.toJson(ser1.deserialize(ser2.serialize(obj2))));
                System.out.println(Jsons.toJson(ser2.deserialize(ser1.serialize(obj1))));
            }, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(watch.stop());
    }

    @Test
    public void test4() {
        Stopwatch watch = Stopwatch.createStarted();
        ProtostuffRedisSerializer<Object> ser1 = new ProtostuffRedisSerializer<>();
        ProtostuffRedisSerializer<Object> ser2 = new ProtostuffRedisSerializer<>();

        Result<String> obj1 = Result.success("test123");
        //Tmeta obj2 = new Tmeta(null, "abc", null, true, "#fff");
        LogInfo obj2 = new LogInfo("abce");

        try {
            MultithreadExecutor.execAsync(50, () -> {
                System.out.println(Jsons.toJson(ser1.deserialize(ser2.serialize(obj2))));
                System.out.println(Jsons.toJson(ser2.deserialize(ser1.serialize(obj1))));
            }, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println(watch.stop());
    }

}
