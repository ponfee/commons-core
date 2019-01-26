package test.jce.ecc0;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import code.ponfee.commons.jce.implementation.Cryptor;
import code.ponfee.commons.jce.implementation.Key;

public class CryptoOutputStream extends OutputStream {
    private DataOutputStream out;
    private Cryptor cs;
    private Key key;
    private byte[] buffer;
    private int top;

    public CryptoOutputStream(OutputStream out, Cryptor cs, Key key) {
        this.out = new DataOutputStream(out);
        this.cs = cs;
        this.key = key;
        buffer = new byte[64];
    }

    private void writeOut() throws IOException {
        if (top == 0) return;
        byte[] cipher = cs.encrypt(buffer, top, key);
        out.writeInt(cipher.length);
        out.write(cipher);
        top = 0;
    }

    public @Override void write(int b) throws IOException {
        buffer[top] = (byte) b;
        top++;
        if (top == buffer.length) writeOut();
    }

    public @Override void flush() throws IOException {
        writeOut();
        out.flush();
    }

    public @Override void close() throws IOException {
        out.close();
    }
}
