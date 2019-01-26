package code.ponfee.commons.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * xml和map相互转换工具
 * @author fupf
 */
public final class XmlMap extends LinkedHashMap<String, String> {

    private static final long serialVersionUID = 2775335692799838871L;

    private String root;

    public XmlMap(Map<String, String> map) {
        this(map, "xml");
    }

    public XmlMap(Map<String, String> map, String root) {
        this.root = root;
        super.putAll(map);
    }

    public XmlMap(String xml) {
        Map<String, String> map;
        if (StringUtils.isEmpty(xml)) {
            map = Collections.emptyMap();
        } else {
            map = read(XmlReader.create(xml));
        }
        super.putAll(map);
    }

    public XmlMap(XmlReader reader) {
        super.putAll(read(reader));
    }

    /**
     * 返回Map
     * @return
     */
    public Map<String, String> toMap() {
        return this;
    }

    /**
     * 返回Xml
     * @return
     */
    public String toXml() {
        XmlWriter writers = XmlWriter.create();
        for (Map.Entry<String, String> param : this.entrySet()) {
            if (!StringUtils.isEmpty(param.getValue())) {
                writers.element(param.getKey(), param.getValue());
            }
        }
        return writers.build(this.root);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * XML为Map(仅支持2级)
     * @param reader xmlReader
     * @return Map对象
     */
    private Map<String, String> read(XmlReader reader) {
        this.root = reader.getRoot();
        Node rootNode = reader.getNode(this.root);
        NodeList children;
        if (rootNode == null || (children = rootNode.getChildNodes()).getLength() == 0) {
            return Collections.emptyMap();
        }

        Map<String, String> data = new HashMap<>(children.getLength());
        Node n;
        for (int i = 0; i < children.getLength(); i++) {
            n = children.item(i);
            if (Node.TEXT_NODE != n.getNodeType()) {
                data.put(n.getNodeName(), n.getTextContent());
            }
        }
        return data;
    }

}
