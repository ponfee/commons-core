package test.http;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import cn.ponfee.commons.http.Http;
import cn.ponfee.commons.http.HttpParams;
import cn.ponfee.commons.jce.digest.DigestUtils;

public class OldApiTester {
    private static final String URL = "http://192.168.1.49:8080/service-webapp";

    private static final String SECRET = "abc";
    private static final String KEY = "cde";

    @Test
    public void testOldInf() {
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put("source", "IOS");
        headers.put("time", time);
        headers.put("auth", DigestUtils.md5Hex(SECRET + time));

        Map<String, String> params = new HashMap<>();
        params.put("abc", "123");
        params.put("sign", buildSign(params, KEY));

        String resp = Http.post(URL + "/city/getStartCityList.srv").addParam(params).addHeader(headers).request();
        System.out.println(resp);
    }

    @Test
    public void testcarrychildrenaddorupd() {
        Map<String, String> headers = new HashMap<>();
        String userId = "87";
        String time = String.valueOf(System.currentTimeMillis());
        headers.put("source", "IOS");
        headers.put("time", time);
        headers.put("auth", DigestUtils.md5Hex(SECRET + time));
        headers.put("accToken", buildAcctoken(userId));

        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("certNo", "430122198210130031");
        params.put("realName", "realNamecde");
        params.put("sign", buildSign(params, KEY));

        String resp = Http.post(URL + "/user/carrychildrenaddorupd.srv").addParam(params).addHeader(headers).request();
        System.out.println(resp);
    }

    @Test
    public void testcarrychildrendel() {
        Map<String, String> headers = new HashMap<>();
        String userId = "87";
        String time = String.valueOf(System.currentTimeMillis());
        headers.put("source", "IOS");
        headers.put("time", time);
        headers.put("auth", DigestUtils.md5Hex(SECRET + time));
        headers.put("accToken", buildAcctoken(userId));

        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("id", "2");
        params.put("sign", buildSign(params, KEY));

        String resp = Http.post(URL + "/user/carrychildrendel.srv").addParam(params).addHeader(headers).request();
        System.out.println(resp);
    }

    @Test
    public void testcarrychildrenlist() {
        Map<String, String> headers = new HashMap<>();
        String userId = "87";
        String time = String.valueOf(System.currentTimeMillis());
        headers.put("source", "IOS");
        headers.put("time", time);
        headers.put("auth", DigestUtils.md5Hex(SECRET + time));
        headers.put("accToken", buildAcctoken(userId));

        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("sign", buildSign(params, KEY));

        String resp = Http.post(URL + "/user/carrychildrenlist.srv").addParam(params).addHeader(headers).request();
        System.out.println(resp);
    }

    @Test
    public void testgetUserInfo() {
        Map<String, String> headers = new HashMap<>();
        String userId = "1";
        String time = String.valueOf(System.currentTimeMillis());
        headers.put("source", "IOS");
        headers.put("time", time);
        headers.put("auth", DigestUtils.md5Hex(SECRET + time));
        headers.put("accToken", buildAcctoken(userId));

        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("sign", buildSign(params, KEY));

        String resp = Http.post(URL + "/user/getUserInfo.srv").addParam(params).addHeader(headers).request();
        System.out.println(resp);
    }

    @Test
    public void testBanner() {
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put("source", "MOBILE");
        headers.put("time", time);
        headers.put("auth", DigestUtils.md5Hex(SECRET + time));

        Map<String, String> params = new HashMap<>();
        params.put("module", "huodong");
        params.put("clientIp", "127.0.0.1");
        params.put("sign", buildSign(params, KEY));

        String resp = Http.post(URL + "/sys/activeAdvert.srv").addParam(params).addHeader(headers).request();
        System.out.println(resp);
    }

    private String buildSign(Map<String, String> params, String key) {
        String signing = HttpParams.buildSigning(params, new String[] { "sign" });
        if (signing.length() > 0) signing += "&";
        signing += key;
        return DigestUtils.md5Hex(signing.getBytes()).toUpperCase();
    }

    // 加密
    private static String buildAcctoken(String str) {
        try {
            byte[] bkey = GetKeyBytes("acd");

            SecretKey deskey = new SecretKeySpec(bkey, "DESede"); // 加密
            Cipher c1 = Cipher.getInstance("DESede");
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            byte[] bytes = c1.doFinal(URLEncoder.encode(str, "utf-8").getBytes());
            return Base64.encodeBase64String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] GetKeyBytes(String strKey) throws Exception {
        if (null == strKey || strKey.length() < 1) {
            throw new Exception("key is null or empty!");
        }
        MessageDigest alg = MessageDigest.getInstance("MD5");
        alg.update(strKey.getBytes());
        byte[] bkey = alg.digest();
        int start = bkey.length;
        byte[] bkey24 = new byte[24];
        for (int i = 0; i < start; i++) {
            bkey24[i] = bkey[i];
        }
        for (int i = start; i < 24; i++) {
            // 为了与.net16位key兼容
            bkey24[i] = bkey[i - start];
        }
        return bkey24;
    }

}
