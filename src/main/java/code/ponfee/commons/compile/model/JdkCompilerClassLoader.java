package code.ponfee.commons.compile.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.tools.JavaFileObject;

/**
 * 编译加载类
 * @author fupf
 */
public final class JdkCompilerClassLoader extends ClassLoader {

    private final Map<String, JavaFileObject> classes = new HashMap<>();

    public JdkCompilerClassLoader(ClassLoader parentClassLoader){
        super(parentClassLoader);
    }

    public Collection<JavaFileObject> files() {
        return Collections.unmodifiableCollection(classes.values());
    }

    public void clearCache() {
        this.classes.clear();
    }

    public JavaFileObject getJavaFileObject(String qualifiedClassName) {
        return classes.get(qualifiedClassName);
    }

    @Override
    protected synchronized Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
        JavaFileObject file = classes.get(qualifiedClassName);
        if (file != null) {
            byte[] bytes = ((JavaFileObjectImpl) file).getByteCode();
            return defineClass(qualifiedClassName, bytes, 0, bytes.length);
        }

        try {
            return Class.forName(qualifiedClassName);
        } catch (ClassNotFoundException e) {
            // Ignore and fall through
        }

        try {
            return Thread.currentThread().getContextClassLoader().loadClass(qualifiedClassName);
        } catch (ClassNotFoundException e) {
            // Ignore and fall through
        }

        return super.findClass(qualifiedClassName);
    }

    public void add(String qualifiedClassName, final JavaFileObject javaFile) {
        classes.put(qualifiedClassName, javaFile);
    }

    @Override
    protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        try {
            Class<?> c = findClass(name);
            if (c != null) {
                if (resolve) {
                    resolveClass(c);
                }

                return c;
            }
        } catch (ClassNotFoundException e) {
            // Ignore and fall through
        }

        return super.loadClass(name, resolve);
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        if (name.endsWith(".class")) {
            String qualifiedClassName = name.substring(0, name.length() - ".class".length()).replace('/', '.');
            JavaFileObjectImpl file = (JavaFileObjectImpl) classes.get(qualifiedClassName);

            if (file != null) {
                return new ByteArrayInputStream(file.getByteCode());
            }
        }

        return super.getResourceAsStream(name);
    }

}
