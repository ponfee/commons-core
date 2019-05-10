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
public abstract class DataExtractor<T> {

    protected final Object dataSource;
    protected final String[] headers;

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

    public abstract void extract(RowProcessor<T> processor) throws IOException;

    public final List<T> extract() throws IOException {
        List<T> list = new ArrayList<>();
        this.extract((rowNumber, data) -> list.add(data));
        return list;
    }

    public final void extract(int batchSize, Consumer<List<T>> action) throws IOException {
        Holder<List<T>> holder = Holder.of(new ArrayList<>(batchSize));
        this.extract((rowNumber, data) -> {
            List<T> list = holder.get();
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
    public final ValidateResult<T> verify(RowValidator<T> validator) throws IOException {
        ValidateResult<T> result = new ValidateResult<>();
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
    protected boolean isNotEmpty(T data) {
        if (data instanceof String[]) {
            for (String str : (String[]) data) {
                if (StringUtils.isNotBlank(str)) {
                    return true;
                }
            }
            return false;
        } else if (data instanceof String) {
            return StringUtils.isNotBlank((String) data);
        } else {
            return data != null;
        }
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
