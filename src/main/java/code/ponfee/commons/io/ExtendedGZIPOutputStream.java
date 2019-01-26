package code.ponfee.commons.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * 扩展自GZIPOutputStream的giz压缩输出流
 * @author Ponfee
 */
public class ExtendedGZIPOutputStream extends GZIPOutputStream {

    public ExtendedGZIPOutputStream(OutputStream out) throws IOException {
        this(out, Deflater.DEFAULT_COMPRESSION);
    }

    public ExtendedGZIPOutputStream(OutputStream out, int level) throws IOException {
        super(out);
        super.def.setLevel(level);
    }
}
