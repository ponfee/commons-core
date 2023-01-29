package cn.ponfee.commons.io;

import cn.ponfee.commons.extract.DataExtractorBuilder;
import cn.ponfee.commons.io.charset.BytesDetector;
import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.spring.SpringContextHolder;
import cn.ponfee.commons.tree.TreeNode;
import cn.ponfee.commons.util.MavenProjects;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilesTest {

    @Test
    public void testx() {
        System.out.println(ClassLayout.parseInstance(new Object()).toPrintable());
        System.out.println("\n-----------------------");
        System.out.println(ClassLayout.parseInstance(new Object[10]).toPrintable());
        System.out.println("\n-----------------------");
        System.out.println(ClassLayout.parseInstance(new long[10]).toPrintable());
        System.out.println("\n-----------------------");

        System.out.println(CharsetDetector.detect(MavenProjects.getMainJavaFile(TreeNode.class)));
    }

    @Test
    public void test1() throws MalformedURLException {
        System.out.println("GBK.properties -> "+ CharsetDetector.detect("D:/temp/GBK.properties"));
        System.out.println("UTF8.txt -> "+ CharsetDetector.detect("D:/temp/UTF8.txt"));
        System.out.println("UTF8-WITH-BOM.xml -> "+ CharsetDetector.detect("D:/temp/UTF8-WITH-BOM.xml"));
        System.out.println("UTF8-WITHOUT-BOM.xml -> "+ CharsetDetector.detect("D:/temp/UTF8-WITHOUT-BOM.xml"));
        System.out.println("UTF16-BIG-ENDIAN-WITH-BOM.xml -> "+ CharsetDetector.detect("D:/temp/UTF16-BIG-ENDIAN-WITH-BOM.xml"));
        System.out.println("UTF16-BIG-ENDIAN-WITHOUT-BOM.xml -> "+ CharsetDetector.detect("D:/temp/UTF16-BIG-ENDIAN-WITHOUT-BOM.xml"));
        System.out.println("UTF16-WITH-BOM.xml -> "+ CharsetDetector.detect("D:/temp/UTF16-WITH-BOM.xml"));
        System.out.println("UTF16-WITHOUT-BOM.xml -> "+ CharsetDetector.detect("D:/temp/UTF16-WITHOUT-BOM.xml"));
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
            Charset charset = CharsetDetector.detect(file);
            System.out.println("\n============================="+file+" -> "+", "+charset+"   "+Files.toString(new File(file)).replaceAll("\r\n|\n", ";"));
            DataExtractorBuilder builder = DataExtractorBuilder.newBuilder(file);
            builder.build().extract(100).stream().forEach(row -> System.out.println(Jsons.toJson(row)));
        }
    }
    
    @Test
    public void test5() throws IOException {
        System.out.println(CharsetDetector.detect("D:\\temp\\c24aafd4f3f24c2a86734b20f9a0edd3.Adobe_Fireworks_CS6_XiaZaiBa.exe"));
        System.out.println(CharsetDetector.detect("D:\\temp\\csv-gbk.csv"));
        System.out.println(CharsetDetector.detect("D:\\temp\\csv-gbk-bom.csv"));
        System.out.println(CharsetDetector.detect("D:\\temp\\csv-utf8.csv"));
        System.out.println(CharsetDetector.detect("D:\\temp\\csv-utf8-bom.csv"));
        System.out.println(CharsetDetector.detect("D:\\temp\\csv-utf16.csv"));
        System.out.println(CharsetDetector.detect("D:\\temp\\csv-utf16-bom.csv"));
        System.out.println(CharsetDetector.detect("D:\\temp\\2.png"));
        System.out.println(CharsetDetector.detect("D:\\temp\\IMG_2485.JPG"));
        System.out.println(CharsetDetector.detect("D:\\temp\\ca.pfx"));
        System.out.println(CharsetDetector.detect("D:\\temp\\signers.xml"));
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
            Charset charset = CharsetDetector.detect(file);
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
        System.out.println(CharsetDetector.detect("D:\\temp\\withoutbom\\test-utf8.csv"));
        System.out.println(CharsetDetector.detect("D:\\temp\\withoutbom\\test-utf16le.csv"));
        System.out.println(CharsetDetector.detect("D:\\temp\\withoutbom\\test-utf16be.csv"));
        //System.out.println(Files.toString(new File("D:\\temp\\withoutbom\\test-utf16be.csv"), "UTF-16BE"));
        System.out.println(CharsetDetector.detect("D:\\temp\\withoutbom\\gbk.txt"));
    }

    @Test
    public void testDetect() throws IOException {
        File filePath = MavenProjects.getMainJavaFile(SpringContextHolder.class);
        System.out.println("CharsetDetector.detect -->  " + CharsetDetector.detect(new FileInputStream(filePath)));
        System.out.println("EncodingDetector.detect -->  " + BytesDetector.detect(Files.readByteArray(new FileInputStream(filePath), 12000)));
    }

    @Test
    public void testDetectFile() {
        //detectFile(MavenProjects.getMainJavaPath("cn.ponfee.commons"));
        detectFile(MavenProjects.getTestJavaPath("cn.ponfee.commons.io.file"));
    }

    private static void detectFile(String filePath) {
        Files.listFiles(filePath).traverse(tree -> {
            if (CollectionUtils.isEmpty(tree.getChildren())) {
                try {
                    File f = tree.getAttach();
                    System.out.println(f.getName() + ": CharsetDetector=" + CharsetDetector.detect(new FileInputStream(f)) + ", EncodingDetector=" + BytesDetector.detect(Files.readByteArray(new FileInputStream(f), 1200)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    public void testFormat() {
        String text = "JPFreq[3][74] = 600;\n" +
            "            JPFreq[3][45] = 599;\n" +
            "            JPFreq[3][3] = 598;\n" +
            "            JPFreq[3][24] = 597;\n" +
            "            JPFreq[3][30] = 596;\n" +
            "            JPFreq[4][76] = 485;\n" +
            "            JPFreq[22][65] = 3;\n" +
            "            JPFreq[42][29] = 2;\n" +
            "            JPFreq[27][66] = 1;\n" +
            "            JPFreq[26][89] = 0;";

        List<String> collect = Arrays.stream(text.split(";"))
            .map(String::trim)
            .collect(Collectors.toList());

        int maxLeft = collect.stream().mapToInt(s -> s.split("=")[0].trim().length()).max().orElse(0);
        int maxRight = collect.stream().mapToInt(s -> s.split("=")[1].trim().length()).max().orElse(0);

        for (List<String> line : Lists.partition(collect, 5)) {
            String s = line.stream().map(e -> {
                String[] array = e.split("=");
                return StringUtils.rightPad(array[0].trim(), maxLeft, " ") + " = " + StringUtils.leftPad(array[1].trim(), maxRight, " ")+"; ";
            }).collect(Collectors.joining());
            System.out.println(s);
        }
    }

    public static void main(String[] args) {
        int[][] GBFreq = new int[94][94];
        Arrays.stream(GBFreq).forEach(e -> System.out.println(Arrays.toString(e)));
    }
}
