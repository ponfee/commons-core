package code.ponfee.commons.util;

import static java.util.concurrent.ThreadLocalRandom.current;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * 图片验证码生成类
 * @author fupf
 */
public class Captchas {

    // 使用到Algerian字体，系统里没有的话需要安装字体
    // 去掉了1,0,i,o几个容易混淆的字符
    private static final char[] CODES = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private static final Color[] COLOR_SPACES = {
        Color.RED, Color.GRAY, Color.YELLOW, Color.WHITE,
        Color.GREEN, Color.CYAN, Color.PINK, Color.BLUE,
        Color.MAGENTA, Color.ORANGE, Color.LIGHT_GRAY
    };

    /**
     * 使用系统默认字符源生成验证码
     * @param size 验证码长度
     * @return
     */
    public static String random(int size) {
        return random(size, CODES);
    }

    /**
     * 使用指定源生成验证码
     * @param size 验证码长度
     * @param sources 验证码字符源
     * @return
     */
    public static String random(int size, char[] sources) {
        if (sources == null || sources.length == 0) {
            sources = CODES;
        }
        StringBuilder codes = new StringBuilder(size);
        for (int i = 0, length = sources.length; i < size; i++) {
            codes.append(sources[SecureRandoms.nextInt(length)]);
        }
        return codes.toString();
    }

    public static void generate(int width, OutputStream out, String code) {
        generate(width, (int) Math.ceil(width * 0.618D), out, code);
    }

    /**
     * 输出指定验证码图片流
     * @param width
     * @param height
     * @param out
     * @param code
     */
    public static void generate(int width, int height, OutputStream out, String code) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color[] colors = new Color[5];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = COLOR_SPACES[current().nextInt(COLOR_SPACES.length)];
        }

        g2.setColor(Color.GRAY); // 设置边框色
        g2.fillRect(0, 0, width, height);

        Color c = getRandColor(200, 250);
        g2.setColor(c); // 设置背景色
        g2.fillRect(0, 2, width, height - 4);

        // 绘制干扰线
        g2.setColor(getRandColor(160, 200)); // 设置线条的颜色
        for (int i = 0; i < 15; i++) {
            int x = current().nextInt(width - 1);
            int y = current().nextInt(height - 1);
            int xl = current().nextInt(6) + 1;
            int yl = current().nextInt(12) + 1;
            g2.drawLine(x, y, x + xl + 40, y + yl + 20);
        }

        // 添加噪点
        float yawpRate = 0.03f; // 噪声率
        int area = (int) (yawpRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = current().nextInt(width);
            int y = current().nextInt(height);
            int rgb = getRandomIntColor();
            image.setRGB(x, y, rgb);
        }

        shear(g2, width, height, c); // 使图片扭曲

        g2.setColor(getRandColor(100, 160));
        int fontSize = height - 14;
        g2.setFont(new Font("Algerian", Font.ITALIC, fontSize));
        char[] chars = code.toCharArray();
        int size = code.length();
        for (int i = 0; i < size; i++) {
            AffineTransform affine = new AffineTransform();
            int signum = (current().nextBoolean() ? 1 : -1);
            affine.setToRotation(
                Math.PI / 4 * current().nextDouble() * signum, 
                width / size * i + fontSize / 2, 
                height / 2
            );
            g2.setTransform(affine);
            int x = 1, y = 4;
            g2.drawChars(
                chars, i, 1, 
                (width - 7) / size * i + x, 
                height / 2 + fontSize / 2 - y
            );
        }

        g2.dispose();

        try {
            ImageIO.write(image, "JPEG", out);
            //JPEGCodec.createJPEGEncoder(out).encode(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //-------------------------private methods
    private static Color getRandColor(int fc, int bc) {
        fc = fc > 255 ? 255 : fc;
        bc = bc > 255 ? 255 : bc;
        int r = fc + current().nextInt(bc - fc);
        int g = fc + current().nextInt(bc - fc);
        int b = fc + current().nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static int getRandomIntColor() {
        int color = 0;
        for (int c : getRandomRgb()) {
            color = (color << 8) | c;
        }
        return color;
    }

    private static int[] getRandomRgb() {
        return new int[] {
            current().nextInt(256),
            current().nextInt(256),
            current().nextInt(256)
        };
    }

    private static void shear(Graphics g, int w1, int h1, Color color) {
        shearX(g, w1, h1, color);
        shearY(g, w1, h1, color);
    }

    private static void shearX(Graphics g, int w, int h, Color color) {
        int period = current().nextInt(2),
             phase = current().nextInt(2);
        double frames = (2 * Math.PI * phase) / 1.0D;

        for (int d, i = 0; i < h; i++) {
            d = (int) ((period >> 1) * Math.sin((double) i / period + frames));
            g.copyArea(0, i, w, 1, d, 0);
            //if (current().nextBoolean()) {
            g.setColor(color);
            g.drawLine(d, i, 0, i);
            g.drawLine(d + w, i, w, i);
            //}
        }
    }

    private static void shearY(Graphics g, int w, int h, Color color) {
        int period = current().nextInt(40) + 10, // 50
            phase = 7;
        double frames = (2 * Math.PI * phase) / 20.0D;

        for (int d, i = 0; i < w; i++) {
            d = (int) ((period >> 1) * Math.sin((double) i / period + frames));
            g.copyArea(i, 0, 1, h, 0, d);
            //if (current().nextBoolean()) {
            g.setColor(color);
            g.drawLine(i, d, i, 0);
            g.drawLine(i, d + h, i, h);
            //}
        }
    }

}
