package code.ponfee.commons.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class EscapeRegexTest {

    static String s = "t\\\\\\\\e\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\st.a.bc";
    int round = 9999999;

    @Test
    public void test1() {
        System.out.println(Strings.escapeRegex(s));
        Assert.assertEquals(Strings.escapeRegex(s), escapeExprSpecialWord(s));
        for (int i = 0; i < round; i++) {
            Strings.escapeRegex(s);
        }
    }

    @Test
    public void test2() {
        System.out.println(Strings.escapeRegex(s));
        Assert.assertEquals(Strings.escapeRegex(s), escapeExprSpecialWord(s));
        for (int i = 0; i < round; i++) {
            escapeExprSpecialWord(s);
        }
    }

    static String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };

    /** 
     * 转义正则特殊字符 （$()*+.[]?\^{},|） 
     *  
     * @param keyword 
     * @return 
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }
}
