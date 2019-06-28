package code.ponfee.commons.model;

/**
 * Search after
 * 
 * @author Ponfee
 */
public class SearchAfter<T> implements java.io.Serializable {

    private static final long serialVersionUID = 4870755106055211046L;

    private final SortField sortField;
    private final T value;

    public SearchAfter(SortField sortField, T value) {
        this.sortField = sortField;
        this.value = value;
    }

    public SortField getSortField() {
        return sortField;
    }

    public T getValue() {
        return value;
    }

    public SearchAfter<T> copy(T value) {
        return new SearchAfter<>(this.sortField, value);
    }

}
