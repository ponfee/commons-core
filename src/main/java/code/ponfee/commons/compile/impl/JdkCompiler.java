package code.ponfee.commons.compile.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import code.ponfee.commons.compile.JavaSourceCompiler;
import code.ponfee.commons.compile.exception.CompileExprException;
import code.ponfee.commons.compile.exception.JdkCompileException;
import code.ponfee.commons.compile.model.JavaSource;
import code.ponfee.commons.compile.model.JdkCompilerClassLoader;
import code.ponfee.commons.compile.model.RegexJavaSource;

/**
 * jdk编译
 * @author fupf
 */
public class JdkCompiler implements JavaSourceCompiler {

    private static final Map<ClassLoader, JdkCompilerClassLoader> CLASS_LOADERS = new HashMap<>();

    private List<String> options;

    public JdkCompiler() {
        options = new ArrayList<>();
        //options.add("-target");
        //options.add("1.6");
    }

    @Override
    public Class<?> compile(String sourceString) {
        return compile(new RegexJavaSource(sourceString));
    }

    /**
     * 强制重新编译加载类（创建新的classLoader加载类）
     * @param javaSource
     * @return
     */
    @Override
    public Class<?> compileForce(JavaSource javaSource) {
        return compile(javaSource, new JdkCompilerClassLoader(this.getClass().getClassLoader()));
    }

    /**
     * 编译加载类（如果之前已加载过，则返回）
     * 慎用：如果运行时代码更改，无法重新加载类
     */
    @Override
    public Class<?> compile(JavaSource javaSource) {
        JdkCompilerClassLoader loader = findOrCreate(this.getClass().getClassLoader());
        try {
            Class<?> clazz = Class.forName(javaSource.getFullyQualifiedName(), true, loader);
            if (clazz != null) {
                return clazz; // 已加载，直接返回
            }
        } catch (ClassNotFoundException ignored) {
        }

        return compile(javaSource, loader);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Class<?> compile(JavaSource src, JdkCompilerClassLoader classLoader) {
        try {
            DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<>();
            JdkCompileTask<?> compileTask = new JdkCompileTask(classLoader, options);
            return compileTask.compile(src.getFullyQualifiedName(), src.getSourceCode(), errs);
        } catch (JdkCompileException ex) {
            DiagnosticCollector<JavaFileObject> diagnostics = ex.getDiagnostics();
            throw new CompileExprException("compile error, source : \n" + src
                                         + ", " + diagnostics.getDiagnostics(), ex);
        } catch (Exception ex) {
            throw new CompileExprException("compile error, source : \n" + src, ex);
        }
    }

    public static synchronized JdkCompilerClassLoader findOrCreate(ClassLoader parent) {
        return CLASS_LOADERS.computeIfAbsent(parent, JdkCompilerClassLoader::new);
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

}
