package code.ponfee.commons.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;

import code.ponfee.commons.extract.DataExtractorBuilder;
import code.ponfee.commons.json.Jsons;

public class FilesTest {

    @Test
    public void test1() throws MalformedURLException {
        System.out.println("fbdp.xml -> "+CharacterEncodingDetector.detect("D:/temp/fbdp.xml"));
        System.out.println("fbdp3.xml -> "+CharacterEncodingDetector.detect("D:/temp/fbdp3.xml"));
        System.out.println("GBK.properties -> "+CharacterEncodingDetector.detect("D:/temp/GBK.properties"));
        System.out.println("UTF8.txt -> "+CharacterEncodingDetector.detect("D:/temp/UTF8.txt"));
        System.out.println("UTF8-WITH-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF8-WITH-BOM.xml"));
        System.out.println("UTF8-WITHOUT-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF8-WITHOUT-BOM.xml"));
        System.out.println("UTF16-BIG-ENDIAN-WITH-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF16-BIG-ENDIAN-WITH-BOM.xml"));
        System.out.println("UTF16-BIG-ENDIAN-WITHOUT-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF16-BIG-ENDIAN-WITHOUT-BOM.xml"));
        System.out.println("UTF16-WITH-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF16-WITH-BOM.xml"));
        System.out.println("UTF16-WITHOUT-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF16-WITHOUT-BOM.xml"));
    }

    @Test
    public void test2() throws IOException {
        Files.removeBOM("D:\\test\\csv-gbk-bom.csv");
        Files.removeBOM("D:\\test\\csv-utf8-bom.csv");
        Files.removeBOM("D:\\test\\csv-utf16-bom.csv");
    }
    
    
    @Test
    public void test3() throws IOException {
        Files.addBOM("D:\\temp\\csv-gbk-bom.csv");
        Files.addBOM("D:\\temp\\csv-utf8-bom.csv");
        Files.addBOM("D:\\temp\\csv-utf16-bom.csv");
    }

    @Test
    public void test4() throws IOException {
        String[] files = { 
            "D:\\temp\\csv-gbk.csv", 
            "D:\\temp\\csv-gbk-bom.csv", 
            
            "D:\\temp\\csv-utf8.csv", 
            "D:\\temp\\csv-utf8-bom.csv", 
            
            "D:\\temp\\csv-utf16.csv" ,
            "D:\\temp\\csv-utf16-bom.csv" 
        };
        for (String file : files) {
            String charset = CharacterEncodingDetector.detect(file);
            System.out.println("\n============================="+file+" -> "+", "+charset+"   "+Files.toString(new File(file)).replaceAll("\r\n|\n", ";"));
            DataExtractorBuilder builder = DataExtractorBuilder.newBuilder(file);
            builder.build().extract((rowNum, row) -> {
                System.out.println(Jsons.toJson(row));
            });
        }
    }
    
    @Test
    public void test5() throws IOException {
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\c24aafd4f3f24c2a86734b20f9a0edd3.Adobe_Fireworks_CS6_XiaZaiBa.exe"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\csv-gbk.csv"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\csv-gbk-bom.csv"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\csv-utf8.csv"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\csv-utf8-bom.csv"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\csv-utf16.csv"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\csv-utf16-bom.csv"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\2.png"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\IMG_2485.JPG"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\ca.pfx"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\signers.xml"));
    }
}
