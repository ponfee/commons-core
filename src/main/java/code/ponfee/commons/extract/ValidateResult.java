package code.ponfee.commons.extract;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import code.ponfee.commons.io.Files;

/**
 * 处理结果
 * 
 * @author Ponfee
 */
public class ValidateResult {

    private final List<String[]> data = Lists.newArrayList();
    private final List<String> errors = Lists.newArrayList();

    public boolean hasErrors() {
        return CollectionUtils.isNotEmpty(errors);
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(data);
    }

    public String getErrorsAsString() {
        return StringUtils.join(errors, Files.UNIX_LINE_SEPARATOR);
    }

    public List<String[]> getData() {
        return data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addData(String[] obj) {
        this.data.add(obj);
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
