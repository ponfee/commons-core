package test.reflect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.io.IOUtils;

import code.ponfee.commons.util.Bytes;

public class ProxyTest {

    @SuppressWarnings("restriction")
    public static void main(String[] args) throws IOException {
        Echo target = new MountainEcho();
        EchoInvocationHandler handler = new EchoInvocationHandler(target);
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { Echo.class }, handler);
        String proxyRtn = ((Echo) proxy).echo("hello");

        System.out.println(proxy.getClass()); // com.sun.proxy.$Proxy0
        System.out.println(proxyRtn); // return EchoInvocationHandler.invoke result

        System.out.println("\n==============================导出代理类");

        // 根据类信息和提供的代理类名称，生成字节码  
        byte[] classBytes = sun.misc.ProxyGenerator.generateProxyClass("ProxyClass", proxy.getClass().getInterfaces());

        // 打印字节码
        System.out.println(Bytes.hexDump(classBytes));

        // 导出到文件
        IOUtils.write(classBytes, new FileOutputStream(new File("d:/ProxyClass.class"))); // TODO 用jd-gui反编译ProxyClass.class文件
    }

    // -----------------------------------------------------------------------------------------------------------------------------
    public static interface Echo {
        String echo(String s);
    }

    public static class MountainEcho implements Echo {
        @Override
        public String echo(String saying) {
            return saying + " " + saying + " " + saying + " ...";
        }
    }

    public static class EchoInvocationHandler implements InvocationHandler {
        private final Echo target;

        public EchoInvocationHandler(Echo target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // proxy <= Proxy.newProxyInstance(loader, interfaces, h);
            System.out.println("shout: " + args[0]);
            System.out.println("echo : " + method.invoke(target, args) + ".");
            return "return ((Echo) proxy).echo(\"hello\")";
        }
    }

}
