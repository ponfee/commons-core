/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import cn.ponfee.commons.io.Closeables;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 图片工具类
 * 
 * @author Ponfee
 */
public class ImageUtils {

    /**
     * 获取图片大小
     * @param input
     * @return [width, height]
     */
    public static int[] getImageSize(InputStream input) {
        try {
            BufferedImage image = ImageIO.read(input);
            return new int[] { image.getWidth(), image.getHeight() };
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Closeables.console(input);
        }
    }

    /**
     * 横向合并图片
     * @param format
     * @param imgs
     * @return
     */
    public static byte[] mergeHorizontal(String format, InputStream... imgs) {
        int width = 0, height = 0;
        try {
            List<BufferedImage> list = new ArrayList<>();
            for (InputStream img : imgs) {
                BufferedImage i = ImageIO.read(img);
                width += i.getWidth();// 图片宽度
                height = Math.max(height, i.getHeight());
                list.add(i);
            }

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            width = 0;
            for (BufferedImage i : list) {
                int[] array = new int[i.getWidth() * i.getHeight()];// 从图片中读取RGB
                array = i.getRGB(0, 0, i.getWidth(), i.getHeight(), array, 0, i.getWidth());
                result.setRGB(width, 0, i.getWidth(), i.getHeight(), array, 0, i.getWidth());// 设置左半部分的RGB
                width += i.getWidth();// 图片宽度
                i.flush();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(result, format, out);
            out.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("图片合并失败", e);
        }
    }

    /**
     * 纵向合并图片
     * @param format  png,jpeg,gif
     * @param imgs
     * @return
     */
    public static byte[] mergeVertical(String format, InputStream... imgs) {
        try {
            int width = 0, height = 0;
            List<BufferedImage> list = new ArrayList<>();
            for (InputStream img : imgs) {
                BufferedImage i = ImageIO.read(img);
                height += i.getHeight();// 图片宽度
                width = Math.max(width, i.getWidth());
                list.add(i);
            }

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            height = 0;
            for (BufferedImage i : list) {
                int[] array = new int[i.getWidth() * i.getHeight()];// 从图片中读取RGB
                array = i.getRGB(0, 0, i.getWidth(), i.getHeight(), array, 0, i.getWidth());
                result.setRGB(0, height, i.getWidth(), i.getHeight(), array, 0, i.getWidth());// 设置左半部分的RGB
                height += i.getHeight();// 图片宽度
                i.flush();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(result, format, out);
            out.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("图片合并失败", e);
        }
    }

    /**
     * <pre>
     * 图片透明处理
     *     白    rgb:-1-->255,255,255
     *     红    rgb:-65536-->255,0,0
     *   透明    rgb:0-->0,0,0
     *     红    rgb:-922812416-->255,0,0
     *     黑    rgb:-16777216-->0,0,0
     *     黑    rgb:-939524096-->0,0,0
     *  </pre>
     * @param image
     * @return
     */
    public static byte[] transparent(InputStream image, int refer, int normal) {
        try {
            ImageIcon icon = new ImageIcon(ImageIO.read(image));
            BufferedImage img = new BufferedImage(
                icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR
            );
            Graphics2D g2D = (Graphics2D) img.getGraphics();
            g2D.drawImage(icon.getImage(), 0, 0, icon.getImageObserver());
            for (int alpha, rgb, j, i = img.getMinX(); i < img.getWidth(); i++) {
                for (j = img.getMinY(); j < img.getHeight(); j++) {
                    rgb = img.getRGB(i, j);
                    if (rgb != 0) { // 0为透明
                        if (compare(rgb, refer)) {
                            alpha = 0; // -1为白色：255 255 255
                        } else {
                            alpha = normal; // 默认设置半透明
                        }
                        rgb = (alpha << 24) | (rgb & 0x00ffffff); // 计算rgb
                        img.setRGB(i, j, rgb); // 重新设置rgb
                    }
                }
            }
            //g2D.drawImage(bufferedImage, 0, 0, imageIcon.getImageObserver());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 图片类型
     * @param img
     * @return
     * @throws IOException
     */
    public static String[] getImageType(InputStream img) throws IOException {
        List<String> types = new ArrayList<>();
        try (MemoryCacheImageInputStream m = new MemoryCacheImageInputStream(img)) {
            for (Iterator<ImageReader> i = ImageIO.getImageReaders(m); i.hasNext(); ) {
                types.add(i.next().getFormatName());
            }
            return types.isEmpty() ? null : types.toArray(new String[0]);
        }
    }

    private static boolean compare(int color, int colorRange) {
        int r = (color & 0xff0000) >> 16;
        int g = (color & 0x00ff00) >> 8;
        int b = (color & 0x0000ff);
        return (r >= colorRange && g >= colorRange && b >= colorRange);
    }

}
