package code.ponfee.commons.resource;

import java.io.Closeable;
import java.io.InputStream;

import code.ponfee.commons.io.Closeables;

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
        Closeables.closeConsole(stream);
        stream = null;
    }

}
