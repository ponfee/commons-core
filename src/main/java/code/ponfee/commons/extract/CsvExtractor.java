package code.ponfee.commons.extract;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Csv file data extractor
 * 
 * @author Ponfee
 */
public class CsvExtractor<T> extends DataExtractor<T> {

    private final CSVFormat csvFormat;
    private final boolean specHeaders;
    private final Charset charset;

    public CsvExtractor(Object dataSource, String[] headers) {
        this(dataSource, headers, null, null);
    }

    public CsvExtractor(Object dataSource, String[] headers, 
                        Charset charset, CSVFormat csvFormat) {
        super(dataSource, headers);
        this.specHeaders = ArrayUtils.isNotEmpty(headers);
        this.charset = Optional.ofNullable(charset).orElse(StandardCharsets.UTF_8);
        this.csvFormat = Optional.ofNullable(csvFormat).orElse(CSVFormat.DEFAULT);
        if (this.specHeaders) {
            this.csvFormat.withHeader(headers);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void extract(RowProcessor<T> processor) throws IOException {
        try (InputStream stream = asInputStream();
             BOMInputStream bom = new BOMInputStream(stream);
             Reader reader = new InputStreamReader(bom, charset)
        ) {
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
