package test.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import code.ponfee.commons.http.ContentType;
import code.ponfee.commons.http.Http;
import code.ponfee.commons.jce.security.KeyStoreResolver;
import code.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import code.ponfee.commons.resource.Resource;
import code.ponfee.commons.resource.ResourceLoaderFacade;
import code.ponfee.commons.util.Bytes;

public class HttpTester {
    //private static final String URL = "http://192.168.1.49:8080/web/";
    private static final String URL = "http://192.168.1.120:8100/";

    @Test
    public void testHttps() {
        InputStream keyInput = Object.class.getResourceAsStream("d:/cert.p12");
        KeyStoreResolver resolver = new KeyStoreResolver(KeyStoreType.PKCS12, keyInput, "1253089901");
        SSLSocketFactory sockFact = resolver.getSSLContext("1253089901").getSocketFactory();

        String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
        String data = "<xml><appid><![CDATA[wxf66ea2204a7a1c58]]></appid><mch_id><![CDATA[1253089901]]></mch_id><nonce_str><![CDATA[rvcmjyhg9205v4wo]]></nonce_str><op_user_id><![CDATA[1253089901]]></op_user_id><out_refund_no><![CDATA[TEST3344556677]]></out_refund_no><refund_fee><![CDATA[1]]></refund_fee><refund_fee_type><![CDATA[CNY]]></refund_fee_type><sign><![CDATA[3D07071971352BCFF5E007B2B7FA9495]]></sign><total_fee><![CDATA[1]]></total_fee><transaction_id><![CDATA[1003110578201511281803217943]]></transaction_id></xml>";
        String resp = Http.post(url).setSSLSocketFactory(sockFact).data(data).request();
        System.out.println(resp);
    }

