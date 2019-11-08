package test.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import code.ponfee.commons.export.AbstractDataExporter;
import code.ponfee.commons.export.CellStyleOptions;
import code.ponfee.commons.export.CsvStringExporter;
import code.ponfee.commons.export.ExcelExporter;
import code.ponfee.commons.export.HtmlExporter;
import code.ponfee.commons.export.Table;
import code.ponfee.commons.export.Thead;
import code.ponfee.commons.io.Files;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.model.ResultCode;
import code.ponfee.commons.tree.BaseNode;
import code.ponfee.commons.util.Captchas;

public class ExportTester {

    private int multiple = 20;

    public @Test void testHtml1() throws IOException {
        AbstractDataExporter html = new HtmlExporter();
        AbstractDataExporter csv = new CsvStringExporter();
        List<BaseNode<Integer, Thead>> list = new ArrayList<>();

        list.add(new BaseNode<>(1, 0, new Thead("区域")));
        list.add(new BaseNode<>(2, 0, new Thead("分公司")));
        
        list.add(new BaseNode<>(3, 0, new Thead("昨天")));
        list.add(new BaseNode<>(4, 3, new Thead("项目数")));
        list.add(new BaseNode<>(5, 3, new Thead("项目应收(元)")));
        list.add(new BaseNode<>(6, 3, new Thead("成交套数")));
        list.add(new BaseNode<>(7, 3, new Thead("套均收入(元)")));
        list.add(new BaseNode<>(8, 3, new Thead("团购项目数")));
        list.add(new BaseNode<>(9, 3, new Thead("导客项目数")));
        list.add(new BaseNode<>(10, 3, new Thead("代收项目数")));
        list.add(new BaseNode<>(11, 3, new Thead("线上项目数")));
        list.add(new BaseNode<>(12, 0, new Thead("本月")));
        list.add(new BaseNode<>(13, 12,new Thead("应收(万)")));
        list.add(new BaseNode<>(14, 12,new Thead("实收(万)")));
        list.add(new BaseNode<>(15, 12,new Thead("成交套数")));
        list.add(new BaseNode<>(16, 12,new Thead("套均收入(元)")));
        list.add(new BaseNode<>(17, 12,new Thead("团购项目应收(万)")));
        list.add(new BaseNode<>(18, 12,new Thead("团购项目成交套数")));
        list.add(new BaseNode<>(19, 12,new Thead("团购项目经服成交套数")));
        list.add(new BaseNode<>(20, 12,new Thead("团购项目套均收入(元)")));
        list.add(new BaseNode<>(21, 12,new Thead("团购项目经服成交应收(万)")));
        list.add(new BaseNode<>(22, 12,new Thead("团购项目中介应付外佣(万)")));
        list.add(new BaseNode<>(23, 12,new Thead("团购项目经服成交套数占比")));
        list.add(new BaseNode<>(24, 12,new Thead("团购项目中介分佣比例")));
        list.add(new BaseNode<>(25, 12,new Thead("导客项目应收(万)")));
        list.add(new BaseNode<>(26, 12,new Thead("导客项目成交套数")));
        list.add(new BaseNode<>(27, 12,new Thead("导客项目套均收入(元)")));
        list.add(new BaseNode<>(28, 12,new Thead("导客项目中介应付外佣(万)")));
        list.add(new BaseNode<>(29, 12,new Thead("导客项目中介分佣比例")));
        list.add(new BaseNode<>(30, 12,new Thead("代收项目应收(万)")));
        list.add(new BaseNode<>(31, 12,new Thead("代收项目成交套数")));
        list.add(new BaseNode<>(32, 12,new Thead("线上项目应收(万)")));
        list.add(new BaseNode<>(33, 12,new Thead("线上项目成交套数")));
        list.add(new BaseNode<>(34, 12,new Thead("月指标(万)")));
        list.add(new BaseNode<>(35, 12,new Thead("指标完成率")));

        Table table = new Table(list);
        System.out.println(Jsons.toJson(table.getThead()));
        table.setCaption("abc");
        table.addRowsAndEnd(Lists.newArrayList(new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                                          new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        ));
        table.setTfoot(new Object[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1});
        table.setComment("comment1;comment2;comment3;comment4;comment5;comment6;");
        html.build(table);
        csv.build(table);

        table = new Table(list);
        table.setCaption("123");
        table.toEnd();
        html.build(table);
        
        table = new Table(list);
        table.toEnd();
        table.setCaption("bnm");
        html.build(table);

        IOUtils.write((String) html.setName("报表").export(), new FileOutputStream("d://testHtml1.html"), "UTF-8");
        Files.addBOM("d:/testHtml1.html");
        IOUtils.write((String) csv.export(), new FileOutputStream("d://testHtml1.csv"), "UTF-8");
        Files.addBOM("d:/testHtml1.csv");

        html.close();
        csv.close();
    }

