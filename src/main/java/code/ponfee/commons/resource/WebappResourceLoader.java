package code.ponfee.commons.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * webapp资源加载器
 * 
 * @author Ponfee
 */
final class WebappResourceLoader {
    private static final Logger LOG = LoggerFactory.getLogger(WebappResourceLoader.class);

    private ServletContext servletContext;

    WebappResourceLoader() {}

    void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    ServletContext getServletContext() {
        return this.servletContext;
    }

    Resource getResource(String filePath, String encoding) {
        try {
            File f = new File(servletContext.getRealPath(filePath));
            return new Resource(f.getAbsolutePath(), f.getName(), new FileInputStream(f));
        } catch (FileNotFoundException e) {
            LOG.error("file not found [" + filePath + "]", e);
            return null;
        }
    }

    List<Resource> listResources(String directory, String[] extensions, boolean recursive, String encoding) {
        List<Resource> list = new ArrayList<>();
        try {
            File fileDir = new File(servletContext.getRealPath(directory));
            Collection<File> files = FileUtils.listFiles(fileDir, extensions, recursive);
            if (files != null && !files.isEmpty()) {
                for (File f : files) {
                    list.add(new Resource(f.getAbsolutePath(), f.getName(), new FileInputStream(f)));
                }
            }
        } catch (FileNotFoundException e) {
            LOG.error("file not found [" + directory + "]", e);
        }
        return list;
    }

}
