package test.jce.ecc0;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import cn.ponfee.commons.jce.implementation.Cryptor;
import cn.ponfee.commons.jce.implementation.Key;

public class CryptoInputStream extends InputStream {

    private DataInputStream in;
    private Cryptor cs;
    private Key key;
    private byte[] buffer;
    private int top;
    private int blocksize;

    public CryptoInputStream(InputStream in, Cryptor cs, Key key) {
        this.in = new DataInputStream(in);
        this.cs = cs;
        this.key = key;
        buffer = new byte[0];
    }

    public @Override int read() throws IOException {
        if (top == buffer.length) {
            try {
                blocksize = in.readInt();
            } catch (EOFException e) {
                return -1;
            }
            byte[] cipher = new byte[blocksize];
            in.read(cipher);
            buffer = cs.decrypt(cipher, key);
            top = 0;
        }
        top++;
        return buffer[top - 1];
    }

    public @Override void close() throws IOException {
        in.close();
    }
}
