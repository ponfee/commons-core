package code.ponfee.commons.extract;

import code.ponfee.commons.io.Files;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Validate result
 * 
 * @author Ponfee
 */
public class ValidateResult {

    private final List<String[]> data = Lists.newLinkedList();
    private final List<String> errors = Lists.newLinkedList();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isEmpty() {
        return data.isEmpty();
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
