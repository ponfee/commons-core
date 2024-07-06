package cn.ponfee.commons;

import java.util.HashSet;
import java.util.Set;

import cn.ponfee.commons.util.UuidUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.reflect.GenericUtils;
import cn.ponfee.commons.util.Networks;
import cn.ponfee.commons.util.ObjectUtils;
import cn.ponfee.commons.spring.SpringContextHolder;
import cn.ponfee.commons.ws.JAXWS;

/**
 * addressUrl: http://localhost:8888/testws/address
 * 
 * Endpoint.publish(addressUrl, new WebServiceImpl());
 * 
 * wsdl-url: http://localhost:8888/testws/address?wsdl
 * 
 * xmlns:ns1="http://service.ws.ponfee.cn/" name="TestService" targetNamespace="http://impl.service.ws.ponfee.cn/">
 * 
 * namespaceURI: targetNamespace="http://impl.service.ws.ponfee.cn/"
 *    localPart: name="TestService"
 * 
 * @author Ponfee
 * @param <T>
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "classpath:spring-context.xml" })
public abstract class WebServiceJaxTest<T> {

    private static final Set<String> PUBLISHED = new HashSet<>();

    private T client;
    private final String addressUrl;
    private final String namespaceURI; // targetNamespace="http://impl.service.ws.ponfee.cn/"
    private final String localPart;    // name="TestService"

    protected WebServiceJaxTest(String namespaceURI, String localPart) {
        this("http://localhost:" + Networks.findAvailablePort(8000) + "/testws/" + UuidUtils.uuid32(), namespaceURI, localPart);
    }

    protected WebServiceJaxTest(String url, String namespaceURI, String localPart) {
        this.addressUrl = url;
        this.namespaceURI = namespaceURI;
        this.localPart = localPart;
    }

    protected final T client() {
        return client;
    }

    @Before
    public final void setUp() {
        Class<T> clazz = GenericUtils.getActualTypeArgument(this.getClass());
        synchronized (WebServiceJaxTest.class) {
            int pos = StringUtils.ordinalIndexOf(addressUrl, "/", 3);
            if (pos == -1) {
                pos = addressUrl.length();
            }
            String prefixUrl = addressUrl.substring(0, pos); // http://domain:port
            if (PUBLISHED.add(prefixUrl)) {
                // 发布web service
                JAXWS.publish(addressUrl, SpringContextHolder.getBean(clazz));
            } else {
                System.err.println("The web service: " + prefixUrl + " are already published.");
            }
        }

        // 创建客户端
        client = JAXWS.client(clazz, addressUrl + "?wsdl", namespaceURI, localPart);

        initiate();
    }

    @After
    public final void tearDown() {
        destory();
    }

    protected void initiate() {
        // do no thing
    }

    protected void destory() {
        // do no thing
    }

    public static void consoleJson(Object obj) {
        try {
            Thread.sleep(100);
            System.err.println(Jsons.toJson(obj));
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void console(Object obj) {
        try {
            Thread.sleep(100);
            System.err.println(obj);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
