package code.ponfee.commons.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 预先第一次读取InputStream的数据用来判断文件类型，文件编码等用途
 * 
 * @author Ponfee
 */
public class PrereadInputStream extends InputStream {

    private final InputStream input;
    private final byte[] heads;
    private final int limit;
    private int offset;

    public PrereadInputStream(InputStream input, int maxCount) throws IOException {
        this.input = input;
        this.heads = Files.readByteArray(input, maxCount);
        this.offset = 0;
        this.limit = this.heads.length;
    }

    /**
     * 读取下一个可用的字节数据，如果到达流的末尾而没有字节可用，则返回值-1
     */
    @Override
    public int read() throws IOException {
        if (this.offset < this.limit) {
            return this.heads[this.offset];
        } else {
            return this.input.read();
        }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int remaining = this.limit - this.offset;
        if (remaining > 0) {
            int count = len - off;
            if (remaining >= count) {
                System.arraycopy(this.heads, this.offset, buf, off, len);
                this.offset += count;
                return count;
            } else {
                System.arraycopy(this.heads, this.offset, buf, off, remaining);
                int cnt = this.input.read(buf, off + remaining, len - remaining);
                this.offset = this.limit;
                return cnt == -1 ? remaining : remaining + cnt;
            }
        } else {
            return this.input.read(buf, off, len);
        }
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    public byte[] heads() {
        return this.heads;
    }

    @Override
    public long skip(long n) throws IOException {
        int remaining = this.limit - this.offset;
        if (remaining <= 0) {
            return this.input.skip(n);
        } else if (remaining > n) {
            this.offset += n;
            return n;
        } else {
            this.offset = this.limit;
            return this.input.skip(n - remaining) + remaining;
        }
    }

    @Override
    public int available() throws IOException {
        return this.input.available() + this.limit - this.offset;
    }

    @Override
    public void close() throws IOException {
        this.input.close();
        this.offset = this.limit;
    }

    @Override @Deprecated
    public synchronized void mark(int readLimit) {
        throw new UnsupportedOperationException("mark/reset not supported");
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

}
