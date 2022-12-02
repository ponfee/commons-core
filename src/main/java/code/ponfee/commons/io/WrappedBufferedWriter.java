package code.ponfee.commons.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * 包装文件/输入流缓冲写入（decorator）
 *
 * @author Ponfee
 */
public class WrappedBufferedWriter extends Writer {

    private OutputStream output;
    private BufferedWriter buffer;

    public WrappedBufferedWriter(File file) throws FileNotFoundException {
        this(file, Charset.defaultCharset());
    }

    public WrappedBufferedWriter(File file, String charset) throws FileNotFoundException {
        this(new FileOutputStream(file), Charset.forName(charset));
    }

    public WrappedBufferedWriter(File file, Charset charset) throws FileNotFoundException {
        this(new FileOutputStream(file), charset);
    }

    public WrappedBufferedWriter(OutputStream output, Charset charset) {
        this.output = output;
        this.buffer = new BufferedWriter(
            new OutputStreamWriter(output, charset), Files.BUFF_SIZE
        );
    }

    @Override
    public void write(String str) throws IOException {
        buffer.write(str);
    }

    @Override
    public void write(int c) throws IOException {
        buffer.write(c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        buffer.write(cbuf);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        buffer.write(str, off, len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        return buffer.append(csq);
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        return buffer.append(csq, start, end);
    }

    @Override
    public Writer append(char c) throws IOException {
        return buffer.append(c);
    }

    @Override
    public void flush() throws IOException {
        buffer.flush();
    }

    @Override
    public void close() {
        Closeables.console(buffer);
        buffer = null;
        output = null;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        buffer.write(cbuf, off, len);
    }

    public void newLine() throws IOException {
        buffer.newLine();
    }

    public void write(byte[] bytes) throws IOException {
        output.write(bytes);
    }

    public void writeln() throws IOException {
        newLine();
    }

    public void writeln(String str) throws IOException {
        synchronized (super.lock) {
            buffer.write(str);
            writeln();
        }
    }

}
