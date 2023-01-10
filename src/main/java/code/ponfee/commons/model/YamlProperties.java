package code.ponfee.commons.model;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.InputStreamResource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Yaml properties
 *
 * @author Ponfee
 */
public class YamlProperties extends Properties implements TypedMap<Object, Object> {

    public YamlProperties(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            loadYaml(inputStream);
        }
    }

    public YamlProperties(InputStream inputStream) {
        loadYaml(inputStream);
    }

    public YamlProperties(String content) {
        loadYaml(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    private void loadYaml(InputStream inputStream) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new InputStreamResource(inputStream));
        factory.afterPropertiesSet();
        super.putAll(factory.getObject());
    }

}
