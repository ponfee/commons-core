package code.ponfee.commons;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.commons.ws.JAXWS;

/**
 * webservice-url: http://localhost:8888/testws/address
 * 
 * Endpoint.publish(webservice-url, new WebServiceImpl());
 * 
 * wsdl-url: http://localhost:8888/testws/address?wsdl
 * 
 * xmlns:ns1="http://service.screen.ddt.sf.com/" name="TestScreenBizService" targetNamespace="http://impl.service.screen.ddt.sf.com/">
 * 
 * namespaceURI: targetNamespace="http://impl.service.screen.ddt.sf.com/"
 *    localPart: name="TestScreenBizService"
 * 
 * @author Ponfee
 * @param <T>
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "classpath:spring-context.xml" })
public abstract class WebServiceTest<T> {

    private static final Set<String> PUBLISHED = new HashSet<>();

    private T client;
    private final String addressUrl;
    private final String namespaceURI; // targetNamespace="http://impl.service.screen.ddt.sf.com/"
    private final String localPart;    // name="TestScreenBizService"

    protected WebServiceTest(String namespaceURI, String localPart) {
        this("http://localhost:8888/testws/" + ObjectUtils.uuid32(), namespaceURI, localPart);
    }

    protected WebServiceTest(String url, String namespaceURI, String localPart) {
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
        synchronized (WebServiceTest.class) {
            int pos = StringUtils.ordinalIndexOf(addressUrl, "/", 3);
            if (pos == -1) {
                pos = addressUrl.length();
            }
            String prefixUrl = addressUrl.substring(0, pos); // http://domain:port
            if (!PUBLISHED.contains(prefixUrl)) {
                JAXWS.publish(addressUrl, SpringContextHolder.getBean(clazz)); // 发布web service
                PUBLISHED.add(addressUrl);
            } else {
                System.out.println("The web service: " + prefixUrl + " are already published.");
            }
        }

        /*JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(clazz);
        factory.setAddress(addressUrl);
        client = (T) factory.create();*/

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