    @Test
    public void testHtml2() throws FileNotFoundException, IOException {
        AbstractDataExporter html = new HtmlExporter();
        html.build(new Table("a,b,c,d,e".split(",")).toEnd());
        IOUtils.write((String) html.export(), new FileOutputStream("d://testHtml2.html"), "UTF-8");
        Files.addBOM("d:/testHtml2.html");
        html.close();
    }

    @Test
    public void testExcel() throws IOException {
        ExcelExporter excel = new ExcelExporter();
        List<BaseNode<Integer, Thead>> list = new ArrayList<>();

        list.add(new BaseNode<>(1, 0, new Thead("区域")));
        list.add(new BaseNode<>(2, 0, new Thead("分公司")));
        
        list.add(new BaseNode<>(3, 0, new Thead("昨天")));
        list.add(new BaseNode<>(4, 3, new Thead("项目数")));
        list.add(new BaseNode<>(5, 3, new Thead("项目应收(元)")));
        list.add(new BaseNode<>(6, 3, new Thead("成交套数")));
        list.add(new BaseNode<>(7, 3, new Thead("套均收入(元)")));
        list.add(new BaseNode<>(8, 3, new Thead("团购项目数")));
        list.add(new BaseNode<>(9, 3, new Thead("导客项目数")));
        list.add(new BaseNode<>(10, 3, new Thead("代收项目数")));
        list.add(new BaseNode<>(11, 3, new Thead("线上项目数")));
        list.add(new BaseNode<>(12, 0, new Thead("本月")));
        list.add(new BaseNode<>(13, 12, new Thead("应收(万)")));
        list.add(new BaseNode<>(14, 12, new Thead("实收(万)")));
        list.add(new BaseNode<>(15, 12, new Thead("成交套数")));
        list.add(new BaseNode<>(16, 12, new Thead("套均收入(元)")));
        list.add(new BaseNode<>(17, 12, new Thead("团购项目应收(万)")));
        list.add(new BaseNode<>(18, 12, new Thead("团购项目成交套数")));
        list.add(new BaseNode<>(19, 12, new Thead("团购项目经服成交套数")));
        list.add(new BaseNode<>(20, 12, new Thead("团购项目套均收入(元)")));
        list.add(new BaseNode<>(21, 12, new Thead("团购项目经服成交应收(万)")));
        list.add(new BaseNode<>(22, 12, new Thead("团购项目中介应付外佣(万)")));
        list.add(new BaseNode<>(23, 12, new Thead("团购项目经服成交套数占比")));
        list.add(new BaseNode<>(24, 12, new Thead("团购项目中介分佣比例")));
        list.add(new BaseNode<>(25, 12, new Thead("导客项目应收(万)")));
        list.add(new BaseNode<>(26, 12, new Thead("导客项目成交套数")));
        list.add(new BaseNode<>(27, 12, new Thead("导客项目套均收入(元)")));
        list.add(new BaseNode<>(28, 12, new Thead("导客项目中介应付外佣(万)")));
        list.add(new BaseNode<>(29, 12, new Thead("导客项目中介分佣比例")));
        list.add(new BaseNode<>(30, 12, new Thead("代收项目应收(万)")));
        list.add(new BaseNode<>(31, 12, new Thead("代收项目成交套数")));
        list.add(new BaseNode<>(32, 12, new Thead("线上项目应收(万)")));
        list.add(new BaseNode<>(33, 12, new Thead("线上项目成交套数")));
        list.add(new BaseNode<>(34, 12, new Thead("月指标(万)")));
        list.add(new BaseNode<>(35, 12, new Thead("指标完成率")));
        
        List<Object[]> data1 = new ArrayList<>();
        for (int i = 0; i < 2*multiple; i++) {
            data1.add(new Object[] { "1234563.918%", "2017-02-03", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd",
                "abd", "abd",
                "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "1" });
        }
        
        List<Object[]> data2 = new ArrayList<>();
        for (int i = 0; i < 5*multiple; i++) {
            data2.add(new Object[] { "1234563.918%", "2017-02-03", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd",
                "abd", "abd",
                "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "1" });
        }
        
        List<Object[]> data3 = new ArrayList<>();
        for (int i = 0; i < 3*multiple; i++) {
            data3.add(new Object[] { "1234563.918%", "2017-02-03", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd",
                "abd", "abd",
                "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "1" });
        }
        Object[] tfoot = new Object[] {"1", "2", "3", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd","abd", "abd",
            "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "abd", "1" };
        Map<CellStyleOptions, Object> options = ImmutableMap.of(CellStyleOptions.HIGHLIGHT, ImmutableMap.of("cells", Lists.newArrayList(Lists.newArrayList(1,1),Lists.newArrayList(2,2)), "color", "#FF3030"));
        long start = System.currentTimeMillis();
        System.out.println("========================================start");

        Table<Object[]> table1 = new Table<>(list);
        table1.setCaption("test1");
        table1.addRowsAndEnd(data1);
        table1.setTfoot(tfoot);
        table1.setOptions(options);
        excel.setName("报表1").build(table1);

        // ------------------------------------------
        Table table2 = new Table(list);
        table2.setCaption("test2");
        table2.addRowsAndEnd(data2);
        table2.setTfoot(tfoot);
        table2.setOptions(options);
        excel.setName("报表2").build(table2);

        // ------------------------------------------
        Table table3 = new Table(list);
        table3.setCaption("test3");
        table3.addRowsAndEnd(data3);
        table3.setTfoot(tfoot);
        table3.setOptions(options);
        excel.setName("报表1").build(table3);

        // ------------------------------------------
        excel.setName("图表");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Captchas.generate(200, baos, RandomStringUtils.randomAlphanumeric(10));
        excel.insertImage(baos.toByteArray());
        baos = new ByteArrayOutputStream();
        Captchas.generate(200, baos, RandomStringUtils.randomAlphanumeric(10));
        excel.insertImage(baos.toByteArray());

        OutputStream out = new FileOutputStream("d:/abc1.xlsx");
        excel.write(out);
        out.close();
        excel.close();
        System.out.println("========================================excel: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        // -------------------------csv
        CsvStringExporter csv = new CsvStringExporter();
        table1 = new Table<>(list);
        table1.setCaption("test1");
        table1.addRowsAndEnd(data1);
        table1.setTfoot(tfoot);
        table1.setOptions(options);
        csv.build(table1);
        IOUtils.write(csv.export().toString(), new FileOutputStream("d://testExcel.csv"), "UTF-8");
        Files.addBOM(new File("d://testExcel.csv"));
        csv.close();
        System.out.println("========================================csv: " + (System.currentTimeMillis() - start));
        
        start = System.currentTimeMillis();
        HtmlExporter html = new HtmlExporter();
        table1 = new Table(list);
        table1.setCaption("test1");
        table1.addRowsAndEnd(data1);
        table1.setTfoot(tfoot);
        table1.setOptions(options);
        html.build(table1);
        html.setName("test");
        IOUtils.write((String) html.export(), new FileOutputStream("d://testExcel.html"), "UTF-8");
        Files.addBOM("d:/testExcel.html");
        html.close();
        System.out.println("========================================html: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void testExcel2() throws IOException {
        AbstractDataExporter excel = new ExcelExporter();

        Table table = new Table("a,b,c,d,e".split(","));
        table.setCaption("title");
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[] { "11111111111111111111111111111111111111111", "2", "3", "4", "5" });
        table.addRowsAndEnd(data);
        excel.setName("21321");
        excel.build(table);
        IOUtils.write((byte[]) excel.export(), new FileOutputStream("d:/testExcel2.xlsx"));
        excel.close();
    }
    
    @Test
    public void testExcel5() throws IOException {
        AbstractDataExporter<byte[]> excel = new ExcelExporter();

        Table<Result<Void>> table = new Table<>(
            "a,b".split(","), 
            o -> new Object[] { o.getCode(), o.getMsg() }
        );
        table.setCaption("title");
        table.addRow(Result.SUCCESS);
        table.addRow(Result.failure(ResultCode.BAD_REQUEST));
        table.toEnd();

        excel.setName("21321");
        excel.build(table);
        IOUtils.write((byte[]) excel.export(), new FileOutputStream("d:/11111xxxx.xlsx"));
        excel.close();
    }
    
    
    @Test
    public void testExcel3() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        FileOutputStream out = new FileOutputStream("d:/test_empty.xlsx");
        wb.createSheet();
        wb.write(out);
        wb.close();
        out.close();
    }

}
