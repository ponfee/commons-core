package code.ponfee.commons.ws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

/**
 * jax-ws工具类
 * 
 * 错误：java.lang.NoSuchMethodError: javax.wsdl.xml.WSDLReader.readWSDL
 *     (Ljavax/wsdl/xml/WSDLLocator;Lorg/w3c/dom/Element;)Ljavax/wsdl/Definition
 * 
 * 原因：jar包冲突问题
 *     [axis:axis:1.4]依赖[axis:axis-wsdl4j:1.5.1]               axis-wsdl4j-1.5.1.jar
 *     [org.apache.cxf:cxf-api:2.7.15]依赖[wsdl4j:wsdl4j:1.6.3]  wsdl4j-1.6.3.jar
 * 
 * 解决：排除依赖axis:axis-wsdl4j
 *     <dependency>
 *         <groupId>axis</groupId>
 *         <artifactId>axis</artifactId>
 *         <version>1.4</version>
 *       <exclusions>
 *         <exclusion>
 *           <groupId>axis</groupId>
 *           <artifactId>axis-wsdl4j</artifactId>
 *         </exclusion>
 *       </exclusions>
 *     </dependency>
 * 
 * @author Ponfee
 */
public class JAXWS {

    /**
     * Returns a JAX-WS client
     * 
     * @param clazz         the webservice interface, as use {@code WebService} annotation
     * @param address       the wsdl url like as http://ip:port/ws/webserviceName?wsdl
     * @param namespaceURI  the targetNamespace of element <b>&lt;wsdl:definitions&gt;</b> attribute
     * @param localPart     the name of element <b>&lt;wsdl:definitions&gt;</b> attribute
     * @return client object can calls rpc
     */
    public static <T> T client(Class<T> clazz, String address, 
                               String namespaceURI, String localPart) {
        return client(clazz, address, new QName(namespaceURI, localPart));
    }

    public static <T> T client(Class<T> clazz, String address, QName qname) {
        try {
            // clazz为接口类
            return Service.create(new URL(address), qname).getPort(clazz);
        } catch (MalformedURLException e) {
            // cannot happened
            throw new IllegalArgumentException("Invalid url: " + address, e);
        }
    }

    /**
     * Server publish the webservice
     * 
     * @param address
     * @param implementor  the webservice interface implements class instance
     */
    public static void publish(String address, Object implementor) {
        Endpoint.publish(address, implementor);
    }

}
