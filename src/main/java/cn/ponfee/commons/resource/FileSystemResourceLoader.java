/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.resource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 文件资源加载器
 * 
 * @author Ponfee
 */
final class FileSystemResourceLoader {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemResourceLoader.class);

    Resource getResource(String filePath, String encoding) {
        try {
            File f = new File(filePath);
            return new Resource(f.getAbsolutePath(), f.getName(), new FileInputStream(f));
        } catch (FileNotFoundException e) {
            LOG.error("file not found: " + filePath, e);
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
            LOG.error("file not found: " + directory, e);
        }
        return list;
    }

}
