package code.ponfee.commons.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import code.ponfee.commons.io.Files;

/**
 * url编码/解码
 * 
 * @author Ponfee
 */
public final class UrlCoder {

    public static String encodeURI(String url) {
        return encodeURI(url, Files.UTF_8);
    }

    /**
     * 相当于javascript中的encodeURI
     * 不会被此方法编码的字符：! @ # $& * ( ) = : / ; ? + '
     * encodeURI("http://www.oschina.net/search?scope=bbs&q=C语言", "UTF-8") 
     * -> http://www.oschina.net/search?scope=bbs&q=C%E8%AF%AD%E8%A8%80
     * @param url
     * @param charset
     * @return
     */
    public static String encodeURI(String url, String charset) {
        StringBuilder builder = new StringBuilder(url.length() * 3 / 2);
        byte[] b;
        for (int n = url.length(), i = 0; i < n; i++) {
            char c = url.charAt(i);
            if (c >= 0 && c <= 255) {
                builder.append(c);
            } else {
                try {
                    b = Character.toString(c).getBytes(charset);
                } catch (Exception ex) {
                    b = new byte[0];
                }
                for (int k, j = 0; j < b.length; j++) {
                    k = b[j];
                    if (k < 0) {
                        k += 256;
                    }
                    builder.append('%').append(Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return builder.toString();
    }

    public static String decodeURI(String url) {
        return decodeURI(url, Files.UTF_8);
    }

    /**
     * 相当于javascript的decodeURI
     * @param url
     * @param charset
     * @return
     */
    public static String decodeURI(String url, String charset) {
        try {
            return URLDecoder.decode(url, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String encodeURIComponent(String url) {
        return encodeURIComponent(url, Files.UTF_8);
    }

    /**
     * 相当于javascript中的encodeURIComponent
     * 不会被此方法编码的字符：! * ( )
     * encodeURIComponent("http://www.oschina.net/search?scope=bbs&q=C语言", "UTF-8") 
     * -> http%3A%2F%2Fwww.oschina.net%2Fsearch%3Fscope%3Dbbs%26q%3DC%E8%AF%AD%E8%A8%80
     * @param url
     * @param charset
     * @return
     */
    public static String encodeURIComponent(String url, String charset) {
        try {
            return URLEncoder.encode(url, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String decodeURIComponent(String url) {
        return decodeURIComponent(url, Files.UTF_8);
    }

    /**
     * 相当于javascript中的decodeURIComponent
     * @param url
     * @param charset
     * @return
     */
    public static String decodeURIComponent(String url, String charset) {
        return decodeURI(url, charset);
    }

}
