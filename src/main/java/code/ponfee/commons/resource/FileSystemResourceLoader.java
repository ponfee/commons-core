package code.ponfee.commons.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件资源加载器
 * @author fupf
 */
final class FileSystemResourceLoader {

    private static Logger logger = LoggerFactory.getLogger(FileSystemResourceLoader.class);

    Resource getResource(String filePath, String encoding) {
        try {
            File f = new File(filePath);
            return new Resource(f.getAbsolutePath(), f.getName(), new FileInputStream(f));
        } catch (FileNotFoundException e) {
            logger.error("file not found: " + filePath, e);
            return null;
        }
    }

    List<Resource> listResources(String directory, String[] extensions, boolean recursive) {
        List<Resource> list = new ArrayList<>();
        try {
            File fileDir = new File(directory);
            Collection<File> files = FileUtils.listFiles(fileDir, extensions, recursive);
            if (files != null && !files.isEmpty()) {
                for (File f : files) {
                    list.add(new Resource(f.getAbsolutePath(), f.getName(), new FileInputStream(f)));
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("file not found: " + directory, e);
        }
        return list;
    }

}
