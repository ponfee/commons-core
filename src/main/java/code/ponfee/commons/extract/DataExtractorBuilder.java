package code.ponfee.commons.extract;

import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;

import com.google.common.collect.ImmutableList;

import code.ponfee.commons.extract.ExcelExtractor.ExcelType;
import code.ponfee.commons.extract.streaming.StreamingExcelExtractor;
import code.ponfee.commons.http.ContentType;

/**
 * The data extractor builder, facade operator
 * 
 * @author Ponfee
 */
public class DataExtractorBuilder {

    private static final List<String> EXCEL_EXTENSION = ImmutableList.of("xlsx", "xls");
    private static final List<String> CSV_EXTENSION = ImmutableList.of("csv", "txt");

    private final Object dataSource; // only support such type as File, InputStream
    private final String fileName;
    private final String contentType;
    private String[] headers;
    private int startRow = 0;

    private int sheetIndex = 0; // excel work book sheet index: start with 0
    private boolean streaming = true; // excel whether streaming read, default true

    private CSVFormat csvFormat; // csv format

    private DataExtractorBuilder(Object dataSource, String fileName, 
                                 String contentType) {
        this.dataSource = dataSource;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public static DataExtractorBuilder newBuilder(InputStream dataSource, 
                                                  String fileName, 
                                                  String contentType) {
        return new DataExtractorBuilder(dataSource, fileName, contentType);
    }

    public static DataExtractorBuilder newBuilder(String path) {
        return newBuilder(new File(path));
    }

    public static DataExtractorBuilder newBuilder(File dataSource) {
        String fileName = dataSource.getName();
        return new DataExtractorBuilder(dataSource, fileName,
                                        getExtension(fileName));
    }

    public DataExtractorBuilder headers(String[] headers) {
        this.headers = headers;
        return this;
    }

    public DataExtractorBuilder startRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    public DataExtractorBuilder sheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
        return this;
    }

    public DataExtractorBuilder streaming(boolean streaming) {
        this.streaming = streaming;
        return this;
    }

    public DataExtractorBuilder csvFormat(CSVFormat csvFormat) {
        this.csvFormat = csvFormat;
        return this;
    }

    public <T> DataExtractor<T> build() {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        if (ContentType.TEXT_PLAIN.value().equalsIgnoreCase(contentType)
            || CSV_EXTENSION.contains(extension)) {
            // csv, txt文本格式数据
            return new CsvExtractor<>(dataSource, headers, csvFormat, startRow);
        } else if (EXCEL_EXTENSION.contains(extension)) {
            // content-type
            // xlsx: application/vnd.openxmlformats-officedocument.wordprocessingml.document
            //       application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
            //
            // xls: application/vnd.ms-excel
            //      application/msword application/x-xls
            ExcelType type = EnumUtils.getEnumIgnoreCase(ExcelType.class, extension);
            return streaming 
                   ? new StreamingExcelExtractor<>(dataSource, headers, startRow, type, sheetIndex)
                   : new ExcelExtractor<>(dataSource, headers, startRow, type, sheetIndex);
        } else {
            throw new RuntimeException("File content type not supported: " + fileName);
        }
    }
}
