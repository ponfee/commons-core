package code.ponfee.commons.extract;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.io.BeforeReadInputStream;
import code.ponfee.commons.io.CharacterEncodingDetector;
import code.ponfee.commons.io.WindowsBOM;

/**
 * Csv file data extractor
 * 
 * @author Ponfee
 */
public class CsvExtractor<T> extends DataExtractor<T> {

    private final CSVFormat csvFormat;
    private final boolean withHeader;
    private final int startRow; // start with 0
    private final Charset charset;

    protected CsvExtractor(Object dataSource, String[] headers, 
                           CSVFormat csvFormat, int startRow, Charset charset) {
        super(dataSource, headers);
        this.withHeader = ArrayUtils.isNotEmpty(headers);
        this.csvFormat = Optional.ofNullable(csvFormat).orElse(CSVFormat.DEFAULT);
        this.startRow = startRow;
        this.charset = charset;
        if (this.withHeader) {
            this.csvFormat.withHeader(headers);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void extract(RowProcessor<T> processor) throws IOException {
        BeforeReadInputStream bris = new BeforeReadInputStream(
            asInputStream(), CharacterEncodingDetector.DETECT_COUNT
        );

        // 检测文件编码
        Charset encoding = this.charset != null 
                         ? this.charset 
                         : CharacterEncodingDetector.detect(bris.getArray());

        // 检查是否有BOM
        WindowsBOM wbom = WindowsBOM.of(encoding, bris.getArray());
        if (wbom != null) {
            bris.skip(wbom.length());
        }

        try (Reader reader = new InputStreamReader(/*new BOMInputStream(bris)*/bris, encoding)) { // Error: UTF-16LE, UTF-16BE,
            int columnSize = withHeader ? this.headers.length : 0;
            Iterable<CSVRecord> records = csvFormat.parse(reader);
            int i = 0, j, n, start = startRow;
            T data;
            for (CSVRecord record : records) {
                if (super.end) {
                    return;
                }
                if (start > 0) {
                    start--;
                    continue;
                }
                if (!withHeader && i == 0) {
                    columnSize = record.size(); // 不指定表头，则取第一行数据为表头
                }
                n = record.size();
                if (columnSize == 1) {
                    if (n == 0) {
                        data = (T) StringUtils.EMPTY;
                    } else {
                        data = (T) record.get(0);
                    }
                } else {
                    String[] array = new String[columnSize];
                    for (j = 0; j < n && j < columnSize; j++) {
                        array[j] = record.get(j);
                    }
                    for (; j < columnSize; j++) {
                        array[j] = StringUtils.EMPTY;
                    }
                    data = (T) array;
                }
                if (isNotEmpty(data)) {
                    processor.process(i++, data);
                }
            }
        }
    }

}
