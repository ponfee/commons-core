package code.ponfee.commons.extract.streaming;

import code.ponfee.commons.extract.ExcelExtractor;
import code.ponfee.commons.extract.ExtractableDataSource;
import code.ponfee.commons.extract.streaming.xls.HSSFStreamingReader;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 * Excel file data extractor based streaming
 * 
 * 在打开一本工作簿时，不管是一个.xls HSSFWorkbook，还是一个.xlsx XSSFWorkbook，
 * 工作簿都可以从文件或InputStream中加载。使用File对象可以降低内存消耗，
 * 而InputStream则需要更多的内存，因为它必须缓冲整个文件。
 * 
 *  // Use a file
 *  Workbook wb = WorkbookFactory.create(new File("MyExcel.xls"));
 *  
 *  // Use an InputStream, needs more memory
 *  Workbook wb = WorkbookFactory.create(new FileInputStream("MyExcel.xlsx"));
 *  
 *  
 *  =======================================================================
 *  // HSSFWorkbook, File
 *  NPOIFSFileSystem fs = new NPOIFSFileSystem(new File("file.xls"));
 *  HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
 *  ....
 *  fs.close();
 *  
 *  // HSSFWorkbook, InputStream, needs more memory
 *  NPOIFSFileSystem fs = new NPOIFSFileSystem(myInputStream);
 *  HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
 *  
 *  
 *  =======================================================================
 *  // XSSFWorkbook, File
 *  OPCPackage pkg = OPCPackage.open(new File("file.xlsx"));
 *  XSSFWorkbook wb = new XSSFWorkbook(pkg);
 *  ....
 *  pkg.close();
 *  
 *  // XSSFWorkbook, InputStream, needs more memory
 *  OPCPackage pkg = OPCPackage.open(myInputStream);
 *  XSSFWorkbook wb = new XSSFWorkbook(pkg);
 *  ....
 *  pkg.close();
 * 
 * 
 * https://blog.csdn.net/zl_momomo/article/details/80703533
 * http://poi.apache.org/components/spreadsheet/how-to.html
 * https://github.com/monitorjbl/excel-streaming-reader
 * 
 * 
 * https://github.com/alibaba/easyexcel
 * https://www.jianshu.com/p/cb9cd9965a63
 * 
 * @author Ponfee
 */
public class StreamingExcelExtractor extends ExcelExtractor {

    private final Executor executor;

    public StreamingExcelExtractor(ExtractableDataSource dataSource, String[] headers, 
                                   int startRow, ExcelType type) {
        this(dataSource, headers, startRow, type, 0, null);
    }

    public StreamingExcelExtractor(ExtractableDataSource dataSource, String[] headers,
                                   int startRow, ExcelType type, int sheetIndex) {
        this(dataSource, headers, startRow, type, sheetIndex, null);
    }

    public StreamingExcelExtractor(ExtractableDataSource dataSource, String[] headers, 
                                   int startRow, ExcelType type, int sheetIndex, Executor executor) {
        super(dataSource, headers, startRow, type, sheetIndex);
        this.executor = executor;
    }

    @Override
    protected Workbook createWorkbook(ExtractableDataSource dataSource) {
        Object ds = dataSource.getDataSource();
        switch (type) {
            case XLS:
                HSSFStreamingReader reader = HSSFStreamingReader.create(200, sheetIndex);
                return ds instanceof File 
                     ? reader.open((File) ds, executor) 
                     : reader.open((InputStream) ds, executor);
            case XLSX:
                // only support xlsx
                StreamingReader.Builder builder = StreamingReader.builder()
                    .rowCacheSize(50) // 缓存到内存中的行数，默认是10
                    .bufferSize(4096); // 读取资源时，缓存到内存的字节大小，默认是1024
                return ds instanceof File
                     ? builder.open((File) ds) 
                     : builder.open((InputStream) ds);
            default:
                throw new RuntimeException("Unknown excel type: " + type);
        }
    }

}
