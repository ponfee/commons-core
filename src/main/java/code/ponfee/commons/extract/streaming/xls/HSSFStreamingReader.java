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

    private int rowCacheableSize = 0; // if less or equals 0 then infinity
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
     * @param rowCacheableSize  the size of read row count in memory
     * @param sheetIndexs sheet index at int array
     * @return HSSFStreamingReader instance
     */
    public static HSSFStreamingReader create(int rowCacheableSize, int... sheetIndexs) {
        Preconditions.checkArgument(rowCacheableSize > 0);
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(sheetIndexs));
        HSSFStreamingReader reader = new HSSFStreamingReader();
        reader.rowCacheableSize = rowCacheableSize;
        reader.sheetIndexs = sheetIndexs;
        return reader;
    }

    /**
     * Reads spec sheet that is in sheet name at string array
     * 
     * @param rowCacheableSize  the size of read row count in memory
     * @return HSSFStreamingReader instance
     */
    public static HSSFStreamingReader create(int rowCacheableSize, String... sheetNames) {
        Preconditions.checkArgument(rowCacheableSize > 0);
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(sheetNames));
        HSSFStreamingReader reader = new HSSFStreamingReader();
        reader.rowCacheableSize = rowCacheableSize;
        reader.sheetNames = sheetNames;
        return reader;
    }

    public HSSFStreamingWorkbook open(InputStream input, ExecutorService executor) {
        return new HSSFStreamingWorkbook(input, rowCacheableSize, 
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
