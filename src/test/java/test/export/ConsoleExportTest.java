package test.export;

import cn.ponfee.commons.export.AbstractDataExporter;
import cn.ponfee.commons.export.ConsoleExporter;
import cn.ponfee.commons.export.Table;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConsoleExportTest {
    private int multiple = 20;

    @Test
    public void test() {
        //AbstractDataExporter<String> export = new ConsoleExporter(System.out, 30, true);
        AbstractDataExporter<String> export = new ConsoleExporter(System.out, 36, false);
        Table table1 = new Table(new String[]{"name1", "age", "gender", "birthday", "x"});

        List<Object[]> data1 = Arrays.asList(
                new Object[]{"alalicealicealicealicealicealicealicealicealicealicealicealicealicealicealicealicealicealicealicealicealicealicealiceice", 20, "male", "2000-05-30", 0},
                new Object[]{"bob", 20, "female", "2001-05-30", 1},
                new Object[]{UUID.randomUUID().toString(), 25, "male", "2000-04-30", 2}
        );

        table1.setCaption("test");
        table1.addRowsAndEnd(data1);
        export.setName("报表1").build(table1);
    }
}
