package code.ponfee.commons.model;

/**
 * Representing a boolean status
 *
 * @author Ponfee
 */
public enum YesNo {

    YES(1, "是"), //

    NO (0, "否"), // 

    ;

    private final int value;
    private final String desc;

    YesNo(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int value() {
        return value;
    }

    public String desc() {
        return this.desc;
    }

    // ------------------------------------------------ equals methods
    public boolean equals(Integer value) {
        return equals(value == null ? NO.value : value);
    }

    public boolean equals(int value) {
        if (value != YES.value && value != NO.value) {
            throw new IllegalArgumentException("Invalid value '" + value + "'");
        }
        return this.value == value;
    }

    // ------------------------------------------------ check whether the value is yes
    public static boolean yes(Integer value) {
        return YES.equals(value);
    }

    public static boolean yes(int value) {
        return YES.equals(value);
    }

    // ------------------------------------------------ check whether the value is no
    public static boolean no(Integer value) {
        return NO.equals(value);
    }

    public static boolean no(int value) {
        return NO.equals(value);
    }
}