    @Test
    public void upload() throws IOException {
        String url = URL + "account/v1/user/photoupdate.json";
        Map<String, String> params = new HashMap<>();
        params.put("time", "1478859839449");
        params.put("deviceid", "991182d512da8f4615f7a4eddb878512");
        params.put("platform", "H5");
        params.put("nickName", "厚大司考等哈说11");
        params.put("authToken", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ2aHg0amVkekRsNGUrZElOOWh0RnhIaG5vcnV0UmpyV1YySmFRdWVWODRvPSIsImV4cCI6MTQ4MTQ1MTQ2OCwicmZoIjoxNDc4OTQ1ODY4fQ.kg17ETWHUFbdJVlJnEUgYPC-34PxYnp9eCVvJt3X4ZfM8-FmM112M799Q8vTRyTnG637pfJJfU2PcrB18Xf1MQ");

        String resp = Http.post(url).addPart("photo", "photo.jpg", IOUtils.toByteArray(new FileInputStream("D:\\photo.jpg"))).addParam(params).request();
        //String resp = Http.post(url).part("photo", "photo.jpg", new Byte[]{123,34}).params(params).request();
        System.out.println(resp);
    }

    @Test
    public void testParams() {
        String url = URL + "account/v1/user/info.json";
        Map<String, String> params = new HashMap<>();
        params.put("time", "1478859839449");
        params.put("deviceid", "991182d512da8f4615f7a4eddb878512");
        params.put("platform", "H5");
        params.put("nickName", "厚大司考等哈说11");
        params.put("authToken", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ2aHg0amVkekRsNGUrZElOOWh0RnhIaG5vcnV0UmpyV1YySmFRdWVWODRvPSIsImV4cCI6MTQ4MTQ1MTQ2OCwicmZoIjoxNDc4OTQ1ODY4fQ.kg17ETWHUFbdJVlJnEUgYPC-34PxYnp9eCVvJt3X4ZfM8-FmM112M799Q8vTRyTnG637pfJJfU2PcrB18Xf1MQ");

        String resp = Http.post(url).addParam(params).request();
        System.out.println(resp);
    }

    @Test
    public void testData() throws IOException {
        Resource resource = ResourceLoaderFacade.getResource("qq.coupon/createConfig.json", "UTF-8");
        String json = IOUtils.toString(resource.getStream(), Charset.forName("UTF-8"));
        Http http = Http.post("http://3bc98c2a.ngrok.io/pay-center-testdemo/test/json");
        String s = http.contentType(ContentType.APPLICATION_JSON, "utf-8").data(json).request();
        System.out.println("======================================================\n" + s);

        http = Http.post("http://3bc98c2a.ngrok.io/pay-center-testdemo/test/post");
        s = http.data("a=1&b=2").request();
        System.out.println("======================================================\n" + s);
    }
    
    
    @Test
    public void test2() throws IOException {
        String url = "http://10.118.58.74:8080/express/risk/outeridentify.html";
        String resp = Http.post(url).addPart("file", "import.txt", IOUtils.toByteArray(new FileInputStream("D:\\import.txt"))).request();
        System.out.println(resp);
    }

    @Test // 创建个性大屏
    public void test11() throws IOException {
        String url = "http://10.118.40.20:8080/customizedScreen/addScreenInfo";
        String resp = Http.post(url)
            .addParam("activityId", "")
            .addParam("screenId", "7")
            .addParam("activityName", "test_ponfee4")
            .addParam("activityStartDate", "2019-03-08")
            .addParam("activityEndDate", "2019-03-13")
            .addParam("showTop", "3")
            .addParam("monthlyCard", "2017112915,9999999999")
            .addParam("expressProductsStr", "SE0004,顺丰特惠")
            .addParam("keyword", "kw")
            .addParam("expressAddress", "北京市_北京市")
            .addParam("page_src", "2019-03-08\\4b480f74-6752-495a-ae76-04ef0e2d436f-1061493_7.jpg")
            .addParam("edit_src", "edit_src")
            .addHeader("Cookie", "JSESSIONID=1j8qfy7sh1jf61n5fgc25zwjo4")
            .request();
        System.out.println(resp);
    }
    
    @Test // 创建仓储大屏
    public void test12() throws IOException {
        String url = "http://10.118.40.20:8080/battleRoom/saveActiveInfo2";
        String resp = Http.post(url)
            .addParam("activeName", "test3423432")
            .addParam("activeReady", "2019-02-24")
            .addParam("activeDate", "2019-02-28")
            .addParam("activeDay", "2")
            .addParam("activeNum", "12")
            .addParam("act_warehouseCode", "DV1,DV2")
            .addParam("active_warahouseName", "监视器仓库1,监视器仓库2")
            .addParam("active_sku", "67445276542155002,77445276542155003")
            .addParam("warehouseCodes", "DV1,DV2")
            .addParam("skus", "67445276542155002,77445276542155003")
            .addHeader("Cookie", "JSESSIONID=1j8qfy7sh1jf61n5fgc25zwjo4")
            .request();
        System.out.println(resp);
    }
    
    @Test // 创建快递大屏
    public void test13() throws IOException {
        String url = "http://10.118.40.20:8080/battleRoomExpress/saveActiveInfo";
        String resp = Http.post(url)
            .addParam("activeName", "test3423432")
            .addParam("activeReady", "2019-02-24")
            .addParam("activeDate", "2019-02-28")
            .addParam("activeDay", "2")
            .addParam("activeState", "0")
            .addParam("activeNum", "12")
            .addParam("id", "0")
            .addParam("customerCodes", "2017112815,2017112915,9999999999,3333333333,5555555555,0203002395")
            .addHeader("Cookie", "JSESSIONID=1j8qfy7sh1jf61n5fgc25zwjo4")
            .request();
        System.out.println(resp);
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        //System.out.println(Bytes.hexDump(Http.get("http://www.apachelounge.com/download/VC14/binaries/httpd-2.4.25-win64-VC14.zip").download()));
        
        //Http http = Http.get("http://www.baidu.com");
        //http.download(new FileOutputStream("d:/baidu.com.txt"));
        //System.out.println(http.getStatus());
        
        Http http = Http.get("http://www.stockstar.com");
        System.out.println(Bytes.hexDump(http.download()));
        System.out.println(http.getRespHeaders());
        System.out.println(http.getStatus());
        
        //Http.get("http://www.baidu.com").download("d:/baidu.html");
        //System.out.println(Http.get("http://localhost:8081/audit/getImg").data(ImmutableMap.of("imgPath", "imgPath")).request());
        //String[] params = new String[]{"{\"analyze_type\":\"mine_all_cust\",\"date_type\":4,\"class_name\":\"\"}", "{\"analyze_type\":\"mine_all_cust\",\"date_type\":4,\"class_name\":\"衬衫\"}"};
        //Http.post("http://10.118.58.156:8080/market/custgroup/kanban/count/recommend").data(ImmutableMap.of("conditions[]", params)).request();
        /*@SuppressWarnings("unchecked") 
        Map<String, Object> resp = Http.post("http://10.118.58.74:8080/uploaded/file")
                                       .addParam("param1", "test1213")
                                       .addPart("uploadFile", "abc.pdf", new File("d:/test/abc.pdf"))
                                       .addPart("uploadFile", "word.pdf", new File("d:/test/word.pdf"))
                                       .contentType("multipart/form-data", "UTF-8") // <input type="file" name="upload" />
                                       //.contentType("application/json", "UTF-8") // @RequestBody
                                       //.contentType("application/x-www-form-urlencoded", "UTF-8") // form data
                                       .accept("application/json") // @ResponseBody
                                       //.setSSLSocketFactory(factory) // trust certs store
                                       .request(Map.class);
        System.out.println(resp);*/
    }
}
