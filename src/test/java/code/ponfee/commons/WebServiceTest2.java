//package code.ponfee.commons;
//
//import javax.xml.ws.Endpoint;
//
//import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import code.ponfee.commons.json.Jsons;
//import code.ponfee.commons.reflect.GenericUtils;
//import code.ponfee.commons.util.ObjectUtils;
//import code.ponfee.commons.util.SpringContextHolder;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:spring/application-config.xml" })
//public abstract class WebServiceBaseTest2<T> {
//
//    private static volatile boolean isPublished = false;
//
//    private T client;
//    private final Class<T> clazz;
//    private final String addressUrl;
//
//    protected WebServiceBaseTest2() {
//        this("http://localhost:8888/test-ws/" + ObjectUtils.uuid32());
//    }
//
//    protected WebServiceBaseTest2(String url) {
//        clazz = GenericUtils.getActualTypeArgument(this.getClass());
//        addressUrl = url;
//    }
//
//    protected final T client() {
//        return client;
//    }
//
//    @Before
//    @SuppressWarnings("unchecked")
//    public final void setUp() {
//        if (!isPublished) {
//            Endpoint.publish(addressUrl, SpringContextHolder.getBean(clazz)); // 发布web service
//            isPublished = true;
//        }
//
//        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
//        factory.setServiceClass(clazz);
//        factory.setAddress(addressUrl);
//        client = (T) factory.create();
//
//        initiate();
//    }
//
//    @After
//    public final void tearDown() {
//        destory();
//    }
//
//    protected void initiate() {
//        // do no thing
//    }
//
//    protected void destory() {
//        // do no thing
//    }
//
//    public static void consoleJson(Object obj) {
//        try {
//            Thread.sleep(100);
//            System.err.println(Jsons.toJson(obj));
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void console(Object obj) {
//        try {
//            Thread.sleep(100);
//            System.err.println(obj);
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
