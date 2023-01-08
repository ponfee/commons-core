package test.extract;

import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;

import cn.ponfee.commons.concurrent.ThreadPoolExecutors;
import cn.ponfee.commons.extract.streaming.xls.HSSFStreamingReader;
import cn.ponfee.commons.extract.streaming.xls.HSSFStreamingRow;
import cn.ponfee.commons.extract.streaming.xls.HSSFStreamingSheet;
import cn.ponfee.commons.extract.streaming.xls.HSSFStreamingWorkbook;

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

}
