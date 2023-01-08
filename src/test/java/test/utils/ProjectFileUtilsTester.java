package test.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import cn.ponfee.commons.util.MavenProjects;

public class ProjectFileUtilsTester {

    @Test
    public void testGetMainJavaFile() {
        File file = MavenProjects.getMainJavaFile(MavenProjects.class);
        printFile(file);
    }

    @Test
    public void testGetTestJavaFile() {
        File file = MavenProjects.getTestJavaFile(ProjectFileUtilsTester.class);
        printFile(file);
    }

    @Test
    public void testGetMainJavaPath() {
        String Path = MavenProjects.getMainJavaPath("test", "TestUtils.java");
        printFile(Path);
    }

    @Test
    public void testGetMainResourcesPath() {
        String Path = MavenProjects.getMainResourcesPath("log4j.properties");
        printFile(Path);
    }

    @Test
    public void testGetTestResourcesPath() {
        String Path = MavenProjects.getTestResourcesPath("redis-script-node.lua");
        printFile(Path);
    }

    private void printFile(String filepath) {
        printFile(new File(filepath));
    }

    private void printFile(File file) {
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /*String s = new String(toByteArray(new URL("file:///d:/github/springmvc-demo/src/test/resources/ftl/macro.ftl").openStream()));
        System.out.println(s);*/
        String filename = "D:\\github\\jedis-clients\\src\\main\\java\\test\\TestUtils.java";
        System.out.println(FilenameUtils.getBaseName(filename));
        System.out.println(FilenameUtils.getExtension(filename));
        System.out.println(FilenameUtils.getFullPath(filename));
        System.out.println(FilenameUtils.getFullPathNoEndSeparator(filename));
        System.out.println(FilenameUtils.getName(filename));
        System.out.println(FilenameUtils.getPath(filename));
        System.out.println(FilenameUtils.getPathNoEndSeparator(filename));
        System.out.println(FilenameUtils.getPrefix(filename));
        System.out.println(FilenameUtils.getPrefixLength(filename));
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        int n;
        while (-1 != (n = in.read(buff))) {
            out.write(buff, 0, n);
        }
        return out.toByteArray();
    }

}
