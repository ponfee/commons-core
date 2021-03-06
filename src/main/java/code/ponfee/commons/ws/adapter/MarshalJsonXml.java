package code.ponfee.commons.ws.adapter;

import java.io.Serializable;

/**
 * Wrapped for a bean marshal to xml
 * 
 * @author Ponfee
 */
public class MarshalJsonXml implements Serializable {

    private static final long serialVersionUID = 4570006014299314019L;

    private String type;
    private String data;

    public MarshalJsonXml() {}

    public MarshalJsonXml(String type, String data) {
        super();
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
