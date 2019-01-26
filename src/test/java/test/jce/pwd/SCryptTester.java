// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package test.jce.pwd;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Base64;

import org.junit.Assert;
import org.junit.Test;

import code.ponfee.commons.jce.passwd.SCrypt;

public class SCryptTester {
    String passwd = "secret";

    @Test
    public void scrypt() {
        int N = 8388608;
        int r = 1;
        int p = 1;

        String hashed = SCrypt.create(passwd, N, r, p);
        String[] parts = hashed.split("\\$");

        assertEquals(5, parts.length);
        assertEquals("s0", parts[1]);
        Assert.assertEquals(16, Base64.getUrlDecoder().decode(parts[3]).length);
        assertEquals(32, Base64.getUrlDecoder().decode(parts[4]).length);

        long params = Long.parseLong(parts[2], 16);

        // 0xe0801 >> 16  ->  0xe
        assertEquals(N, (int) Math.pow(2, params >> 16 & 0xffff));
        assertEquals(r, params >> 8 & 0xff);
        assertEquals(p, params >> 0 & 0xff);
    }

    @Test
    public void check() {
        String hashed = SCrypt.create(passwd, 16384, 8, 1);

        assertTrue(SCrypt.check(passwd, hashed));
        assertFalse(SCrypt.check("s3cr3t", hashed));
    }

    @Test
    public void format_0_rp_max() {
        int N = 2;
        int r = 255;
        int p = 255;

        String hashed = SCrypt.create(passwd, N, r, p);
        assertTrue(SCrypt.check(passwd, hashed));

        String[] parts = hashed.split("\\$");
        long params = Long.parseLong(parts[2], 16);

        assertEquals(N, (int) Math.pow(2, params >>> 16 & 0xffff));
        assertEquals(r, params >> 8 & 0xff);
        assertEquals(p, params >> 0 & 0xff);
    }

    public static void main(String[] args) {
        System.out.println(MAX_VALUE / 128 / 255);
        System.out.println(Long.toString(8388608, 16));
        System.out.println(Math.pow(2, 0xe));
    }
}
