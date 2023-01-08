/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * xml构建
 * 
 * @author Ponfee
 */
public final class XmlWriter {
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private final List<E<?>> elements = new ArrayList<>();

    private XmlWriter() {}

    public static XmlWriter create() {
        return new XmlWriter();
    }

    public XmlWriter element(String name, String text) {
        elements.add(new TextE(name, text));
        return this;
    }

    public XmlWriter element(String name, Number number) {
        elements.add(new NumberE(name, number));
        return this;
    }

    public XmlWriter element(String parentName, String childName, String childText) {
        return element(parentName, new TextE(childName, childText));
    }

    public XmlWriter element(String parentName, String childName, Number childNumber) {
        return element(parentName, new NumberE(childName, childNumber));
    }

    /**
     * 构建包含多个子元素的元素
     * @param parentName 父元素名
     * @param childPairs
     * @return this
     */
    public XmlWriter element(String parentName, Object... childPairs) {
        return element(parentName, newElement(childPairs));
    }

    public XmlWriter element(String parentName, E<?> child) {
        return element(parentName, Collections.singletonList(child));
    }

    public XmlWriter element(String parentName, List<E<?>> children) {
        elements.add(new NodeE(parentName, children));
        return this;
    }

    public String build() {
        return build("xml");
    }

    public String build(String root) {
        StringBuilder xml = new StringBuilder(XML_DECLARATION)
                         .append("<").append(root).append(">");
        for (E<?> e : elements) {
            xml.append(e.render());
        }
        return xml.append("</").append(root).append(">").toString();
    }

    /**
     * 创建多个元素的节点列表
     * @param childPairs childName1, childValue1, childName2, childValu2, ...，长度必须为2的倍数
     * @return
     */
    public static List<E<?>> newElement(Object... childPairs) {
        if ((childPairs.length & 0x01) == 1) {
            throw new XmlException("args Object array must be pair");
        }

        List<E<?>> nodes = new ArrayList<>();
        for (int i = 0; i < childPairs.length; i = i + 2) {
            nodes.add(newElement((String) childPairs[i], childPairs[i + 1]));
        }
        return nodes;
    }

    /**
     * 创建元素
     * @param name  元素名
     * @param value 元素值
     * @return
     */
    public static E<?> newElement(String name, Object value) {
        if (value instanceof Number) {
            return new NumberE(name, (Number) value);
        } else if (value instanceof E<?>) {
            return new NodeE(name, Collections.singletonList((NodeE) value));
        } else {
            return new TextE(name, Objects.toString(value, null));
        }
    }

    /**
     * 元素抽象类
     * @param <T>
     */
    public static abstract class E<T> {
        protected final String name;
        protected final T value;

        public E(String name, T value) {
            if (name == null) {
                throw new IllegalArgumentException("element name cannot be null.");
            }
            this.name = name;
            this.value = value;
        }

        private String render() {
            StringBuilder content = new StringBuilder("<").append(name).append(">");
            if (value != null) {
                content.append(value());
            }
            return content.append("</").append(name).append(">").toString();
        }

        protected abstract String value();
    }

    /**
     * 文本元素类
     */
    public static class TextE extends E<String> {
        public TextE(String name, String content) {
            super(name, content);
        }

        @Override
        protected String value() {
            return new StringBuilder("<![CDATA[").append(value).append("]]>").toString();
        }
    }

    /**
     * 数值元素类
     */
    public static class NumberE extends E<Number> {
        public NumberE(String name, Number value) {
            super(name, value);
        }

        @Override
        protected String value() {
            return value.toString();
        }
    }

    /**
     * 节点元素类
     */
    public static class NodeE extends E<List<E<?>>> {
        public NodeE(String name, List<E<?>> nodes) {
            super(name, nodes);
        }

        @Override
        protected String value() {
            StringBuilder content = new StringBuilder();
            for (E<?> e : value) {
                if (e != null) {
                    content.append(e.render());
                }
            }
            return content.toString();
        }
    }

}
