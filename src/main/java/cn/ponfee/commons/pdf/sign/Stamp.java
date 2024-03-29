/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.pdf.sign;

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
