package code.ponfee.commons.model;

/**
 * Query Order by
 * 
 * @author Ponfee
 */
public enum SortOrder {

    ASC, DESC;

    public static SortOrder of(String name) {
        return "ASC".equalsIgnoreCase(name) ? ASC : DESC;
    }

    public static enum NullHandling {
        NATIVE, NULLS_FIRST, NULLS_LAST;
    }

}
