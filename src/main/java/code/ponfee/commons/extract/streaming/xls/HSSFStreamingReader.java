package code.ponfee.commons.extract.streaming.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Preconditions;

/**
 * The version for 2003 or early XSL excel file 
 * streaming reader
 * 
 * excel reader
 * 
 * @author Ponfee
 */
public class HSSFStreamingReader {

    private int rowCacheSize = 0; // if less 0 then 0(SynchronousQueue)
    private int[] sheetIndexs;
    private String[] sheetNames;

    private HSSFStreamingReader() {}

    /**
     * Reads all sheet
     * 
     * @return HSSFStreamingReader instance
     */
    public static HSSFStreamingReader create() {
        return new HSSFStreamingReader();
    }

    /**
     * Reads spec sheet that is in sheet index at int array
     * 
     * @param rowCacheSize  the size of read row count in memory
     * @param sheetIndexs sheet index at int array
     * @return HSSFStreamingReader instance
     */
    public static HSSFStreamingReader create(int rowCacheSize, int... sheetIndexs) {
        Preconditions.checkArgument(rowCacheSize > 0);
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(sheetIndexs));
        HSSFStreamingReader reader = new HSSFStreamingReader();
        reader.rowCacheSize = rowCacheSize;
        reader.sheetIndexs = sheetIndexs;
        return reader;
    }

    /**
     * Reads spec sheet that is in sheet name at string array
     * 
     * @param rowCacheSize  the size of read row count in memory
     * @return HSSFStreamingReader instance
     */
    public static HSSFStreamingReader create(int rowCacheSize, String... sheetNames) {
        Preconditions.checkArgument(rowCacheSize > 0);
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(sheetNames));
        HSSFStreamingReader reader = new HSSFStreamingReader();
        reader.rowCacheSize = rowCacheSize;
        reader.sheetNames = sheetNames;
        return reader;
    }

    public HSSFStreamingWorkbook open(InputStream input, ExecutorService executor) {
        return new HSSFStreamingWorkbook(input, rowCacheSize, 
                                         sheetIndexs, sheetNames, executor);
    }

    public HSSFStreamingWorkbook open(File file, ExecutorService executor) {
        try {
            return open(new FileInputStream(file), executor);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public HSSFStreamingWorkbook open(String filePath, ExecutorService executor) {
        return open(new File(filePath), executor);
    }

}
