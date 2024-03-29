/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.xml;

import cn.ponfee.commons.io.Closeables;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * xml读取
 * 
 * @author Ponfee
 */
public final class XmlReader {

    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    static {
        FACTORY.setExpandEntityReferences(false); // XXE漏洞
    }

    private Document document;
    private String root;
    private XPath xpath;

    private XmlReader() {}

    public static XmlReader create(String xml) {
        if (StringUtils.isEmpty(xml)) {
            throw new IllegalArgumentException("xml can't be empty.");
        }
        //xml = xml.replaceAll("(\\r|\\n)", "");
        return create(new ByteArrayInputStream(xml.getBytes()));
    }

    public static XmlReader create(InputStream inputStream) {
        try {
            XmlReader readers = new XmlReader();
            readers.document = FACTORY.newDocumentBuilder().parse(inputStream);
            readers.root = readers.document.getFirstChild().getNodeName();
            readers.xpath = XPathFactory.newInstance().newXPath();
            return readers;
        } catch (Exception e) {
            throw new XmlException("Xmls create fail", e);
        } finally {
            Closeables.console(inputStream);
        }
    }

    /**
     * 获取根节点名称
     * @return
     */
    public String getRoot() {
        return this.root;
    }

    /**
     * 通过xpath取值
     * @param xpathExp 表达式
     * @return
     */
    public String evaluate(String xpathExp) {
        try {
            return this.xpath.evaluate(xpathExp, document);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("xpath evaluate error", e);
        }
    }

    /**
     * 获取节点
     * @param tagName
     * @return
     */
    public Node getNode(String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() <= 0) {
            return null;
        } else {
            return nodes.item(0);
        }
    }

    /**
     * 获取节点列表
     * @param tagName
     * @return
     */
    public NodeList getNodes(String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() <= 0) {
            return null;
        } else {
            return nodes;
        }
    }

    /**
     * 获取某个节点的文本内容，若有多个该节点，只会返回第一个
     * @param tagName 标签名
     * @return 文本内容，或NULL
     */
    public String getNodeText(String tagName) {
        Node node = getNode(tagName);
        return node == null ? null : node.getTextContent();
    }

    /**
     * 获取某个节点的Integer，若有多个该节点，只会返回第一个
     * @param tagName 标签名
     * @return Integer值，或NULL
     */
    public Integer getNodeInt(String tagName) {
        String nodeContent = getNodeText(tagName);
        return nodeContent == null ? null : Integer.valueOf(nodeContent);
    }

    /**
     * 获取某个节点的Long值，若有多个该节点，只会返回第一个
     * @param tagName 标签名
     * @return Long值，或NULL
     */
    public Long getNodeLong(String tagName) {
        String nodeContent = getNodeText(tagName);
        return nodeContent == null ? null : Long.valueOf(nodeContent);
    }

    /**
     * 获取某个节点的Float，若有多个该节点，只会返回第一个
     * @param tagName 标签名
     * @return Float值，或NULL
     */
    public Float getNodeFloat(String tagName) {
        String nodeContent = getNodeText(tagName);
        return nodeContent == null ? null : Float.valueOf(nodeContent);
    }

}
