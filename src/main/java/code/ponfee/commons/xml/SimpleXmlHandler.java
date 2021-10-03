package code.ponfee.commons.xml;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.dom4j.util.XMLErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * xml工具类
 * 
 * @author Ponfee
 */
public class SimpleXmlHandler {

    private static final int MAX_ERROR_SIZE = 500;
    /**
     * <pre>
     * 待解析XML文件 格式必须符合如下规范：
     *   1.最多三级，每级的node名称自定义，一级节点为根节点，不能包含属性，如：encryptors； 
     *   2.二级节点支持节点属性，属性将被视作子节点，二级节点如：encryptor；
     *   3.三级节点不能包含属性，三级节点如：encryptorId；
     *   4.CDATA必须包含在节点中，不能单独出现；
     *
     *  <span>xml文件：</span>
     *  &lt;?xml version="1.0" encoding="UTF-8"?&gt;
     *  &lt;encryptors xmlns="http://code.ponfee/encryptor" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     *              xsi:schemaLocation="http://code.ponfee/encryptor encryptor.xsd"&gt;
     *    &lt;encryptor&gt;
     *      &lt;!-- require --&gt;
     *      &lt;encryptorId&gt;1&lt;/encryptorId&gt;
     *      &lt;!--元素：require；规则：以（classpath:或classpath*:或file:或context:开头，默认以classpath开头 ）--&gt;
     *      &lt;keyStore&gt;&lt;![CDATA[classpath:META-INF/encrypt/encryptors/1.pfx]]&gt;&lt;/keyStore&gt;
     *      &lt;!-- require --&gt;
     *      &lt;storePass&gt;1234&lt;/storePass&gt;
     *      &lt;!-- require --&gt;
     *      &lt;keyPass&gt;1234&lt;/keyPass&gt;
     *      &lt;!-- optional[可选]，值：[pfx|jks] --&gt;
     *      &lt;storeType&gt;pfx&lt;/storeType&gt;
     *      &lt;!-- implied[可选]，不填默认选第1个密钥对 --&gt;
     *      &lt;alias&gt;45e4ea70a3589e96cd670c8e5c8c7be5_28766470-a40c-4c9e-b312-d7a5618db23b&lt;/alias&gt;
     *    &lt;/encryptor&gt;
     *    ...
     *    &lt;encryptor&gt;...&lt;/encryptor&gt;
     *  &lt;/encryptors&gt;
     *
     *  <span>xsd文件：</span>
     *  &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
     *  &lt;schema xmlns="http://www.w3.org/2001/XMLSchema"
     *          xmlns:tns="http://code.ponfee/encryptor"
     *          attributeFormDefault="unqualified"
     *          elementFormDefault="qualified"
     *          targetNamespace="http://code.ponfee/encryptor"&gt;
     *    &lt;element name="encryptors"&gt;
     *      &lt;complexType&gt;
     *        &lt;sequence&gt;
     *          &lt;element maxOccurs="unbounded" name="encryptor" type="tns:encryptorType"/&gt;
     *        &lt;/sequence&gt;
     *      &lt;/complexType&gt;
     *    &lt;/element&gt;
     *    &lt;complexType name="encryptorType"&gt;
     *      &lt;sequence&gt;
     *        &lt;element name="encryptorId" type="string"/&gt;
     *        &lt;element name="keyStore" type="tns:resourceType"/&gt;
     *        &lt;element name="storePass" type="string"/&gt;
     *        &lt;element name="keyPass" type="string"/&gt;
     *        &lt;element minOccurs="0" name="storeType" type="tns:storeTypeType"/&gt;
     *        &lt;element minOccurs="0" name="alias" type="string"/&gt;
     *      &lt;/sequence&gt;
     *    &lt;/complexType&gt;
     *    &lt;simpleType name="storeTypeType"&gt;
     *      &lt;restriction base="string"&gt;
     *        &lt;enumeration value="jks"/&gt;
     *        &lt;enumeration value="pfx"/&gt;
     *      &lt;/restriction&gt;
     *    &lt;/simpleType&gt;
     *    &lt;simpleType name="resourceType"&gt;
     *      &lt;restriction base="string"&gt;
     *        &lt;pattern value="(classpath:|classpath\*:|file:(([c-zC-Z]:)(/|\\\\)){0,1}|context:){0,1}[^:\?\|\*]*" /&gt;
     *      &lt;/restriction&gt;
     *    &lt;/simpleType&gt;
     *  &lt;/schema&gt;
     *
     * </pre>
     */
    public static List<Map<String, String>> parse(InputStream input) {
        try (InputStream xml = input) {
            List<Map<String, String>> results = new LinkedList<>();
            Element root = new SAXReader().read(xml).getRootElement();
            for (Iterator<Element> seconds = root.elementIterator(); seconds.hasNext(); ) {
                Element second = seconds.next();
                Map<String, String> element = new HashMap<>();
                for (Attribute attr : second.attributes()) {
                    element.put(attr.getName(), attr.getValue()); // 添加二级节点属性
                }
                for (Iterator<Element> thirds = second.elementIterator(); thirds.hasNext(); ) {
                    Element third = thirds.next(); // 添加三级节点
                    element.put(third.getName().trim(), third.getText().trim());
                }
                results.add(element);
            }
            return results;
        } catch (DocumentException e) {
            throw new IllegalArgumentException("Invalid xml data.", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, String>> parse(String xml) {
        return parse(new ByteArrayInputStream(xml.getBytes()));
    }

    /**
     * 通过Schema验证xml文件
     */
    public static void validate(InputStream xmlIn, InputStream xsdIn) {
        try (InputStream xml = xmlIn; InputStream xsd = xsdIn) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            parser.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, JAXPConstants.W3C_XML_SCHEMA);
            //parser.setProperty(JAXPConstants.JAXP_SCHEMA_SOURCE, "file:" + xsdPath);
            parser.setProperty(JAXPConstants.JAXP_SCHEMA_SOURCE, xsd);
            if (!parser.isValidating()) {
                throw new IllegalStateException("Invalid xsd definition.");
            }

            XMLErrorHandler errorHandler = new XMLErrorHandler();
            SAXValidator validator = new SAXValidator(parser.getXMLReader());
            validator.setErrorHandler(errorHandler);
            validator.validate(new SAXReader().read(xml)); // 校验

            if (errorHandler.getErrors().hasContent()) { // 校验失败
                // 校验失败则打印错误信息
                StringBuilder errors = new StringBuilder(128);
                Set<String> exists = new HashSet<>();
                for (Element e : errorHandler.getErrors().elements()) {
                    String position = e.attributeValue("line") + "#" + e.attributeValue("column");
                    if (!exists.add(position)) {
                        continue;
                    }
                    errors.append(position).append(":").append(e.getTextTrim()).append("\n");
                    if (errors.length() > MAX_ERROR_SIZE) {
                        break; // break output error
                    }
                }

                if (errors.length() > MAX_ERROR_SIZE) {
                    errors.setLength(MAX_ERROR_SIZE - 3);
                    errors.append("...");
                }
                throw new IllegalStateException(errors.toString());
            }
        } catch (ParserConfigurationException | SAXException | DocumentException e) {
            throw new IllegalStateException("Invalid xml data.", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void validate(String xml, String xsd) {
        validate(new ByteArrayInputStream(xml.getBytes()), new ByteArrayInputStream(xsd.getBytes()));
    }

    private static final class JAXPConstants {
        static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
        static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
        static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    }

}
