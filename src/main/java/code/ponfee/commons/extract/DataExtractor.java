package code.ponfee.commons.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.util.Holder;

/**
 * The file data extractor
 * 
 * @author Ponfee
 */
public abstract class DataExtractor {

    protected final Object dataSource;
    protected final String[] headers;
    protected volatile boolean end = false;

    protected DataExtractor(Object dataSource, String[] headers) {
        if (   !(dataSource instanceof File)
            && !(dataSource instanceof InputStream)
        ) {
            throw new IllegalArgumentException(
                "Datasouce only support such type as File, InputStream"
            );
        }
        this.dataSource = dataSource;
        this.headers = headers;
    }

    public abstract void extract(RowProcessor processor) throws IOException;

    public final List<String[]> extract() throws IOException {
        List<String[]> list = new ArrayList<>();
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
                holder.getAndSet(new ArrayList<>(batchSize));
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
    public final ValidateResult verify(RowValidator validator) throws IOException {
        ValidateResult result = new ValidateResult();
        this.extract((rowNumber, data) -> {
            String error = validator.verify(rowNumber, data);
            if (StringUtils.isBlank(error)) {
                result.addData(data);
            } else {
                result.addError(
                    new StringBuilder(error.length() + 12)
                        .append("第").append(rowNumber)
                        .append("行错误：").append(error).toString()
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

        for (String str : (String[]) data) {
            if (StringUtils.isNotBlank(str)) {
                return true;
            }
        }
        return false;
    }

    protected final InputStream asInputStream() {
        if (dataSource instanceof File) {
            try {
                return new FileInputStream((File) dataSource);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return (InputStream) dataSource;
        }
    }

}
