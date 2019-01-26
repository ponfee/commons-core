package bean;

import code.ponfee.commons.constrain.Constraint;

import java.io.Serializable;

public class TestBean implements Serializable {
    static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final long serialVersionUID = 1716190333294826147L;
    @Constraint(tense = Constraint.Tense.FUTURE)
    private int i;
    private Long l;
    private String s;
    private String str;

    public TestBean() {}

    public TestBean(int i, Long l, String s) {
        super();
        this.i = i;
        this.l = l;
        this.s = s;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public Long getL() {
        return l;
    }

    public void setL(Long l) {
        this.l = l;
    }

    public String getS() {
        return s;
    }

    @Override
    public String toString() {
        return "Bean [i=" + i + ", l=" + l + ", s=" + s + "]";
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
    
    public static void main(String[] args) {
        System.out.println(tableSizeFor(9));
        System.out.println(MAXIMUM_CAPACITY);
    }
}
