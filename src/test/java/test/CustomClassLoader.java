package test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.google.common.io.Files;

public class CustomClassLoader extends ClassLoader {

    private final byte[] classBytes;

    public CustomClassLoader() {
        this.classBytes = null;
    }

    public CustomClassLoader(File classFile) throws IOException {
        this.classBytes = Files.toByteArray(classFile);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return defineClass(name, classBytes, 0, classBytes.length);
    }

    public static void main(String[] args) throws Exception {
        CustomClassLoader classLoader1 = new CustomClassLoader();
        CustomClassLoader classLoader2 = new CustomClassLoader(new File("D:\\test\\ToolProvider.class"));

        System.out.println(Arrays.toString(classLoader1.loadClass("javax.tools.ToolProvider").getDeclaredMethods()));
        System.out.println("\n============================================");

        Class<?> objClass = classLoader2.findClass("javax.tools.ToolProvider");
        System.out.println(Arrays.toString(objClass.getDeclaredMethods()));
        objClass.getDeclaredMethod("say").invoke(objClass.newInstance());
        
        
        code.ponfee.commons.io.Files.touch(new File("D:\\test\\a\\b\\ToolProvider1.class"));
    }
}
