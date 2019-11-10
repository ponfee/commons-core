package code.ponfee.commons.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class WindowsBomTest {

    @Test
    public void testHasBOM() throws IOException {
        System.out.println(WindowsBOM.hasBOM("D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(WindowsBOM.hasBOM("D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(WindowsBOM.hasBOM("D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }

    @Test
    public void testAddBOM() throws IOException {
        System.out.println(WindowsBOM.addBOM("D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(WindowsBOM.addBOM("D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(WindowsBOM.addBOM("D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }
    
    @Test
    public void testRemoveBOM() throws IOException {
        System.out.println(WindowsBOM.removeBOM("D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(WindowsBOM.removeBOM("D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(WindowsBOM.removeBOM("D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }
    
    @Test
    public void testAddBOMCharset() throws IOException {
        System.out.println(WindowsBOM.addBOM(StandardCharsets.UTF_8,  "D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(WindowsBOM.addBOM(StandardCharsets.UTF_16LE,"D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(WindowsBOM.addBOM(StandardCharsets.UTF_16BE,"D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }
    
    @Test
    public void testRemoveBOMCharset() throws IOException {
        System.out.println(WindowsBOM.removeBOM(StandardCharsets.UTF_8,"D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(WindowsBOM.removeBOM(StandardCharsets.UTF_16LE,"D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(WindowsBOM.removeBOM(StandardCharsets.UTF_16BE,"D:\\temp\\withbom\\csv-utf16be-bom.csv"));
    }
}
