package code.ponfee.commons.resource;

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

/**
 * <pre>
 *   用法：
 *   new ResourceScanner("∕**∕").scan4text("*.properties")
 *   new ResourceScanner("∕**∕").scan4text("*.class");
 *   new ResourceScanner("∕").scan4text("*.xml");
 *   new ResourceScanner("∕**∕").scan4text("*.xml")
 *   
 *   new ResourceScanner("code.ponfee").scan4class();
 *   new ResourceScanner("code.ponfee").scan4class(new Class[] { Service.class });
 * </pre>
 * 
 * 资源扫描
 * @author fupf
 */
public class ResourceScanner {

    private static Logger logger = LoggerFactory.getLogger(ResourceScanner.class);

    private final List<String> scanPaths = new LinkedList<>();

    /**
     * @param paths 扫描路径
     */
    public ResourceScanner(String... paths) {
        if (paths == null || paths.length == 0) {
            paths = new String[] { "*" };
        }

        Collections.addAll(this.scanPaths, paths);
    }

    /**
     * 类扫描
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<Class<?>> scan4class() {
        return scan4class(new Class[0]);
    }

    /**
     * 类扫描
     * @param annotations 包含指定注解的类
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<Class<?>> scan4class(Class<? extends Annotation>... annotations) {
        Set<Class<?>> classSet = new HashSet<>();
        if (this.scanPaths.isEmpty()) {
            return classSet;
        }

        List<TypeFilter> typeFilters = new LinkedList<>();
        if (annotations != null) {
            for (Class<? extends Annotation> annotation : annotations) {
                typeFilters.add(new AnnotationTypeFilter(annotation, false));
            }
        }

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            for (String packageName : this.scanPaths) {
                packageName = packageName.replace('.', '/');
                Resource[] resources = resolver.getResources(CLASSPATH_ALL_URL_PREFIX + packageName + "/**/*.class");
                MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
                for (Resource resource : resources) {
                    if (!resource.isReadable()) {
                        continue;
                    }

                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    if (!this.matchesFilter(reader, typeFilters, readerFactory)) {
                        continue;
                    }

                    try {
                        classSet.add(Class.forName(reader.getClassMetadata().getClassName()));
                    } catch (Throwable e) {
                        logger.error("load class error", e);
                    }
                }
            }

            return classSet;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 流扫描
     * @return Map<String, byte[]>
     */
    public Map<String, byte[]> scan4binary() {
        return scan4binary("*");
    }

    /**
     * 流扫描
     * @param wildcard 通配符
     * @return
     */
    public Map<String, byte[]> scan4binary(String wildcard) {
        if (wildcard == null) {
            wildcard = "*";
        }

        Map<String, byte[]> result = new HashMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            for (String path : this.scanPaths) {
                Resource[] resources = resolver.getResources(CLASSPATH_ALL_URL_PREFIX + path + wildcard);
                for (Resource resource : resources) {
                    if (!resource.isReadable()) {
                        continue;
                    }
                    try (InputStream in = resource.getInputStream()) {
                        result.put(resource.getFilename(), IOUtils.toByteArray(in));
                    } catch (IOException e) {
                        logger.error("scan binary error", e);
                    }
                }
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文本扫描
     * @return
     */
    public Map<String, String> scan4text() {
        return scan4text(null, Charset.defaultCharset());
    }

    /**
     * 文本扫描
     * @param wildcard
     * @return
     */
    public Map<String, String> scan4text(String wildcard) {
        return scan4text(wildcard, Charset.defaultCharset());
    }

    /**
     * 文本扫描
     * @param wildcard
     * @param charset
     * @return
     */
    public Map<String, String> scan4text(String wildcard, Charset charset) {
        Map<String, String> result = new HashMap<>();
        for (Entry<String, byte[]> entry : scan4binary(wildcard).entrySet()) {
            result.put(entry.getKey(), new String(entry.getValue(), charset));
        }
        return result;
    }

    /**
     * 检查当前扫描到的Bean含有任何一个指定的注解标记
     * @param reader
     * @param typeFilters
     * @param factory
     * @return
     * @throws IOException
     */
    private boolean matchesFilter(MetadataReader reader, List<TypeFilter> typeFilters,
                                  MetadataReaderFactory factory) throws IOException {
        if (typeFilters.isEmpty()) {
            return true;
        }

        for (TypeFilter filter : typeFilters) {
            if (filter.match(reader, factory)) {
                return true;
            }
        }
        return false;
    }

}
