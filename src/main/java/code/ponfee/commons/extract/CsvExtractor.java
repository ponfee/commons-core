package code.ponfee.commons.extract;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.io.BeforeReadInputStream;
import code.ponfee.commons.io.CharacterEncodingDetector;

/**
 * Csv file data extractor
 * 
 * @author Ponfee
 */
public class CsvExtractor<T> extends DataExtractor<T> {

    private final CSVFormat csvFormat;
    private final boolean specHeaders;

    public CsvExtractor(Object dataSource, String[] headers) {
        this(dataSource, headers, null);
    }

    public CsvExtractor(Object dataSource, String[] headers, CSVFormat csvFormat) {
        super(dataSource, headers);
        this.specHeaders = ArrayUtils.isNotEmpty(headers);
        this.csvFormat = Optional.ofNullable(csvFormat).orElse(CSVFormat.DEFAULT);
        if (this.specHeaders) {
            this.csvFormat.withHeader(headers);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void extract(RowProcessor<T> processor) throws IOException {
        BeforeReadInputStream bris = new BeforeReadInputStream(
            asInputStream(), CharacterEncodingDetector.COUNT
        );
        String charset = CharacterEncodingDetector.detect(bris.getArray()); // 检测文件编码

        try (Reader reader = new InputStreamReader(new BOMInputStream(bris), charset)) {
            int columnSize = specHeaders ? this.headers.length : 0;
            Iterable<CSVRecord> records = csvFormat.parse(reader);
            int i = 0, j, n; T data;
            for (CSVRecord record : records) {
                if (!specHeaders && i == 0) {
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
