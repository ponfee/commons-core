package code.ponfee.commons.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * 包装文件/输入流缓冲读取（decorator）
 * @author Ponfee
 */
public class WrappedBufferedReader extends Reader {

    private InputStream input;
    private InputStreamReader reader;
    private BufferedReader buffer;

    public WrappedBufferedReader(File file) throws FileNotFoundException {
        this(file, Charset.defaultCharset());
    }

    public WrappedBufferedReader(File file, String charset) throws FileNotFoundException {
        this(new FileInputStream(file), Charset.forName(charset));
    }

    public WrappedBufferedReader(File file, Charset charset) throws FileNotFoundException {
        this(new FileInputStream(file), charset);
    }

    public WrappedBufferedReader(InputStream input, Charset charset) {
        super();
        this.input = input;
        this.reader = new InputStreamReader(input, charset);
        this.buffer = new BufferedReader(reader, Files.BUFF_SIZE);
    }

    @Override
    public void close() {
        if (buffer != null) try {
            buffer.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        buffer = null;

        if (reader != null) try {
            reader.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        reader = null;

        if (input != null) try {
            input.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        input = null;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return buffer.read(cbuf, off, len);
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        return buffer.read(target);
    }

    @Override
    public int read() throws IOException {
        return buffer.read();
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return buffer.read(cbuf);
    }

    @Override
    public long skip(long n) throws IOException {
        return buffer.skip(n);
    }

    @Override
    public boolean ready() throws IOException {
        return buffer.ready();
    }

    @Override
    public boolean markSupported() {
        return buffer.markSupported();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        buffer.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        buffer.reset();
    }

    public String readLine() throws IOException {
        return buffer.readLine();
    }

}
