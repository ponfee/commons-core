/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.resource;

import cn.ponfee.commons.io.Closeables;

import java.io.Closeable;
import java.io.InputStream;

/**
 * 资源类
 * 
 * @author Ponfee
 */
public class Resource implements Closeable {

    private final String filePath;
    private final String fileName;
    private InputStream stream;

    public Resource(String filePath, String fileName, InputStream stream) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.stream = stream;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getStream() {
        return stream;
    }

    @Override
    public String toString() {
        return "Resource [filePath=" + filePath + ", fileName=" + fileName + ", stream=" + stream + "]";
    }

    @Override
    public void close() {
        Closeables.console(stream);
        stream = null;
    }

}
