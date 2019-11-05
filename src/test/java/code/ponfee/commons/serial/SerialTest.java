/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.serial;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.collect.ByteArrayWrapper;
import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.jedis.spring.ProtostuffRedisSerializer;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.log.LogInfo;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.util.Convertors;
import code.ponfee.commons.util.SecureRandoms;

/**
 * 
 * 
 * @author Ponfee
 */
public class SerialTest {
    //static long data = Long.MAX_VALUE;
    

    @Test
    @Ignore
    public void test0() {
        Assert.assertNotNull(ClassUtils.newInstance(SerialTest.class, "abc"));
    }

    @Test
    public void test1() {
        Assert.assertNotNull(ClassUtils.newInstance(SerialTest.class));
        Assert.assertNotNull(ClassUtils.newInstance(ByteArrayWrapper.class, SecureRandoms.nextBytes(10)));
        Assert.assertNotNull(ClassUtils.newInstance(Result.class, new Class[] { int.class, String.class }, new Object[] { 1, "a" }));
    }
    
    @Test
    public void test2() {
        Serializer serializer = WrappedSerializer.WRAPPED_KRYO_SERIALIZER;
        //Assert.assertNotNull(serializer.deserialize(serializer.serialize(true), boolean.class));
        //Assert.assertNotNull(serializer.deserialize(serializer.serialize(1), int.class));
        //Assert.assertNotNull(serializer.deserialize(serializer.serialize(ByteArrayWrapper.of(SecureRandoms.nextBytes(10))), ByteArrayWrapper.class));
        //Assert.assertNotNull(serializer.deserialize(serializer.serialize(new Date()), Date.class));
        //Assert.assertNotNull(serializer.deserialize(serializer.serialize("abc"), String.class));
        Assert.assertNotNull(serializer.deserialize(serializer.serialize(Serializers.BOOLEAN), Serializers.class));
        //Assert.assertNotNull(serializer.deserialize(serializer.serialize(Result.SUCCESS), Result.class));
    }

