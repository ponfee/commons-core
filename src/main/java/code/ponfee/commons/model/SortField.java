package code.ponfee.commons.model;

import code.ponfee.commons.model.SortOrder.NullHandling;

/**
 * SortOrder and Field
 * 
 * @author Ponfee
 */
public class SortField implements java.io.Serializable {

    private static final long serialVersionUID = -2400506091734529951L;

    private final SortOrder sortOrder;
    private final String field;
    private final boolean ignoreCase;
    private final NullHandling nullHandling;

    public SortField(SortOrder sortOrder, String field) {
        this(sortOrder, field, false, null);
    }

    public SortField(SortOrder sortOrder, String field, 
                     boolean ignoreCase, NullHandling nullHandling) {
        this.sortOrder = sortOrder;
        this.field = field;
        this.ignoreCase = ignoreCase;
        this.nullHandling = nullHandling;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public String getField() {
        return field;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public NullHandling getNullHandling() {
        return nullHandling;
    }

}
