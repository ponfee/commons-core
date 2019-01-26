package test.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import code.ponfee.commons.http.Http;

public class NewApiTester {
    private static final String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtVU9SMk5XVzJsUXVNd1VxUlRuWjR3PT0iLCJleHAiOjE0OTEwMTY1NTUsInJmaCI6MTQ4ODUxMDk1NX0.mjeVWE3vEfI89r65DtoqRGFkUSx-KeQL0SE5liUrwSIG3mh7ptovcDkpjq6oqJNnMrul3vOHNWhMjBbwt0lFAQ";
    private static final String URL = "http://192.168.1.122:8100";

    @Test
    public void testChildrenAdd() {
        String url = URL + "/account/v1/contact/childrenadd.json";
        Map<String, String> params = new HashMap<>();
        params.put("time", "1478859839449");
        params.put("deviceid", "991182d512da8f4615f7a4eddb878512");
        params.put("platform", "H5");
        params.put("authToken", TOKEN);

        params.put("realName", "abcdef");
        params.put("certNo", "430121198901227354");

        String resp = Http.post(url).addParam(params).request();
        System.out.println(resp);
    }

    @Test
    public void testChildrenUpd() {
        String url = URL + "/account/v1/contact/childrenupd.json";
        Map<String, String> params = new HashMap<>();
        params.put("time", "431121198910163461");
        params.put("deviceid", "991182d512da8f4615f7a4eddb878512");
        params.put("platform", "H5");
        params.put("authToken", TOKEN);

        params.put("realName", "ccccccc");
        params.put("certNo", "430121198901227354");
        params.put("id", "5");

        String resp = Http.post(url).addParam(params).request();
        System.out.println(resp);
    }

    @Test
    public void testChildrenDel() {
        String url = URL + "/account/v1/contact/childrendel.json";
        Map<String, String> params = new HashMap<>();
        params.put("time", "1478859839449");
        params.put("deviceid", "991182d512da8f4615f7a4eddb878512");
        params.put("platform", "H5");
        params.put("authToken", TOKEN);

        params.put("id", "6");

        String resp = Http.post(url).addParam(params).request();
        System.out.println(resp);
    }

    @Test
    public void testChildrenlist() {
        String url = URL + "/account/v1/contact/childrenlist.json";
        Map<String, String> params = new HashMap<>();
        params.put("time", "1478859839449");
        params.put("deviceid", "991182d512da8f4615f7a4eddb878512");
        params.put("platform", "H5");
        params.put("authToken", TOKEN);

        String resp = Http.post(url).addParam(params).request();
        System.out.println(resp);
    }

}
