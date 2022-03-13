package code.ponfee.commons.resource;

import code.ponfee.commons.io.Closeables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * 类资源加载器
 * 
 * @author Ponfee
 */
final class ClassPathResourceLoader {

    private static final String URL_PROTOCOL_FILE = "file";
    private static final String URL_PROTOCOL_JAR = "jar";
    private static final String URL_PROTOCOL_ZIP = "zip";
    private static final String JAR_URL_SEPARATOR = "!/";

    private static final Logger LOG = LoggerFactory.getLogger(ClassPathResourceLoader.class);

    /**
     * 加载资源文件
     * @param filePath
     * @param contextClass
     * @param encoding
     * @return
     */
    Resource getResource(String filePath, Class<?> contextClass, String encoding) {
        Enumeration<URL> urls;
        JarFile jar = null;
        ZipFile zip = null;
        try {
            if (contextClass != null) {
                urls = contextClass.getClassLoader().getResources(filePath);
            } else {
                urls = Thread.currentThread().getContextClassLoader().getResources(filePath);
            }

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement(); // 获取下一个元素
                switch (url.getProtocol()) {
                    case URL_PROTOCOL_FILE:
                        String path = URLDecoder.decode(url.getFile(), encoding);
                        // 判断是否是指定类所在Jar包中的文件：path.length()-filePath.length() == path.lastIndexOf(filePath)
                        if (checkWithoutClass(contextClass, path.substring(0, path.length() - filePath.length()), encoding)) {
                            continue;
                        }
                        return new Resource(path, new File(path).getName(), new FileInputStream(path));
                    case URL_PROTOCOL_JAR:
                        jar = ((JarURLConnection) url.openConnection()).getJarFile(); // 获取jar
                        // 判断是否是指定类所在Jar包中的文件
                        if (checkWithoutClass(contextClass, jar.getName(), encoding)) {
                            continue;
                        }
                        Enumeration<JarEntry> entries = jar.entries(); // 从此jar包 得到一个枚举类
                        while (entries.hasMoreElements()) { // 进行循环迭代
                            JarEntry entry = entries.nextElement(); // 获取jar里的一个实体：可以是目录或其他如META-INF等文件
                            if (!filePath.equals(entry.getName())) {
                                continue;
                            }
                            String fileName = entry.getName();
                            fileName = fileName.replace("\\", "/");
                            if (fileName.contains("/")) {
                                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                            }
                            return new Resource(URLDecoder.decode(url.getFile(), encoding), 
                                                fileName, transform(jar.getInputStream(entry)));
                        }
                        jar.close();
                        jar = null;
                        break;
                    case URL_PROTOCOL_ZIP: // as a zip file in weblogic environment
                        String zipPath = URLDecoder.decode(url.getFile(), encoding);
                        if (zipPath.startsWith(ResourceLoaderFacade.FS_PREFIX)) {
                            zipPath = zipPath.substring(ResourceLoaderFacade.FS_PREFIX.length());
                        }
                        if (!zipPath.contains(JAR_URL_SEPARATOR)) {
                            continue;
                        }
                        zipPath = zipPath.substring(0, zipPath.lastIndexOf(JAR_URL_SEPARATOR));
                        // 判断是否是指定类所在Jar包中的文件
                        if (checkWithoutClass(contextClass, zipPath, encoding)) {
                            continue;
                        }
                        zip = new ZipFile(zipPath);
                        // org.apache.tools.zip.ZipEntry;
                        // org.apache.tools.zip.ZipFile;
                        //Enumeration<ZipEntry> entries = zip.getEntries();
                        Enumeration<? extends ZipEntry> entries0 = zip.entries();
                        while (entries0.hasMoreElements()) {
                            ZipEntry entry = entries0.nextElement();
                            if (!filePath.equals(entry.getName())) {
                                continue;
                            }
                            String fileName = entry.getName();
                            fileName = fileName.replace("\\", "/");
                            if (fileName.contains("/")) {
                                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                            }
                            return new Resource(
                                URLDecoder.decode(url.getFile(), encoding), 
                                fileName, transform(zip.getInputStream(entry))
                            );
                        }
                        zip.close();
                        zip = null;
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported protocol: " + url.getProtocol());
                }
            }
            return null;
        } catch (IOException e) {
            LOG.error("load resource from jar file occur error", e);
            return null;
        } finally {
            Closeables.console(jar);
            Closeables.console(zip);
        }
    }

    List<Resource> listResources(String directory, String[] extensions, boolean recursive, 
                                 Class<?> contextClass, String encoding) {
        List<Resource> list = new ArrayList<>();
        JarFile jar = null;
        ZipFile zip = null;
        Enumeration<URL> dirs;
        try {
            if (contextClass != null) {
                dirs = contextClass.getClassLoader().getResources(directory);
            } else {
                dirs = Thread.currentThread().getContextClassLoader().getResources(directory);
            }

            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                switch (url.getProtocol()) {
                    case URL_PROTOCOL_FILE:
                        String path = URLDecoder.decode(url.getFile(), encoding);
                        // 判断是否是指定类所在Jar包中的文件：path.length()-directory.length() == path.lastIndexOf(directory)
                        if (checkWithoutClass(contextClass, path.substring(0, path.length() - directory.length()), encoding)) {
                            continue;
                        }
                        Collection<File> files = FileUtils.listFiles(new File(path), extensions, recursive);
                        if (files != null && !files.isEmpty()) {
                            for (File file : files) {
                                list.add(new Resource(file.getAbsolutePath(), file.getName(), new FileInputStream(file)));
                            }
                        }
                        break;
                    case URL_PROTOCOL_JAR:
                        jar = ((JarURLConnection) url.openConnection()).getJarFile(); // 读取Jar包
                        // 判断是否是指定类所在Jar包中的文件
                        if (checkWithoutClass(contextClass, jar.getName(), encoding)) {
                            continue;
                        }
                        Enumeration<JarEntry> entries = jar.entries(); // 从此jar包 得到一个枚举类
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.isDirectory()) {
                                continue;
                            }
                            String name = entry.getName();

                            // 1、全目录匹配 或 当可递归时子目录
                            // 2、匹配后缀
                            int idx = name.lastIndexOf('/');
                            boolean isDir = (idx != -1 && name.substring(0, idx).equals(directory)) 
                                            || (recursive && name.startsWith(directory));
                            boolean isSufx = isEmpty(extensions) 
                                             || name.toLowerCase().matches("^(.+\\.)(" + join(extensions, "|") + ")$");
                            if (isDir && isSufx) {
                                list.add(new Resource(URLDecoder.decode(url.getFile(), encoding), 
                                                      entry.getName(), 
                                                      transform(jar.getInputStream(entry))));
                            }
                        }
                        jar.close();
                        jar = null;
                        break;
                    case URL_PROTOCOL_ZIP: // weblogic is zip file
                        String zipPath = URLDecoder.decode(url.getFile(), encoding);
                        if (zipPath.startsWith(ResourceLoaderFacade.FS_PREFIX)) {
                            zipPath = zipPath.substring(ResourceLoaderFacade.FS_PREFIX.length());
                        }
                        if (!zipPath.contains(JAR_URL_SEPARATOR)) {
                            continue;
                        }
                        zipPath = zipPath.substring(0, zipPath.lastIndexOf(JAR_URL_SEPARATOR));
                        // 判断是否是指定类所在Jar包中的文件
                        if (checkWithoutClass(contextClass, zipPath, encoding)) {
                            continue;
                        }
                        zip = new ZipFile(zipPath);
                        //Enumeration<ZipEntry> entries = zip.getEntries();
                        Enumeration<? extends ZipEntry> entries0 = zip.entries();
                        while (entries0.hasMoreElements()) {
                            ZipEntry entry = entries0.nextElement();
                            String name = entry.getName();
                            int idx = name.lastIndexOf('/');
                            // 1、全目录匹配 或 当可递归时子目录
                            // 2、匹配后缀
                            boolean isDir = (idx != -1 && name.substring(0, idx).equals(directory)) 
                                            || (recursive && name.startsWith(directory));
                            boolean isSuffix = isEmpty(extensions) 
                                            || name.toLowerCase().matches("^(.+\\.)(" + join(extensions, "|") + ")$");
                            if (isDir && isSuffix) {
                                list.add(new Resource(
                                    URLDecoder.decode(url.getFile(), encoding), 
                                    entry.getName(), transform(zip.getInputStream(entry))
                                ));
                            }
                        }
                        zip.close();
                        zip = null;
                        break;
                    default:
                        throw new UnsupportedOperationException("un supported process " + url.getProtocol());
                }
            }
            return list;
        } catch (IOException e) {
            LOG.error("load resource from jar file occur error", e);
            return list;
        } finally {
            Closeables.console(jar);
            Closeables.console(zip);
        }
    }

    /**
     * 判断资源文件是否在contextClass的classpath中（jar包或class目录）
     * @param contextClass
     * @param filepath
     * @param encoding
     * @return
     * @throws IOException
     */
    private static boolean checkWithoutClass(Class<?> contextClass, String filepath,
                                             String encoding) throws IOException {
        if (contextClass == null) {
            return false;
        }

        String destPath = contextClass.getProtectionDomain().getCodeSource().getLocation().getFile();
        destPath = URLDecoder.decode(destPath, encoding);
        return !new File(destPath).getCanonicalFile().equals(new File(filepath).getCanonicalFile());
    }

    /**
     * 流转换
     * @param input
     * @return
     * @throws IOException
     */
    private static ByteArrayInputStream transform(InputStream input) throws IOException {
        if (input instanceof ByteArrayInputStream) {
            return (ByteArrayInputStream) input;
        }
        try {
            return new ByteArrayInputStream(IOUtils.toByteArray(input));
        } finally {
            Closeables.console(input);
        }
    }

}
