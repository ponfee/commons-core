package test.http;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import code.ponfee.commons.http.ContentType;
import code.ponfee.commons.http.Http;
import code.ponfee.commons.http.HttpParams;
import code.ponfee.commons.util.ObjectUtils;

public class HttpParamsTest {

    @Test
    public void test1() {
        String str = "service=http%3A%2F%2Flocalhost%2Fcas-client%2F&test=fds中文a";
        System.out.println("\n=============parseParams==============");
        System.out.println(ObjectUtils.toString(HttpParams.parseParams(str, "UTF-8")));

        System.out.println("\n=============parseUrlParams==============");
        str = "http://localhost:8080/test?service=http%3A%2F%2Flocalhost%2Fcas-client%2F&test=fds中文a";
        System.out.println(ObjectUtils.toString(HttpParams.parseUrlParams(str)));

        System.out.println("\n=============buildParams==============");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("a", new String[] { "1" });
        map.put("b", new String[] { "2" });
        map.put("merReserved", new String[] { "{a=1&b=2}" });
        String queryString = HttpParams.buildParams(map, "utf-8");
        System.out.println(queryString);
        System.out.println(ObjectUtils.toString(HttpParams.parseParams(queryString, "utf-8")));

        System.out.println("\n=============buildUrlPath==============");
        System.out.println(HttpParams.buildUrlPath("/index.html", "utf-8", map));

        System.out.println("\n=============buildForm==============");
        System.out.println(HttpParams.buildForm("http://localhost:8080", map));
    }

    @Test
    public void test2() {
        String url = "http://10.118.58.74:8000/open/api/test?a=1=32=14=12=4=3214=2&abcdef&" + Math.random();
        System.out.println(ObjectUtils.toString(HttpParams.parseUrlParams(url, "UTF-8")));
    }

    @Test
    public void test3() {
        System.out.println(HttpParams.buildUrlPath("url", "UTF-8", "a", "1","b","2"));
    }

    @Test
    public void test4() {
        String soap = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://WebXml.com.cn/\">\n" + 
            "  <soapenv:Body>\n" + 
            "    <ns:getCountryCityByIp>\n" + 
            "      <ns:theIpAddress>119.139.199.75</ns:theIpAddress>\n" + 
            "    </ns:getCountryCityByIp>\n" + 
            "  </soapenv:Body>\n" + 
            "</soapenv:Envelope>";

        //soap = HttpParams.buildSoap("getCountryCityByIp", "http://WebXml.com.cn/", ImmutableMap.of("theIpAddress", "119.139.199.75"));
        System.out.println(soap);

        String resp = Http.post("http://www.webxml.com.cn/WebServices/IpAddressSearchWebService.asmx").contentType(ContentType.TEXT_XML).data(soap).request();
        System.out.println(resp);
    }

    @Test
    public void test5() {
        String soap = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://service.screen.ddt.sf.com/\">\n" + 
            "  <soapenv:Body>\n" + 
            "    <ns:testListJavaBean>\n" + 
            "      <arg0>119.139.199.75</arg0>\n" + 
            "    </ns:testListJavaBean>\n" + 
            "  </soapenv:Body>\n" + 
            "</soapenv:Envelope>";

        //soap = HttpParams.buildSoap("testListJavaBean", "http://service.screen.ddt.sf.com/", ImmutableMap.of("arg0", "1"));
        System.out.println(soap);
        String resp = Http.post("http://localhost:8009/ws/testScreen?wsdl").contentType(ContentType.TEXT_XML).data(soap).request();
        System.out.println(resp);
    }
    
}
