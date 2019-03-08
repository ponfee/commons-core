package test.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.concurrent.ThreadPoolExecutors;
import code.ponfee.commons.export.AbstractDataExporter;
import code.ponfee.commons.export.CsvFileExporter;
import code.ponfee.commons.export.ExcelExporter;
import code.ponfee.commons.export.SplitCsvFileExporter;
import code.ponfee.commons.export.SplitExcelExporter;
import code.ponfee.commons.export.Table;

public class ExportTester2 {
    static final ExecutorService EXECUTOR = ThreadPoolExecutors.create(
       4, 16, 120, 0, "test"
    );

    @Test
    public void testExcel1() throws IOException {
        AbstractDataExporter excel = new ExcelExporter();

        Table table = new Table("a,b,c,d,e".split(","));
        table.setCaption("title");
        int n = 10;
        AtomicInteger count = new AtomicInteger(0);
        Stopwatch watch = Stopwatch.createStarted();
        for (int j = 0; j < n; j++) {
            EXECUTOR.submit(()-> {
                List<Object[]> data = new ArrayList<>();
                for (int i = 0; i < 100000; i++) {
                    data.add(new Object[] { "1", "2", "3", "4", "5" });
                }
                table.addRows(data);
                if (count.incrementAndGet() == n) {
                    table.end();
                }
            });
        }
        System.out.println("***************"+watch.stop());
        watch.reset().start();
        excel.setName("21321");
        excel.build(table);
        IOUtils.write((byte[]) excel.export(), new FileOutputStream("d:/test11.xlsx"));
        excel.close();
        System.out.println(watch.stop());
    }

    
    @Test
    public void testExcel2() throws IOException {
        AbstractDataExporter excel = new ExcelExporter();

        Table table = new Table("a,b,c,d,e".split(","));
        table.setCaption("title");
        int n = 10;
        AtomicInteger count = new AtomicInteger(0);
        Stopwatch watch = Stopwatch.createStarted();
        for (int j = 0; j < n; j++) {
            EXECUTOR.submit(()-> {
                for (int i = 0; i < 100000; i++) {
                    table.addRow(new Object[] { "1", "2", "3", "4", "5" });
                }
                if (count.incrementAndGet() == n) {
                    table.end();
                }
            });
        }
        System.out.println("================"+watch.stop());
        watch.reset().start();
        excel.setName("21321");
        excel.build(table);
        IOUtils.write((byte[]) excel.export(), new FileOutputStream("d:/test22.xlsx"));
        excel.close();
        System.out.println(watch.stop());
    }

    @Test
    public void testCsv1() throws IOException {
        CsvFileExporter excel = new CsvFileExporter("E:/test.csv", true);

        Table table = new Table("中,文,b,o,m".split(","));
        table.setCaption("title");
        int n = 100;
        AtomicInteger count = new AtomicInteger(0);
        Stopwatch watch = Stopwatch.createStarted();
        for (int j = 0; j < n; j++) {
            EXECUTOR.submit(()-> {
                for (int i = 0; i < 100000; i++) {
                    table.addRow(new Object[] { "1", "2", "3", "4", "5" });
                }
                if (count.incrementAndGet() == n) {
                    table.end();
                }
            });
        }
        System.out.println("================"+watch.stop());
        watch.reset().start();
        excel.setName("21321");
        excel.build(table);
        excel.close();
        System.out.println(watch.stop());
    }
    
    @Test
    public void testSplitExcel() throws IOException {
        Table table = new Table("a,b,c,d,e".split(","));
        table.setCaption("title");
        int n = 100;
        AtomicInteger count = new AtomicInteger(0);
        Stopwatch watch = Stopwatch.createStarted();
        for (int j = 0; j < n; j++) {
            EXECUTOR.submit(()-> {
                for (int i = 0; i < 100000; i++) {
                    table.addRow(new Object[] { "1111111111111111111111111111", "2", "3", "4", "5" });
                }
                if (count.incrementAndGet() == n) {
                    table.end();
                }
            });
        }
        
        SplitExcelExporter excel = new SplitExcelExporter(65537,"d:/test/test_excel_", EXECUTOR);
        excel.setName("21321");
        System.out.println("================"+watch.stop());
        watch.reset().start();
        excel.build(table);
        excel.close();
        System.out.println(watch.stop());
    }
    
    @Test
    public void testSplitCsv() throws IOException {
        Table table = new Table("中国,人,c,d,e".split(","));
        table.setCaption("title");
        int n = 10;
        AtomicInteger count = new AtomicInteger(0);
        Stopwatch watch = Stopwatch.createStarted();
        for (int j = 0; j < n; j++) {
            EXECUTOR.submit(()-> {
                for (int i = 0; i < 100000; i++) {
                    table.addRow(new Object[] { "1", "2", "3", "4", "5" });
                }
                if (count.incrementAndGet() == n) {
                    table.end();
                }
            });
        }
        
        SplitCsvFileExporter csv = new SplitCsvFileExporter(65537,"d:/test/test_csv_", true, EXECUTOR);
        System.out.println("================"+watch.stop());
        watch.reset().start();
        csv.build(table);
        csv.close();
        System.out.println(watch.stop());
    }
}

