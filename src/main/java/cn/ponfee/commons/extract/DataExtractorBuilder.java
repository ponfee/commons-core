/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.extract;

import cn.ponfee.commons.extract.ExcelExtractor.ExcelType;
import cn.ponfee.commons.extract.streaming.StreamingExcelExtractor;
import cn.ponfee.commons.http.ContentType;
import com.google.common.collect.ImmutableList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * The data extractor builder, facade operator
 * 
 * @author Ponfee
 */
public class DataExtractorBuilder {

    private static final List<String> EXCEL_EXTENSION = ImmutableList.of("xlsx", "xls");
    private static final List<String> CSV_EXTENSION = ImmutableList.of("csv", "log", "txt");

    private final Object dataSource; // only support such type as File, InputStream
    private final String fileName;
    private final String contentType;
    private String[] headers;
    private int startRow = 0; // start with 0

    // ------------------------------------------excel config
    private int sheetIndex = 0; // excel work book sheet index: start with 0
    private boolean streaming = true; // excel whether streaming read, default true

    // ------------------------------------------csv config
    private CSVFormat csvFormat; // csv format
    private Charset charset; // csv file encoding

    private DataExtractorBuilder(Object dataSource, String fileName, 
                                 String contentType) {
        this.dataSource = dataSource;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public static DataExtractorBuilder newBuilder(InputStream dataSource, 
                                                  String fileName, String contentType) {
        return new DataExtractorBuilder(dataSource, fileName, contentType);
    }

    public static DataExtractorBuilder newBuilder(String path) {
        return newBuilder(new File(path));
    }

    public static DataExtractorBuilder newBuilder(File dataSource) {
        String fileName = dataSource.getName();
        return new DataExtractorBuilder(
            dataSource, fileName, FilenameUtils.getExtension(fileName)
        );
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

    public DataExtractorBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public DataExtractor build() {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        if (   ContentType.TEXT_PLAIN.value().equalsIgnoreCase(contentType)
            || CSV_EXTENSION.contains(extension)
        ) {
            // csv, txt文本格式数据
            ExtractableDataSource ds = new ExtractableDataSource(dataSource);
            return new CsvExtractor(ds, headers, csvFormat, startRow, charset);
        } else if (EXCEL_EXTENSION.contains(extension)) {
            // Content-Type
            // xlsx: application/vnd.openxmlformats-officedocument.wordprocessingml.document
            //       application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
            //
            // xls: application/vnd.ms-excel
            //      application/x-xls
            ExtractableDataSource ds = new ExtractableDataSource(dataSource);
            ExcelType type = EnumUtils.getEnumIgnoreCase(ExcelType.class, extension);
            return streaming 
                   ? new StreamingExcelExtractor(ds, headers, startRow, type, sheetIndex)
                   : new ExcelExtractor(ds, headers, startRow, type, sheetIndex);
        } else {
            throw new RuntimeException("File content type not supported: " + fileName);
        }
    }

}
