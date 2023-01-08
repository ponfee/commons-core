package cn.ponfee.commons.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import cn.ponfee.commons.util.MavenProjects;
import org.junit.Test;

public class WindowsBomTest {

    @Test
    public void testhas() throws IOException {
        System.out.println(ByteOrderMarks.has(MavenProjects.getTestJavaFile(WindowsBomTest.class)));
        System.out.println(ByteOrderMarks.has("D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(ByteOrderMarks.has("D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(ByteOrderMarks.has("D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }

    @Test
    public void testadd() throws IOException {
        System.out.println(ByteOrderMarks.add("D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(ByteOrderMarks.add("D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(ByteOrderMarks.add("D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }
    
    @Test
    public void testremove() throws IOException {
        System.out.println(ByteOrderMarks.remove("D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(ByteOrderMarks.remove("D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(ByteOrderMarks.remove("D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }
    
    @Test
    public void testaddCharset() throws IOException {
        System.out.println(ByteOrderMarks.add(StandardCharsets.UTF_8,  "D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(ByteOrderMarks.add(StandardCharsets.UTF_16LE,"D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(ByteOrderMarks.add(StandardCharsets.UTF_16BE,"D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }
    
    @Test
    public void testremoveCharset() throws IOException {
        System.out.println(ByteOrderMarks.remove(StandardCharsets.UTF_8,"D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(ByteOrderMarks.remove(StandardCharsets.UTF_16LE,"D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(ByteOrderMarks.remove(StandardCharsets.UTF_16BE,"D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }
}
