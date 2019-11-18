package code.ponfee.commons.model;

/**
 * Representing a boolean flag status
 * 
 * @author Ponfee
 */
public enum Status {

    DISABLED(0, "禁用"), // 

    ENABLED (1, "启用"), //

    ;

    private final int value;
    private final String desc;

    Status(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int value() {
        return this.value;
    }

    public String desc() {
        return this.desc;
    }

    public boolean equals(int value) {
        return this.value == value;
    }

    public static boolean isEnabled(int value) {
        return ENABLED.equals(value);
    }

    public static boolean isDisabled(int value) {
        return DISABLED.equals(value);
    }

}
