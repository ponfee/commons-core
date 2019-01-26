package test.compile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import com.google.common.io.Files;

import $._.a.b.n323c23.$._.CompilerSource;
import code.ponfee.commons.compile.model.JavaSource;
import code.ponfee.commons.compile.model.RegexJavaSource;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.util.MavenProjects;

/**
 * 源码编译
 * @author fupf
 */
public class JavaSourceCompilerDemo {
    public static void main(String[] args) throws Exception {
        compile(Files.asCharSource(MavenProjects.getTestJavaFile(CompilerSource.class), Charset.forName("UTF-8")).read());
    }

    @SuppressWarnings({ "unchecked", "resource", "rawtypes" })
    public static void compile(String sourceCode) throws Exception {
        // 1.解析源码
        //JavaSource code = new JavacJavaSource(sourceCode);
        JavaSource code = new RegexJavaSource(sourceCode);

        // 2.开始编译  
        List<JavaFileObject> srcs = Arrays.asList(new JavaStringObject(code.getPublicClass(), sourceCode));
        List<String> options = Arrays.asList("-d", ClassUtils.getClasspath());

        CompilationTask task = ToolProvider.getSystemJavaCompiler().getTask(null, null, null, options, null, srcs);
        if (!task.call()) {
            System.out.println("编译失败");
            System.exit(-1);
        }
        System.out.println("编译成功");

        // 3.加载类
        URL[] urls = new URL[] { new URL("file:/" + ClassUtils.getClasspath()) };
        URLClassLoader classLoader = new URLClassLoader(urls);
        Class classl = classLoader.loadClass(code.getFullyQualifiedName());
        System.out.println(classl == CompilerSource.class); // true
        Method method = classl.getDeclaredMethod("say");
        method.invoke(classl.newInstance());
    }

    private static class JavaStringObject extends SimpleJavaFileObject {
        private String code;

        public JavaStringObject(String name, String code) {
            //super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE); 
            super(URI.create(name + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

}
