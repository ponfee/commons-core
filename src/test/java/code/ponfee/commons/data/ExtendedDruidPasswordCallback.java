package code.ponfee.commons.data;

import java.util.Properties;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.util.DruidPasswordCallback;

/**
 * Druid数据源数据库密码解密
 * 
 * @author Ponfee
 */
public class ExtendedDruidPasswordCallback extends DruidPasswordCallback {

    private static final long serialVersionUID = -4596359636208162436L;

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        super.setPassword(decrypt((String) properties.get("password")).toCharArray());
        //super.setUrl(decrypt((String) properties.get("url")));
    }

    private String decrypt(String data) {
        try {
            return ConfigTools.decrypt("public-key-text", data);
        } catch (Exception e) {
            return data;
        }
    }
}
