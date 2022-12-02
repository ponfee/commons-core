package code.ponfee.commons.extract;

import code.ponfee.commons.io.ByteOrderMarks;
import code.ponfee.commons.io.CharsetDetector;
import code.ponfee.commons.io.PrereadInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;

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

    protected CsvExtractor(ExtractableDataSource dataSource, String[] headers, 
                           CSVFormat csvFormat, int startRow, Charset charset) {
        super(dataSource, headers);
        this.withHeader = ArrayUtils.isNotEmpty(headers);
        this.startRow = startRow;
        this.charset = charset;
        CSVFormat.Builder builder = CSVFormat.Builder.create(ObjectUtils.defaultIfNull(csvFormat, CSVFormat.DEFAULT));
        if (this.withHeader) {
            builder.setHeader(headers);
        }
        this.csvFormat = builder.build();
    }

    @Override
    public void extract(BiConsumer<Integer, String[]> processor) throws IOException {
        PrereadInputStream bris = new PrereadInputStream(
            super.dataSource.asInputStream(), CharsetDetector.DEFAULT_DETECT_LENGTH
        );

        // 检测文件编码
        Charset encoding = this.charset != null 
                         ? this.charset 
                         : CharsetDetector.detect(bris.heads());

        // 检查是否有BOM
        ByteOrderMarks bom = ByteOrderMarks.of(encoding, bris.heads());
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
                    data[j] = null;
                }
                if (isNotEmpty(data)) {
                    processor.accept(i++, data);
                }
            }
        }
    }

}
