package code.ponfee.commons.compile.model;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import code.ponfee.commons.compile.impl.JdkCompileTask;

/**
 * java文件编译管理
 * @author fupf
 */
public class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

    private final JdkCompilerClassLoader classLoader;

    private final Map<URI, JavaFileObject> fileObjects = new HashMap<>();

    public JavaFileManagerImpl(JavaFileManager fileManager, JdkCompilerClassLoader classLoader) {
        super(fileManager);
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, 
                                      String relativeName) throws IOException {
        FileObject o = fileObjects.get(uri(location, packageName, relativeName));

        if (o != null) {
            return o;
        }

        return super.getFileForInput(location, packageName, relativeName);
    }

    public void putFileForInput(StandardLocation location, String packageName, 
                                String relativeName, JavaFileObject file) {
        fileObjects.put(uri(location, packageName, relativeName), file);
    }

    private URI uri(Location location, String packageName, String relativeName) {
        return JdkCompileTask.toURI(location.getName() + '/' + packageName + '/' + relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, 
                                               Kind kind, FileObject outputFile) {
        JavaFileObject file = new JavaFileObjectImpl(qualifiedName, kind);
        classLoader.add(qualifiedName, file);
        return file;
    }

    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        return classLoader;
    }

    @Override
    public String inferBinaryName(Location loc, JavaFileObject file) {
        if (file instanceof JavaFileObjectImpl) {
            return file.getName();
        }

        return super.inferBinaryName(loc, file);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, 
                                         Set<Kind> kinds, boolean recurse) throws IOException {
        Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);

        ArrayList<JavaFileObject> files = new ArrayList<>();

        /*List<URL> urlList = new ArrayList<>();
        Enumeration<URL> e = Thread.currentThread().getContextClassLoader().getResources(basePath);
        while (e.hasMoreElements()) {
            urlList.add(e.nextElement());
        }*/

        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            for (JavaFileObject file : fileObjects.values()) {
                if (file.getKind() == Kind.CLASS && file.getName().startsWith(packageName)) {
                    files.add(file);
                }
            }

            files.addAll(classLoader.files());
        } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
            for (JavaFileObject file : fileObjects.values()) {
                if (file.getKind() == Kind.SOURCE && file.getName().startsWith(packageName)) {
                    files.add(file);
                }
            }
        }

        for (JavaFileObject file : result) {
            files.add(file);
        }

        return files;
    }
}
