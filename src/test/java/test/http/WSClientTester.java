package test.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.junit.Test;

public class WSClientTester {

    @Test
    public void testSoap1() throws Exception {
        String soap = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><getSupportCity xmlns=\"http://WebXml.com.cn/\"><byProvinceName></byProvinceName></getSupportCity></soap:Body></soap:Envelope>";
        URL url = new URL("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?wsdl");
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);

        conn.setRequestProperty("Content-Length", Integer.toString(soap.length()));
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conn.setRequestProperty("SOAPAction", "http://WebXml.com.cn/getSupportCity");

        OutputStream os = conn.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "utf-8");
        osw.write(soap);
        osw.flush();
        osw.close();
        StringBuilder sTotalString = new StringBuilder();
        String sCurrentLine = "";
        InputStream is = conn.getInputStream();
        BufferedReader l_reader = new BufferedReader(new InputStreamReader(is));
        while ((sCurrentLine = l_reader.readLine()) != null) {
            sTotalString.append(sCurrentLine);
        }
        System.out.println(sTotalString.toString());
    }

    @Test
    public void testSoap2() throws Exception {
        SOAPMessage message = MessageFactory.newInstance().createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();

        /*SOAPHeader header = envelope.getHeader();
        if (header == null) header = envelope.addHeader();
        header.addHeaderElement( new QName("http://www.tyky.com.cn/cMashup/" , "license" , "tns")).setValue("this a license" );*/

        SOAPBody body = envelope.getBody();
        SOAPBodyElement elem = body.addBodyElement(new QName("http://www.tyky.com.cn/cMashup/", "UpdateCAStatusResult", "tns"));

        SOAPElement arrayOfKeyValueElem = elem.addChildElement("ArrayOfKeyValueOfstringstring");

        SOAPElement keyValueElem1 = arrayOfKeyValueElem.addChildElement("KeyValueOfstringstring");
        keyValueElem1.addChildElement("Key").setValue("123456");
        keyValueElem1.addChildElement("value").setValue("org");

        SOAPElement keyValueElem2 = arrayOfKeyValueElem.addChildElement("KeyValueOfstringstring");
        keyValueElem2.addChildElement("Key").setValue("0001");
        keyValueElem2.addChildElement("value").setValue("user");

        URL url = new URL("http://112.95.149.106:8088/SystemPadServices.svc?wsdl");
        QName qName = new QName("http://tempuri.org/", "SystemPadService");
        Service service = Service.create(url, qName);
        Dispatch<SOAPMessage> dispatch = service.createDispatch(new QName("http://www.tyky.com.cn/cMashup/", "SystemPadServicePort"), SOAPMessage.class, Service.Mode.MESSAGE);
        SOAPMessage msg = dispatch.invoke(message);
        System.out.println(msg.getSOAPBody().getElementsByTagName("addResult").item(0).getTextContent());
    }

    @Test
    public void testPost() throws Exception {
        StringBuilder sTotalString = new StringBuilder();
        URL urlTemp = new URL("http://112.95.149.106:8088/SystemPadServices.svc/UpdateCAStatus/Platform");
        HttpURLConnection connection = (HttpURLConnection) urlTemp.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-type", "application/json");
        connection.setRequestMethod("POST");
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        out.write("{\"caUser\":[{\"Key\":\"123123\",\"Value\":\"org\"}]}");
        out.flush();
        out.close();

        String sCurrentLine;
        InputStream l_urlStream = connection.getInputStream();// 请求
        BufferedReader l_reader = new BufferedReader(new InputStreamReader(l_urlStream));
        while ((sCurrentLine = l_reader.readLine()) != null) {
            sTotalString.append(sCurrentLine);
        }
        System.out.println(sTotalString.toString());
    }

    /*@Test
    public void testCXFDynamic() {
        org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory clientFactory = org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory.newInstance();
        String url = "http://www.webxml.com.cn/webservices/qqOnlineWebService.asmx?wsdl"; // http://www.fjyxd.com:17001/DefDispatcher/dispatche?wsdl
        org.apache.cxf.endpoint.Client clientTemp = clientFactory.createClient(url);
        try {
            // 查询QQ在线状态：Y在线；N离线；E号码错误；A商业用户验证失败；V免费用户超过数量；
            Object[] objects = clientTemp.invoke("qqCheckOnline", "8698053");
            System.out.println(com.alibaba.fastjson.JSON.toJSONString(objects));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*String endpoint = "http://10.202.16.116:8080/cos_webservice/services/CreateComplainService";
    String request = "{\"carryId\":\"070034424401\",\"title\":\"\",\"complainLevel\":\"15\",\"urgency\":\"11\",\"complainChannel\":\"60014708\",\"complainDetailChannel\":\"\",\"complainSource\":\"30\",\"linkman\":\"张先生\",\"linkmanPhone\":\"15623850085\",\"receiveSendChoose\":\"19057\",\"contactLink\":\"101000000\",\"focus\":\"101030000\",\"customerFeedback\":\"101030100\",\"content\":\"此单客户来电反馈，商品其在4月3日上午9点订购的，5号19点多才发出来已经超过48小时了，其现在要求退款，还请将此单商品拦截不要派送，点部作废即可，谢谢SSM姜亮\",\"internalComplainSource\":\"CCS5-SYSTEM\",\"dealWithAreaCode\":\"755Y\",\"problemLink\":\"\",\"monthAccount\":\"\"}";
    @Test
    public void test1() throws Exception {
        net.bingosoft.complain.CreateComplainServicePortType Service = new net.bingosoft.complain.CreateComplainServicePortTypeProxy(endpoint);
        String resp = Service.newComplainFromCommon(request);
        System.out.println(resp);
    }
    @Test
    public void test2() throws Exception {
        org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory dcf = org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory.newInstance();
        org.apache.cxf.endpoint.Client client = dcf.createClient(endpoint + "?wsdl");
        Object[] objects = client.invoke("newComplainFromCommon", request);
        System.out.println(com.alibaba.fastjson.JSON.toJSONString(objects));
    }*/
}
