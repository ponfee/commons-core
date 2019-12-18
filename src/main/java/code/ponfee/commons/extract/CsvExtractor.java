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
import code.ponfee.commons.io.ByteOrderMarks;

/**
 * Csv file data extractor
 * 
 * @author Ponfee
 */
public class CsvExtractor extends DataExtractor {

    private final CSVFormat csvFormat;
    private final boolean withHeader;
    private final int startRow; // start with 0
    private final Charset charset;

    protected CsvExtractor(Object dataSource, String[] headers, 
                           CSVFormat csvFormat, int startRow, 
                           Charset charset) {
        super(dataSource, headers);
        this.withHeader = ArrayUtils.isNotEmpty(headers);
        this.csvFormat = Optional.ofNullable(csvFormat).orElse(CSVFormat.DEFAULT);
        this.startRow = startRow;
        this.charset = charset;
        if (this.withHeader) {
            this.csvFormat.withHeader(headers);
        }
    }

    @Override
    public void extract(RowProcessor processor) throws IOException {
        BeforeReadInputStream bris = new BeforeReadInputStream(
            asInputStream(), CharacterEncodingDetector.DETECT_COUNT
        );

        // 检测文件编码
        Charset encoding = this.charset != null 
                         ? this.charset 
                         : CharacterEncodingDetector.detect(bris.getArray());

        // 检查是否有BOM
        ByteOrderMarks bom = ByteOrderMarks.of(encoding, bris.getArray());
        if (bom != null) {
            bris.skip(bom.length());
        }

        // Use BOMInputStream maybe occur error(dead loop): UTF-16LE, UTF-16BE,
        try (Reader reader = new InputStreamReader(/*new BOMInputStream(bris)*/bris, encoding)) {
            int columnSize = this.withHeader ? this.headers.length : 0;
            Iterable<CSVRecord> records = this.csvFormat.parse(reader);
            int i = 0, j, n, start = this.startRow;
            String[] data;
            for (CSVRecord record : records) {
                if (super.end) {
                    break;
                }
                if (start > 0) {
                    start--;
                    continue;
                }
                if (!this.withHeader && i == 0) {
                    columnSize = record.size(); // 不指定表头，则取第一行数据为表头
                }

                n = record.size();
                data = new String[columnSize];
                for (j = 0; j < n && j < columnSize; j++) {
                    data[j] = record.get(j);
                }
                for (; j < columnSize; j++) {
                    data[j] = StringUtils.EMPTY;
                }
                if (isNotEmpty(data)) {
                    processor.process(i++, data);
                }
            }
        }
    }

}
