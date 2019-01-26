package code.ponfee.commons.export;

import java.awt.Color;
import java.io.Serializable;

import code.ponfee.commons.util.Colors;

/**
 * 每列的元数据配置
 * @author fupf
 */
public class Tmeta implements Serializable {

    private static final long serialVersionUID = -7653917777812920043L;

    private Type type = Type.CHAR;
    private String format;
    private Align align = Align.LEFT;
    private boolean nowrap = false; // only use in html [nowrap="nowrap"]
    private Color color = null;
    private String colorHex = null;

    public Tmeta() {}

    public Tmeta(Type type, String format, Align align, 
                 boolean nowrap, String colorHex) {
        this.type = type;
        this.format = format;
        this.align = align;
        this.nowrap = nowrap;
        setColor(colorHex);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Align getAlign() {
        return align;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public boolean isNowrap() {
        return nowrap;
    }

    public void setNowrap(boolean nowrap) {
        this.nowrap = nowrap;
    }

    public void setColor(Color color) {
        this.color = color;
        this.colorHex = Colors.color2hex(color);
    }

    public void setColor(String color) {
        this.colorHex = color;
        this.color = Colors.hex2color(color);
    }

    public Color getColor() {
        return color;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public enum Type {
        CHAR, NUMERIC, DATETIME
    }

    public enum Align {
        LEFT, CENTER, RIGHT
    }

}
