package code.ponfee.commons.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类
 * http://blog.csdn.net/carechere/article/details/52315728
 * 
 * @author Ponfee
 */
public final class RegexUtils {
    private RegexUtils() {}

    private static final LoadingCache<String, Pattern> PATTERNS =
    CacheBuilder.newBuilder().softValues().build(
        new CacheLoader<String, Pattern>() {
            @Override
            public Pattern load(String pattern) {
                return Pattern.compile(pattern/*, Pattern.CASE_INSENSITIVE*/);
            }
        }
    );

    /**
     * Finds the first match string from originalStr use regex
     * 
     * @param originalStr the origin str
     * @param regex       the regex
     * @return the first match string
     */
    public static String findFirst(String originalStr, String regex) {
        return findGroup(originalStr, regex, 0);
    }

    public static String findGroup(String originalStr, String regex, int group) {
        if (originalStr == null || regex == null) {
            return StringUtils.EMPTY;
        }

        try {
            Matcher matcher = PATTERNS.get(regex).matcher(originalStr);
            return matcher.find() ? matcher.group(group) : StringUtils.EMPTY;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean matches(String originalStr, String regex) {
        if (originalStr == null || regex == null) {
            return false;
        }

        try {
            return PATTERNS.get(regex).matcher(originalStr).matches();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------mobile phone regexp
    public static final String REGEXP_MOBILE = "^\\s*(((\\+)?86)|(\\((\\+)?86\\)))?1\\d{10}\\s*$";
    public static final Pattern PATTERN_MOBILE = Pattern.compile(REGEXP_MOBILE);

    /**
     * check is mobile phone
     * @param text
     * @return {@code true} is mobile phone
     */
    public static boolean isMobilePhone(String text) {
        return text != null && PATTERN_MOBILE.matcher(text).matches();
    }

    // ----------------------------------------------------------email regexp
    public static final String REGEXP_EMAIL = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)"
                                            + "[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
    public static final Pattern PATTERN_EMAIL = Pattern.compile(REGEXP_EMAIL);

    /**
     * 校验是否邮箱地址
     * @param text
     * @return {@code true} is email address
     */
    public static boolean isEmail(String text) {
        return text != null && PATTERN_EMAIL.matcher(text).matches();
    }

    // ----------------------------------------------------------ip regexp
    public static final String REGEXP_IP = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}"
                                         + "(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
    private static final Pattern PATTERN_IP = Pattern.compile(REGEXP_IP);

    /**
     * 校验是否ip地址
     * @param text
     * @return {@code true} is ip address
     */
    public static boolean isIp(String text) {
        return text != null && PATTERN_IP.matcher(text).matches();
    }

    // ----------------------------------------------------------username regexp
    public static final String REGEXP_USERNAME = "^[0-9A-Za-z_\\-]{4,20}$";
    public static final Pattern PATTERN_USERNAME = Pattern.compile(REGEXP_USERNAME);

    /**
     * 校验是否是有效的用户名
     * 数据库用户名字段最好不要区分大小写
     * @param text
     * @return {@code true} is valid user name
     */
    public static boolean isValidUserName(String text) {
        return text != null && PATTERN_USERNAME.matcher(text).matches();
    }

    // ----------------------------------------------------------password regexp
    private static final String SYMBOL = "@#!%&_\\.\\?\\-\\$\\^\\*";
    public static final String REGEXP_PASSWORD = "^((?=.*\\d)(?=.*[A-Za-z])|(?=.*\\d)(?=.*[" + SYMBOL 
                         + "])|(?=.*[A-Za-z])(?=.*[" + SYMBOL + "]))[\\dA-Za-z" + SYMBOL + "]{8,20}$";
    public static final Pattern PATTERN_PASSWORD = Pattern.compile(REGEXP_PASSWORD);

    /**
     * 校验是否是有效的密码：
     *   > 8-20位
     *   > 必须包含字母、数字、符号中至少2种（可选的符号包括：@#!%&_.?-$^*）
     *   > 其它模式：^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])[\\dA-Za-z@#!%&_\\.\\?\\-\\$\\^\\*]{8,20}$
     *            ：^(?=.*\\d)(?=.*[A-Za-z])[\\dA-Za-z@#!%&_\\.\\?\\-\\$\\^\\*]{8,20}$
     *
     * isValidPassword("12131111") // false: 只有数字
     * isValidPassword("@#.@#.$^") // false: 只有字符
     * isValidPassword("aaaaaaaa") // false: 只有字母
     * isValidPassword("121311@1") // true: 数字字符
     * isValidPassword("121311A1") // true: 数字字母
     * isValidPassword("aaaaaa.a") // true: 字母字符
     * @param text
     * @return {@code true} is valid password
     */
    public static boolean isValidPassword(String text) {
        return text != null && PATTERN_PASSWORD.matcher(text).matches();
    }

    // ----------------------------------------------------------yyyyMMdd(HHmmss(SSS))
    public static final String REGEXP_DATE = 
        "^([1-9]\\d{3}((0[1-9]|1[012])(0[1-9]|1\\d|2[0-8])|(0[13456789]|1[012])(29|30)|(0[13578]|1[02])31)|(([2-9]\\d)(0[48]|[2468][048]|[13579][26])|(([2468][048]|[3579][26])00))0229)"
      + "(([0-1][0-9]|2[0-3])([0-5][0-9])([0-5][0-9])(\\d{3})?)?$";
    public static final Pattern PATTERN_DATE = Pattern.compile(REGEXP_DATE);
    /**
     * Validates the text whether date pattern
     * 
     * @param text the string
     * @return if returns {@code true} then is a valid date pattern
     */
    public static boolean isDatePattern(String text) {
        return text != null
            && PATTERN_DATE.matcher(text).matches(); 
    }

    /**
     * 中国电信号码格式验证 手机段： 133,153,180,181,189,177,1700,173,199
     **/
    public static final Pattern CHINA_TELECOM_PATTERN = Pattern.compile("(^1(33|53|77|73|99|8[019])\\d{8}$)|(^1700\\d{7}$)");

    /**
     * 中国联通号码格式验证 手机段：130,131,132,155,156,185,186,145,176,1709
     **/
    public static final Pattern CHINA_UNICOM_PATTERN = Pattern.compile("(^1(3[0-2]|4[5]|5[56]|7[6]|8[56])\\d{8}$)|(^1709\\d{7}$)");

    /**
     * 中国移动号码格式验证
     * 手机段：134,135,136,137,138,139,150,151,152,157,158,159,182,183,184,187,188,147,178,1705
     **/
    public static final Pattern CHINA_MOBILE_PATTERN = Pattern.compile("(^1(3[4-9]|4[7]|5[0-27-9]|7[8]|8[2-478])\\d{8}$)|(^1705\\d{7}$)");

    /**
     * 获取移动号码运营商类型
     * 
     * @param mobilePhone the mobile phone
     * @return 0未知；1移动； 2联通；3电信；
     */
    public static int getPhoneCarrier(String mobilePhone) {
        if (StringUtils.isBlank(mobilePhone)) {
            return 0;
        } else if (CHINA_MOBILE_PATTERN.matcher(mobilePhone).matches()) {
            return 1;
        } else if (CHINA_UNICOM_PATTERN.matcher(mobilePhone).matches()) {
            return 2;
        } else if (CHINA_TELECOM_PATTERN.matcher(mobilePhone).matches()) {
            return 3;
        } else {
            return 0;
        }
    }

    // -------------------------------------------------------------------
    private static final String[] DATE_PATTERN = {
        "yyyyMMdd", "yyyyMMddHHmmss", "yyyyMMddHHmmssSSS"
    };
    public static boolean isValidDate(String text) {
        try {
            DateUtils.parseDateStrictly(text, DATE_PATTERN);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
