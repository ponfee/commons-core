package code.ponfee.commons.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 预先第一次读取InputStream的数据用来判断文件流类型，文件编码等用途
 * 
 * @author Ponfee
 */
public class BeforeReadInputStream extends InputStream {

    private final InputStream input;
    private final byte[] beforeReadByteArray;
    private final int limit;
    private int offset;

    public BeforeReadInputStream(InputStream input, int maxCount) throws IOException {
        this.input = input;

        if (input.available() <= maxCount) {
            this.beforeReadByteArray = new byte[input.available()];
        } else {
            this.beforeReadByteArray = new byte[maxCount];
        }
        input.read(this.beforeReadByteArray);
        this.offset = 0;
        this.limit = this.beforeReadByteArray.length;
    }

    /**
     * 读取下一个可用的字节数据，如果到达流的末尾而没有字节可用，则返回值-1
     */
    @Override
    public int read() throws IOException {
        if (this.offset < this.limit) {
            return beforeReadByteArray[offset];
        } else {
            return this.input.read();
        }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int remain = this.limit - this.offset;
        if (remain > 0) {
            int count = len - off;
            if (remain >= count) {
                System.arraycopy(this.beforeReadByteArray, this.offset, buf, off, len);
                this.offset += count;
                return count;
            } else {
                System.arraycopy(this.beforeReadByteArray, this.offset, buf, off, remain);
                int cnt = this.input.read(buf, off + remain, len - remain);
                this.offset = this.limit;
                return cnt == -1 ? remain : remain + cnt;
            }
        } else {
            return this.input.read(buf, off, len);
        }
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    public byte[] getArray() {
        return this.beforeReadByteArray;
    }

    // --------------------------------------------------------------
    @Override
    public long skip(long n) throws IOException {
        return this.input.skip(n);
    }

    @Override
    public int available() throws IOException {
        return this.input.available();
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.input.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.input.reset();
    }

    @Override
    public boolean markSupported() {
        return this.input.markSupported();
    }

}
