/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * 包装文件/输入流缓冲读取（decorator）
 *
 * @author Ponfee
 */
public class WrappedBufferedReader extends Reader {

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
        this.buffer = new BufferedReader(
            new InputStreamReader(input, charset), Files.BUFF_SIZE
        );
    }

    @Override
    public void close() {
        Closeables.console(buffer);
        buffer = null;
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
