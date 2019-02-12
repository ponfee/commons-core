
package test.compile;

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.io.Files;

import $._.a.b.n323c23.$._.CompilerSource;
import code.ponfee.commons.compile.impl.GroovyCompiler;
import code.ponfee.commons.compile.impl.JdkCompiler;
import code.ponfee.commons.compile.model.JavaSource;
import code.ponfee.commons.compile.model.JavacJavaSource;
import code.ponfee.commons.compile.model.RegexJavaSource;
import code.ponfee.commons.util.MavenProjects;
import test.concurrent.TestThread;

public class JavaSourceCompilerTester {

    @Test
    public void testJdk() throws Exception {
        //Class<?> _clazz = JavaSourceCompilerTester.class;
        Class<?> _clazz = CompilerSource.class;
        String sourceCode = Files.asCharSource(MavenProjects.getTestJavaFile(_clazz), Charset.forName("UTF-8")).read();

        //JavaSource javaSource = new JavacJavaSource(sourceCode);
        JavaSource javaSource = new RegexJavaSource(sourceCode);

        System.out.println("packageName:" + javaSource.getPackageName());
        System.out.println("className:" + javaSource.getPublicClass());
        System.out.println(RegexJavaSource.QUALIFIER_PATTERN.matcher(javaSource.getFullyQualifiedName()).matches());

        Class<?> clazz = new JdkCompiler().compile(javaSource);

        String s = "_clazz==clazz --> " + (_clazz == clazz) + "  ";
        System.out.println(s + _clazz.getClassLoader().getClass());
        System.out.println(StringUtils.leftPad(" ", s.length()) + clazz.getClassLoader().getClass());

        clazz.getMethod("say").invoke(clazz);
    }

    @Test
    public void testGroovy() throws Exception {
        Class<?> _clazz = JavaSourceCompilerTester.class;
        //Class<?> _clazz = CompilerSource.class; // error
        String sourceCode = Files.asCharSource(MavenProjects.getTestJavaFile(_clazz), Charset.forName("UTF-8")).read();

        //JavaSource javaSource = new JavacJavaSource(sourceCode);
        JavaSource javaSource = new RegexJavaSource(sourceCode);

        System.out.println("packageName:" + javaSource.getPackageName());
        System.out.println("className:" + javaSource.getPublicClass());

        Class<?> clazz = new GroovyCompiler().compile(javaSource);

        String s = "_clazz==clazz --> " + (_clazz == clazz) + "  ";
        System.out.println(s + _clazz.getClassLoader().getClass());
        System.out.println(StringUtils.leftPad(" ", s.length()) + clazz.getClassLoader().getClass());

        clazz.getMethod("say").invoke(clazz);
    }

    public @Test void testCompileForce() throws Exception {
        String sourceCode = Files.asCharSource(MavenProjects.getTestJavaFile(TestThread.class), Charset.forName("UTF-8")).read();
        JavaSource javaSource = new JavacJavaSource(sourceCode);
        System.out.println(javaSource.getFullyQualifiedName());

        Class<?> clazz = new JdkCompiler().compile(javaSource);
        //clazz.getMethod("sayHello").invoke(clazz);

        Class<?> clazz2 = new JdkCompiler().compile(javaSource);
        System.out.println(clazz == clazz2);
        Class<?> clazz3 = new JdkCompiler().compileForce(javaSource);
        System.out.println(clazz == clazz3);
        Class<?> clazz4 = new JdkCompiler().compileForce(javaSource);
        System.out.println(clazz3 == clazz4);
    }

    public static void say() {
        System.out.println("hello world!");
    }
}
