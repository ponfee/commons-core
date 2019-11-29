package code.ponfee.commons.model;

import code.ponfee.commons.model.SortOrder.NullHandling;

/**
 * SortOrder and Field
 * 
 * @author Ponfee
 */
public class SortField implements java.io.Serializable {

    private static final long serialVersionUID = -2400506091734529951L;

    private final String field;
    private final SortOrder sortOrder;
    private final boolean ignoreCase;
    private final NullHandling nullHandling;

    public SortField(String field, SortOrder sortOrder) {
        this(field, sortOrder, false, null);
    }

    public SortField(String field, SortOrder sortOrder, 
                     boolean ignoreCase, NullHandling nullHandling) {
        this.field = field;
        this.sortOrder = sortOrder;
        this.ignoreCase = ignoreCase;
        this.nullHandling = nullHandling;
    }

    public String getField() {
        return field;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public NullHandling getNullHandling() {
        return nullHandling;
    }

}
