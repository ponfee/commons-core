package code.ponfee.commons.util;

import java.io.File;
import java.io.IOException;

import code.ponfee.commons.io.Files;

/**
 * maven标准的项目文件工具类
 * only use in test case
 * 
 * new File("src/test/resources/test.txt");
 * new File("src/test/java/test/test1.java");
 * new File("src/main/resources/log4j2.xml");
 * new File("src/main/java/code/ponfee/commons/util/Asserts.java");
 * 
 * @author Ponfee
 */
public class MavenProjects {

    private static final String EXCLUSION_STRING = "\r|\n"; // "\r|\n|\\s+"

    public static String getProjectBaseDir() {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getFile();
        return new File(path).getParentFile().getParentFile().getPath();
    }

    public static File getMainJavaFile(Class<?> clazz) {
        return new File(getMainJavaPath("") + clazz.getCanonicalName().replace('.', '/') + ".java");
    }

    public static byte[] getMainJavaFileAsByteArray(Class<?> clazz) {
        return Files.toByteArray(MavenProjects.getMainJavaFile(clazz));
    }

    public static String getMainJavaFileAsLineString(Class<?> clazz) {
        try {
            return Files.toString(MavenProjects.getMainJavaFile(clazz)).replaceAll(EXCLUSION_STRING, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getTestJavaFile(Class<?> clazz) {
        return new File(getTestJavaPath("") + clazz.getCanonicalName().replace('.', '/') + ".java");
    }

    public static byte[] getTestJavaFileAsByteArray(Class<?> clazz) {
        return Files.toByteArray(MavenProjects.getTestJavaFile(clazz));
    }

    public static String getTestJavaFileAsLineString(Class<?> clazz) {
        try {
            return Files.toString(MavenProjects.getTestJavaFile(clazz)).replaceAll(EXCLUSION_STRING, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMainJavaPath(String basePackage) {
        return getProjectBaseDir() + "/src/main/java/" + basePackage.replace('.', '/');
    }

    public static String getMainJavaPath(String basePackage, String filename) {
        return getMainJavaPath(basePackage) + "/" + filename;
    }

    public static String getTestJavaPath(String basePackage) {
        return getProjectBaseDir() + "/src/test/java/" + basePackage.replace('.', '/');
    }

    public static String getTestJavaPath(String basePackage, String filename) {
        return getTestJavaPath(basePackage) + "/" + filename;
    }

    public static String getMainResourcesPath() {
        return getProjectBaseDir() + "/src/main/resources/";
    }

    public static String getMainResourcesPath(String followPath) {
        return getMainResourcesPath() + followPath;
    }

    public static String getTestResourcesPath() {
        return getProjectBaseDir() + "/src/test/resources/";
    }

    public static String getTestResourcesPath(String followPath) {
        return getTestResourcesPath() + followPath;
    }

    public static String getWebAppPath() {
        return getProjectBaseDir() + "/src/main/webapp/";
    }

    public static String getWebAppPath(String webappPath) {
        return getWebAppPath() + webappPath;
    }

}
