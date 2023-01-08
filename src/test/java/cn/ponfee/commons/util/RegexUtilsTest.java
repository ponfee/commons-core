package cn.ponfee.commons.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegexUtilsTest {

    @Test
    public void testIsIPv4() {
        assertTrue(RegexUtils.isIpv4("127.0.0.1"));
        assertTrue(RegexUtils.isIpv4("0.0.0.0"));
        assertTrue(RegexUtils.isIpv4("255.255.255.255"));
    }

    @Test
    public void testIsIPv6() {
        assertTrue(RegexUtils.isIpv6("::1"));
        assertFalse(RegexUtils.isIpv6("1200::AB00:1234::2552:7777:1313"));
        assertTrue(RegexUtils.isIpv6("1200:0000:AB00:1234:0000:2552:7777:1313"));
        assertTrue(RegexUtils.isIpv6("21DA:D3:0:2F3B:2AA:FF:FE28:9C5A"));
    }
}
