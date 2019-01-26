package test.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import code.ponfee.commons.extract.DataExtractor;
import code.ponfee.commons.extract.DataExtractorBuilder;

/**
 * 性能：Path > File > Input
 * @author Ponfee
 */
public class ExcelExtractorTest {

    @Test
    public void testXLS1() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("e:\\writeTest2.xls")
            .streaming(true)
            .headers(new String[] { "a", "b", "c", "d", "e", "f" }).build();
        et.extract((n, d) -> {
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void testXLS2() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("e:\\writeTest2.xls")
            .streaming(false)
            .headers(new String[] { "a", "b", "c", "d", "e", "f" }).build();
        et.extract((n, d) -> {
            System.out.println(Arrays.toString((String[])d));
        });
    }

    @Test
    public void testXLSX1() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("e:\\mergeTest.xlsx")
            .streaming(false)
            .headers(new String[] { "a", "b", "c", "d", "e", "f" }).build();
        et.extract((n, d) -> {
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void testXLSX2() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("e:\\mergeTest.xlsx")
            .streaming(true)
            .headers(new String[] { "a", "b", "c", "d", "e", "f" }).build();
        et.extract((n, d) -> {
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void testFile() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder(new File("D:\\test\\test_excel_14.xlsx"))
            /*.headers(new String[] { "a", "b", "c", "d", "e" })*/.build();
        et.extract((n, d) -> {
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void testInput() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder(new FileInputStream("D:\\test\\test_excel_14.xlsx"), "test_excel_16.xlsx", null)
            .headers(new String[] { "a", "b", "c", "d", "e" }).build();
        et.extract((n, d) -> {
            if (n == 0) {
                System.out.println(Arrays.toString((String[])d));
            }
            if (n == 1) {
                System.out.println(Arrays.toString((String[])d));
            }
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void test1() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("e:\\data_expert_temp.xls")
            .headers(new String[] { "a", "b", "c", "d", "e" }).build();
        et.extract((n, d) -> {
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    @Test
    public void test2() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("E:\\test.xlsx")
            .headers(new String[] { "a", "b", "c", "d", "e" }).build();
        et.extract((n, d) -> {
            System.out.println(String.join("|",(String[])d));
        });
    }
    
    @Test
    public void test3() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("E:\\advices_export.xls")
            .headers(new String[] { "a", "b", "c", "d"}).build();
        et.extract((n, d) -> {
            System.out.println(String.join("|",(String[])d));
        });
    }
    
    @Test
    public void testCsvPath() throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder("E:\\test.csv")
            .headers(new String[] { "a", "b", "c", "d", "e" }).build();
        et.extract((n, d) -> {
            if (n < 10)
            System.out.println(Arrays.toString((String[])d));
        });
    }
    
    
    // ------------------------------------------------------
    @Test
    public void test6() throws FileNotFoundException, IOException {
        //test("E:\\test20.xlsx", false); // 9.1 s
        //test("E:\\test100.xlsx", true); // 7.8
        //test("E:\\writeTest.xls", false); // 2.6
        test("E:\\writeTest.xls", true); // 2.0
    }

    private void test(String filename, boolean streaming) throws FileNotFoundException, IOException {
        DataExtractor<?> et = DataExtractorBuilder.newBuilder(filename)
            .streaming(streaming).headers(new String[] { "a", "b", "c", "d", "e" }).build();
        
        AtomicInteger count = new AtomicInteger();
        et.extract((n, d) -> {
            if (n < 10) {
                System.out.println(Arrays.toString((String[])d));
            }
            count.incrementAndGet();
        });
        System.out.println(count.get());
    }
}
