package test.extract;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;

import code.ponfee.commons.concurrent.ThreadPoolExecutors;
import code.ponfee.commons.extract.streaming.xls.HSSFStreamingReader;
import code.ponfee.commons.extract.streaming.xls.HSSFStreamingRow;
import code.ponfee.commons.extract.streaming.xls.HSSFStreamingSheet;
import code.ponfee.commons.extract.streaming.xls.HSSFStreamingWorkbook;

public class TestHSSFStreaming {

    static ThreadPoolExecutor exec = ThreadPoolExecutors.create(1, 8, 60);

    @Test
    public void test1() throws InterruptedException {
        //String file = "e:/data_expert.xls";
        HSSFStreamingWorkbook wb = HSSFStreamingReader.create(40, 0).open("src/test/java/test/extract/writeTest2.xls", exec);
        HSSFStreamingSheet sheet = (HSSFStreamingSheet) wb.getSheetAt(0);
        for (Row row : sheet) {
            System.out.print(row.getRowNum() + ", " + ((HSSFStreamingRow) row).getRowOrder() + ":  ");
            for (Cell cell : row) {
                System.out.print(cell == null ? "null, " : cell.getStringCellValue() + ", ");
            }
            System.out.println();
        }
        System.out.println();
        for (Iterator<Sheet> iter = wb.iterator(); iter.hasNext();) {
            HSSFStreamingSheet sst = (HSSFStreamingSheet) iter.next();
            System.out.println("SheetIndex: "+sst.getSheetIndex()+"，SheetName: "+sst.getSheetName()+"，cheRowCount: "+sheet.getCacheRowCount());
        }
    }

    public static void add(List<String> list, int index, String s) {
        int size;
        if (index == (size = list.size())) {
            list.add(s);
        } else if (index < size) {
            list.set(index, s);
        } else {
            for (int i = size; i < index; i++) {
                list.add(null);
            }
            list.add(s  );
        }
    }
    
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        add(list, 0, "a");
        System.out.println(list);
        
        add(list, 5, "b");
        System.out.println(list);
        
        add(list, 4, "c");
        System.out.println(list);
        
        add(list, 4, "c");
        System.out.println(list);
    }
}
