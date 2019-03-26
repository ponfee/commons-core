package code.ponfee.commons.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.dom4j.util.XMLErrorHandler;
import org.xml.sax.SAXException;

import code.ponfee.commons.io.Closeables;

/**
 * xml工具类
 * @author fupf
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
     *  <span>xml文件</span>
     *  <?xml version="1.0" encoding="UTF-8"?>
     *  <encryptors xmlns="http://code.ponfee/encryptor" 
     *      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     *      xsi:schemaLocation="http://code.ponfee/encryptor encryptor.xsd">
     *    <encryptor>
     *      <!-- require -->
     *      <encryptorId>1</encryptorId>
     *      <!--元素：require；规则：以（classpath:或classpath*:或file:或context:开头，默认以classpath开头 ）-->
     *      <keyStore><![CDATA[classpath:META-INF/encrypt/encryptors/1.pfx]]></keyStore>
     *      <!-- require -->
     *      <storePass>1234</storePass>
     *      <!-- require -->
     *      <keyPass>1234</keyPass>
     *      <!-- optional[可选]，值：[pfx|jks] -->
     *      <storeType>pfx</storeType>
     *      <!-- implied[可选]，不填默认选第1个密钥对 -->
     *      <alias>45e4ea70a3589e96cd670c8e5c8c7be5_28766470-a40c-4c9e-b312-d7a5618db23b</alias>
     *    </encryptor>
     *    ...
     *    <encryptor>...</encryptor>
     *  </encryptors>
     *
     *  <span>xsd文件</span>
     *  <?xml version="1.0" encoding="UTF-8" standalone="no"?>
     *  <schema xmlns="http://www.w3.org/2001/XMLSchema" 
     *      xmlns:tns="http://code.ponfee/encryptor" 
     *      attributeFormDefault="unqualified" 
     *      elementFormDefault="qualified" 
     *      targetNamespace="http://code.ponfee/encryptor">
     *    <element name="encryptors">
     *      <complexType>
     *        <sequence>
     *          <element maxOccurs="unbounded" name="encryptor" type="tns:encryptorType"/>
     *        </sequence>
     *      </complexType>
     *    </element>
     *  
     *    <complexType name="encryptorType">
     *      <sequence>
     *        <element name="encryptorId" type="string"/>
     *        <element name="keyStore" type="tns:resourceType"/>
     *        <element name="storePass" type="string"/>
     *        <element name="keyPass" type="string"/>
     *        <element minOccurs="0" name="storeType" type="tns:storeTypeType"/>
     *        <element minOccurs="0" name="alias" type="string"/>
     *      </sequence>
     *    </complexType>
     *  
     *    <simpleType name="storeTypeType">
     *      <restriction base="string">
     *        <enumeration value="jks"/>
     *        <enumeration value="pfx"/>
     *      </restriction>
     *    </simpleType>
     *  
     *    <simpleType name="resourceType">
     *      <restriction base="string">
     *        <pattern value="(classpath:|classpath\*:|file:(([c-zC-Z]:)(/|\\\\)){0,1}|context:){0,1}[^:\?\|\*]*" />
     *      </restriction>
     *    </simpleType>
     *  </schema>
     * 
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, String>> parse(InputStream xml) {
        try {
            List<Map<String, String>> results = new ArrayList<>();
            Element root = new SAXReader().read(xml).getRootElement();
            Map<String, String> map;
            for (Iterator<Element> seconds = root.elementIterator(); seconds.hasNext();) {
                Element second = seconds.next();
                map = new HashMap<>();
                for (Attribute attr : (List<Attribute>) second.attributes()) {
                    map.put(attr.getName(), attr.getValue()); // 添加二级节点属性
                }
                for (Iterator<Element> thirds = second.elementIterator(); thirds.hasNext();) {
                    Element third = thirds.next(); // 添加三级节点
                    map.put(third.getName().trim(), third.getText().trim());
                }
                results.add(map);
            }
            return results;
        } catch (DocumentException e) {
            throw new IllegalArgumentException("invalid xml data", e);
        } finally {
            Closeables.closeConsole(xml);
        }
    }

    public static List<Map<String, String>> parse(byte[] xml) {
        return parse(new ByteArrayInputStream(xml));
    }

    public static List<Map<String, String>> parse(String xml, String charset) {
        return parse(xml.getBytes(Charset.forName(charset)));
    }

    /**
     * 通过Schema验证xml文件
     */
    public static void validateByXsd(InputStream xsdIn, InputStream xmlIn) {
        try (InputStream xsd = xsdIn; InputStream xml = xmlIn) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            parser.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, JAXPConstants.W3C_XML_SCHEMA);
            //parser.setProperty(JAXPConstants.JAXP_SCHEMA_SOURCE, "file:" + xsdPath);
            parser.setProperty(JAXPConstants.JAXP_SCHEMA_SOURCE, xsd);
            if (!parser.isValidating()) {
                throw new IllegalStateException("invalid xsd defination");
            }

            XMLErrorHandler errorHandler = new XMLErrorHandler();
            SAXValidator validator = new SAXValidator(parser.getXMLReader());
            validator.setErrorHandler(errorHandler);
            validator.validate(new SAXReader().read(xml)); // 校验

            if (errorHandler.getErrors().hasContent()) { // 校验失败
                // 校验失败则打印错误信息
                StringBuilder errors = new StringBuilder(128);
                Set<String> exists = new HashSet<>();
                for (Object obj : errorHandler.getErrors().elements()) {
                    Element e = (Element) obj;
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
        } catch (ParserConfigurationException | SAXException | DocumentException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void validateByXsd(byte[] xsd, byte[] xml) {
        validateByXsd(new ByteArrayInputStream(xsd), new ByteArrayInputStream(xml));
    }

    private static final class JAXPConstants {
        static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
        static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
        static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    }

}
