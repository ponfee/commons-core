package code.ponfee.commons.extract;

import code.ponfee.commons.util.Holder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * The file data extractor
 * 
 * @author Ponfee
 */
public abstract class DataExtractor {

    protected final ExtractableDataSource dataSource;
    protected final String[] headers;
    protected volatile boolean end = false;

    protected DataExtractor(ExtractableDataSource dataSource, String[] headers) {
        this.dataSource = dataSource;
        this.headers = headers;
    }

    public abstract void extract(BiConsumer<Integer, String[]> processor) throws IOException;

    public final List<String[]> extract() throws IOException {
        List<String[]> list = new LinkedList<>();
        this.extract((rowNumber, data) -> list.add(data));
        return list;
    }

    /**
     * Extracts specified count of top rows
     * 
     * @param count the top rows count
     * @return a list
     * @throws IOException if occur io error
     */
    public final List<String[]> extract(int count) throws IOException {
        List<String[]> result = new ArrayList<>(count);
        this.extract((rowNum, row) -> {
            if (rowNum >= count) {
                this.end = true;
                return;
            }
            result.add(row);
        });
        return result;
    }

    public final void extract(int batchSize, Consumer<List<String[]>> action) throws IOException {
        Holder<List<String[]>> holder = Holder.of(new ArrayList<>(batchSize));
        this.extract((rowNumber, data) -> {
            List<String[]> list = holder.get();
            list.add(data);
            if (list.size() == batchSize) {
                action.accept(list);
                holder.set(new ArrayList<>(batchSize));
            }
        });
        if (CollectionUtils.isNotEmpty(holder.get())) {
            action.accept(holder.get());
        }
    }

    /**
     * 验证
     * 
     * @param validator
     * @return
     * @throws IOException
     */
    public final ValidateResult verify(BiFunction<Integer, String[], String> validator) 
        throws IOException {
        ValidateResult result = new ValidateResult();
        this.extract((rowNumber, data) -> {
            String error = validator.apply(rowNumber, data);
            if (StringUtils.isBlank(error)) {
                result.addData(data);
            } else {
                result.addError(
                    new StringBuilder(error.length() + 12)
                        .append("第[").append(rowNumber + 1) // rowNumber start 0
                        .append("]行错误：").append(error).toString()
                );
            }
        });
        return result;
    }

    // ---------------------------------------------------------------------------protected methods
    protected boolean isNotEmpty(String[] data) {
        if (data == null || data.length == 0) {
            return false;
        }

        for (String str : data) {
            if (StringUtils.isNotBlank(str)) {
                return true;
            }
        }
        return false;
    }

}
