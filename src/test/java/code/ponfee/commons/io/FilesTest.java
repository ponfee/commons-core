package code.ponfee.commons.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.junit.Test;

import code.ponfee.commons.extract.DataExtractorBuilder;
import code.ponfee.commons.json.Jsons;

public class FilesTest {

    @Test
    public void test1() throws MalformedURLException {
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
    public void test0() throws IOException {
        System.out.println(ByteOrderMarks.has("D:\\temp\\withbom\\csv-gbk-bom.csv"));
        System.out.println(ByteOrderMarks.has("D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(ByteOrderMarks.has("D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(ByteOrderMarks.has("D:\\temp\\withbom\\csv-utf16be-bom.csv"));
        System.out.println(ByteOrderMarks.has("D:\\temp\\withbom\\csv-unicode-ascii-escaped-bom.csv"));
        System.out.println(ByteOrderMarks.has("D:\\temp\\withbom\\csv-ansi-ascii-bom.csv"));
    }

    @Test
    public void test2() throws IOException {
        /*System.out.println(WindowsBOM.add("D:\\temp\\withbom\\csv-gbk-bom.csv"));
        System.out.println(WindowsBOM.add("D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(WindowsBOM.add("D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(WindowsBOM.add("D:\\temp\\withbom\\csv-utf16be-bom.csv"));
        System.out.println(WindowsBOM.add("D:\\temp\\withbom\\csv-unicode-ascii-escaped-bom.csv"));
        System.out.println(WindowsBOM.add("D:\\temp\\withbom\\csv-ansi-ascii-bom.csv"));*/
        
        System.out.println(ByteOrderMarks.add(StandardCharsets.UTF_8, "D:\\temp\\withoutbom\\test-utf8-bom.csv"));
        System.out.println(ByteOrderMarks.add(StandardCharsets.UTF_16LE, "D:\\temp\\withoutbom\\test-utf16le-bom.csv"));
        System.out.println(ByteOrderMarks.add(StandardCharsets.UTF_16BE, "D:\\temp\\withoutbom\\test-utf16be-bom.csv"));
    }

    @Test
    public void test3() throws IOException {
        System.out.println(ByteOrderMarks.remove("D:\\temp\\withbom\\csv-gbk-bom.csv"));
        System.out.println(ByteOrderMarks.remove("D:\\temp\\withbom\\csv-utf8-bom.csv"));
        System.out.println(ByteOrderMarks.remove("D:\\temp\\withbom\\csv-utf16le-bom.csv"));
        System.out.println(ByteOrderMarks.remove("D:\\temp\\withbom\\csv-utf16be-bom.csv"));
        System.out.println(ByteOrderMarks.remove("D:\\temp\\withbom\\csv-unicode-ascii-escaped-bom.csv"));
        System.out.println(ByteOrderMarks.remove("D:\\temp\\withbom\\csv-ansi-ascii-bom.csv"));
    }

    @Test
    public void test4() throws IOException {
        String[] files = { 
            "D:\\temp\\withoutbom\\csv-gbk.csv", 
            "D:\\temp\\withbom\\csv-gbk-bom.csv", 

            "D:\\temp\\withoutbom\\csv-utf8.csv", 
            "D:\\temp\\withbom\\csv-utf8-bom.csv", 

            "D:\\temp\\withoutbom\\csv-utf16le.csv", 
            "D:\\temp\\withbom\\csv-utf16le-bom.csv", 

            "D:\\temp\\withoutbom\\csv-utf16be.csv", 
            "D:\\temp\\withbom\\csv-utf16be-bom.csv", 

            "D:\\temp\\withoutbom\\csv-unicode-ascii-escaped.csv", 
            "D:\\temp\\withbom\\csv-unicode-ascii-escaped-bom.csv", 

            "D:\\temp\\withoutbom\\csv-ansi-ascii.csv", 
            "D:\\temp\\withbom\\csv-ansi-ascii-bom.csv", 
        };
        for (String file : files) {
            Charset charset = CharacterEncodingDetector.detect(file);
            System.out.println("\n============================="+file+" -> "+", "+charset+"   "+Files.toString(new File(file)).replaceAll("\r\n|\n", ";"));
            DataExtractorBuilder builder = DataExtractorBuilder.newBuilder(file);
            builder.build().extract(100).stream().forEach(row -> System.out.println(Jsons.toJson(row)));
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
    
    @Test
    public void test6() throws IOException {
        System.out.println(Charset.forName("utf-8") == StandardCharsets.UTF_8);
        System.out.println(Charset.forName("utf8") == StandardCharsets.UTF_8);
        System.out.println(Charset.forName("utf-16") == StandardCharsets.UTF_16);
    }

    @Test
    public void test7() throws IOException {
        String[] files = { 
            "D:\\temp\\withoutbom\\test-utf8-bom.csv", 

            "D:\\temp\\withoutbom\\test-utf16le-bom.csv", 

            "D:\\temp\\withoutbom\\test-utf16be-bom.csv", 
        };
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');
        for (String file : files) {
            Charset charset = CharacterEncodingDetector.detect(file);
            System.out.println("\n============================="+file+" -> "+", "+charset+"   "+Files.toString(new File(file)).substring(1, 1000).replaceAll("\r\n|\n", ";"));
            DataExtractorBuilder builder = DataExtractorBuilder.newBuilder(file).csvFormat(format).startRow(1);
            builder.build().extract(2).stream().forEach(row -> System.out.println(Jsons.toJson(row)));
        }
    }

    @Test
    public void test() throws IOException {
        String file;
        DataExtractorBuilder builder;
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"');

        /*file =  "D:\\temp\\withoutbom\\test-utf8.csv";
        builder = DataExtractorBuilder.newBuilder(file).csvFormat(format);
        builder.build().extract(100).stream().forEach(row -> System.out.println(Jsons.toJson(row)));*/

        /*file =  "D:\\temp\\withoutbom\\test-utf16le.csv";
        builder = DataExtractorBuilder.newBuilder(file).csvFormat(format).charset(StandardCharsets.UTF_16LE);
        builder.build().extract(100).stream().forEach(row -> System.out.println(Jsons.toJson(row)));*/

        file = "D:\\temp\\withoutbom\\test-utf16be.csv";
        builder = DataExtractorBuilder.newBuilder(file).csvFormat(format).charset(StandardCharsets.UTF_16BE);
        builder.build().extract(100).stream().forEach(row -> System.out.println(Jsons.toJson(row)));
    }

    @Test
    public void test8() throws IOException {
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\withoutbom\\test-utf8.csv"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\withoutbom\\test-utf16le.csv"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\withoutbom\\test-utf16be.csv"));
        //System.out.println(Files.toString(new File("D:\\temp\\withoutbom\\test-utf16be.csv"), "UTF-16BE"));
        System.out.println(CharacterEncodingDetector.detect("D:\\temp\\withoutbom\\gbk.txt"));
    }

}
