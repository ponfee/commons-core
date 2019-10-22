package code.ponfee.commons.pdf.sign;

/**
 * 印章信息
 * 
 * @author Ponfee
 */
public class Stamp implements java.io.Serializable {
    private static final long serialVersionUID = -6348664154098224106L;

    private final int   pageNo;
    private final float left;
    private final float bottom;

    public Stamp(int pageNo, float left, float bottom) {
        this.pageNo = pageNo;
        this.left = left;
        this.bottom = bottom;
    }

    public int getPageNo() {
        return pageNo;
    }

    public float getLeft() {
        return left;
    }

    public float getBottom() {
        return bottom;
    }

}
