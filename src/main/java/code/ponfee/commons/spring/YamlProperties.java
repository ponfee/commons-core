package code.ponfee.commons.spring;

import code.ponfee.commons.model.TypedMap;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        Resource resource = new InputStreamResource(inputStream);
        super.putAll(YamlPropertySourceFactory.loadYml(resource));
    }

}