    @Test
    public void testFst() {
        Stopwatch watch = Stopwatch.createStarted();
        FstSerializer ser1 = new FstSerializer();
        FstSerializer ser2 = new FstSerializer();

        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(33, () -> {
            Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(ser1.serialize(Result.success("test123")), Result.class)).getData());
            Assert.assertEquals("abce", ((LogInfo) ser1.deserialize(ser2.serialize(new LogInfo("abce")), LogInfo.class)).getMethodName());
        }, 5);

        System.out.println(watch.stop());
    }

    @Test
    public void testKryo() {
        Stopwatch watch = Stopwatch.createStarted();
        KryoSerializer ser1 = new KryoSerializer();
        KryoSerializer ser2 = new KryoSerializer();

        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(33, () -> {
            Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(ser1.serialize(Result.success("test123")), Result.class)).getData());
            Assert.assertEquals("abce", ((LogInfo) ser1.deserialize(ser2.serialize(new LogInfo("abce")), LogInfo.class)).getMethodName());
        }, 5);

        System.out.println(watch.stop());
    }
    
    @Test
    public void testJdk() {
        Stopwatch watch = Stopwatch.createStarted();
        JdkSerializer ser1 = new JdkSerializer();
        JdkSerializer ser2 = new JdkSerializer();

        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(33, () -> {
            Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(ser1.serialize(Result.success("test123")), Result.class)).getData());
            Assert.assertEquals("abce", ((LogInfo) ser1.deserialize(ser2.serialize(new LogInfo("abce")), LogInfo.class)).getMethodName());
        }, 5);

        System.out.println(watch.stop());
    }
    
    @Test
    public void testProtostuff() {
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
    public void testProtostuffRedis() {
        Stopwatch watch = Stopwatch.createStarted();
        ProtostuffRedisSerializer<Object> ser1 = new ProtostuffRedisSerializer<>();
        ProtostuffRedisSerializer<Object> ser2 = new ProtostuffRedisSerializer<>();

        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(33, () -> {
            Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(ser1.serialize(Result.success("test123")))).getData());
            Assert.assertEquals("abce", ((LogInfo) ser1.deserialize(ser2.serialize(new LogInfo("abce")))).getMethodName());
        }, 5);

        System.out.println(watch.stop());
    }
    
    @Test
    public void testGeneral() {
        Stopwatch watch = Stopwatch.createStarted();
        WrappedSerializer ser1 = WrappedSerializer.WRAPPED_KRYO_SERIALIZER;
        WrappedSerializer ser2 = WrappedSerializer.WRAPPED_KRYO_SERIALIZER;

        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(33, () -> {
            Assert.assertEquals("test123", ((Result<String>) ser2.deserialize(ser1.serialize(Result.success("test123")), Result.class)).getData());
            Assert.assertEquals("abce", ((LogInfo) ser1.deserialize(ser2.serialize(new LogInfo("abce")), LogInfo.class)).getMethodName());
        }, 5);

        System.out.println(watch.stop());
    }
    
    @Test
    public void testFst2() {
        Stopwatch watch = Stopwatch.createStarted();
        FstSerializer ser1 = new FstSerializer();
        FstSerializer ser2 = new FstSerializer();

        // XXX: if Executor is CALLER_RUN_EXECUTOR and threadNumber>=33 then will be dead loop
        MultithreadExecutor.execAsync(33, () -> {
            Assert.assertEquals((Integer)1, (Integer) ser2.deserialize(ser1.serialize(1), Integer.class));
        }, 5);

        System.out.println(watch.stop());
    }
    
    @Test
    public void test3() {
        Map<String, Object> data = Jsons.fromJson("{\"destName\":\"深圳市\",\"consignedTime\":1568891175000,\"paymentTypeCode\":\"1\",\"limitTypeCode\":\"T4\",\"addresseeDeptCode\":\"755U\",\"paymentType\":\"寄付\",\"senderArea\":\"福田区\",\"consNamesStr\":\"按摩椅\",\"custNo\":\"9999999999\",\"expressType\":\"快件\",\"consNames\":[\"按摩椅\"],\"senderProvince\":\"广东省\",\"receiverArea\":\"福田区\",\"consignedDate\":\"2019-09-19\",\"limitType\":\"标准快递\",\"destProvince\":\"广东省\",\"consQty\":0.0,\"senderContact\":\"赵小薇\",\"expressTypeCode\":\"B1\",\"consignorDeptCode\":\"755U\",\"senderDeptName\":\"兰天路速运营业部\",\"sendPartName\":\"兰天路速运营业部\",\"sourceName\":\"深圳市\",\"subSize\":0,\"waybillNo\":\"444145693922\",\"flag\":true}", Map.class);
        System.out.println(data);
        
        WrappedSerializer generalSerializer = WrappedSerializer.WRAPPED_KRYO_SERIALIZER;
        KryoSerializer KryoSerializer = new KryoSerializer();
        FstSerializer FstSerializer = new FstSerializer();
        JdkSerializer JdkSerializer = new JdkSerializer();
        HessianSerializer HessianSerializer = new HessianSerializer();
        ProtostuffSerializer ProtostuffSerializer = new ProtostuffSerializer();
        JsonSerializer JsonSerializer = new JsonSerializer();

        System.out.println(generalSerializer.serialize(data).length);
        System.out.println(KryoSerializer.serialize(data).length);
        System.out.println(FstSerializer.serialize(data).length);
        System.out.println(JdkSerializer.serialize(data).length);
        System.out.println(HessianSerializer.serialize(data).length);
        System.out.println(ProtostuffSerializer.serialize(data).length);
        System.out.println(JsonSerializer.serialize(data).length);
        
        
        
        System.out.println();
        System.out.println(generalSerializer.deserialize(generalSerializer.serialize(data), HashMap.class)); // KryoException: Class cannot be created (missing no-arg constructor): java.util.Map
        System.out.println(KryoSerializer.deserialize(KryoSerializer.serialize(data), HashMap.class));
        System.out.println(FstSerializer.deserialize(FstSerializer.serialize(data), Map.class));
        System.out.println(JdkSerializer.deserialize(JdkSerializer.serialize(data), Map.class));
        System.out.println(HessianSerializer.deserialize(HessianSerializer.serialize(data), Map.class));
        System.out.println(ProtostuffSerializer.deserialize(ProtostuffSerializer.serialize(data), HashMap.class)); // RuntimeException: The root object can neither be an abstract class nor interface: "java.util.Map
        System.out.println(JsonSerializer.deserialize(JsonSerializer.serialize(data), Map.class));
        
    }
    
    @Test
    public void test5() {
        long data = Long.MAX_VALUE;
        System.out.println(data);
        
        WrappedSerializer generalSerializer = WrappedSerializer.WRAPPED_KRYO_SERIALIZER;
        KryoSerializer KryoSerializer = new KryoSerializer();
        FstSerializer FstSerializer = new FstSerializer();
        JdkSerializer JdkSerializer = new JdkSerializer();
        HessianSerializer HessianSerializer = new HessianSerializer();
        ProtostuffSerializer ProtostuffSerializer = new ProtostuffSerializer();
        JsonSerializer JsonSerializer = new JsonSerializer();
        ToStringSerializer ToStringSerializer = new ToStringSerializer();

        System.out.println(generalSerializer.serialize(data).length);
        System.out.println(KryoSerializer.serialize(data).length);
        System.out.println(FstSerializer.serialize(data).length);
        System.out.println(JdkSerializer.serialize(data).length);
        System.out.println(HessianSerializer.serialize(data).length);
        System.out.println(ProtostuffSerializer.serialize(data).length);
        System.out.println(JsonSerializer.serialize(data).length);
        System.out.println(ToStringSerializer.serialize(data).length);
        
        
        
        System.out.println();
        System.out.println(generalSerializer.deserialize(generalSerializer.serialize(data), long.class));
        System.out.println(KryoSerializer.deserialize(KryoSerializer.serialize(data), long.class));
        System.out.println(FstSerializer.deserialize(FstSerializer.serialize(data), long.class));
        System.out.println(JdkSerializer.deserialize(JdkSerializer.serialize(data), long.class));
        System.out.println(HessianSerializer.deserialize(HessianSerializer.serialize(data), long.class));
        System.out.println(ProtostuffSerializer.deserialize(ProtostuffSerializer.serialize(data), long.class)); // long: java.lang.RuntimeException: The root object can neither be an abstract class nor interface: "long", RuntimeSchema.createFrom
        System.out.println(JsonSerializer.deserialize(JsonSerializer.serialize(data), long.class));
        System.out.println(ToStringSerializer.deserialize(ToStringSerializer.serialize(data), long.class));
    }
    
    @Test
    public void test6() {
        ToStringSerializer ser = new ToStringSerializer();
        System.out.println(ser.deserialize(ser.serialize(1), int.class));
        System.out.println(ser.deserialize(ser.serialize(true), boolean.class));
    }

    @Test
    public void test7() {
        System.out.println(Serializers.of(int.class));
        System.out.println(Convertors.of(int.class));

        System.out.println(Integer.class.isInstance(1));
        System.out.println(Long.class.isInstance(1));
        System.out.println(Long.class.isInstance(1L));

    }
}
