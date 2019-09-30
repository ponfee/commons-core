package code.ponfee.commons.jce.cert;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Objects;

import javax.security.auth.x500.X500Principal;

/**
 * 解决X500Principal乱码问题
 * 
 * @author Ponfee
 */
public class RepairX500Principal implements Principal {

    private static final byte[][] OID_ARRAY = {
        { 0x55, 0x04, 0x06 }, { 0x55, 0x04, 0x08 }, 
        { 0x55, 0x04, 0x07 }, { 0x55, 0x04, 0x0a }, 
        { 0x55, 0x04, 0x0b }, { 0x55, 0x04, 0x03 },

        { 0x2a, (byte) 0x86, 0x48, (byte) 0x86, 
         (byte) 0xf7, 0x0d, 0x01, 0x09, 0x01 }
    };

    private static final String[] DN_STR = { "C", "ST", "L", "O", "OU", "CN", "E" };

    private final ByteArrayInputStream input;

    public RepairX500Principal(X500Principal principal) {
        Objects.requireNonNull(principal);
        input = new ByteArrayInputStream(principal.getEncoded());
    }

    @Override
    public String getName() {
        if (preLen(0x30) != input.available()) {
            return null;
        }

        byte[] oid = new byte[9];
        int oidType;
        StringBuilder builder = new StringBuilder();
        for (;;) {
            if (preLen(0x31) == 0) {
                break;
            }
            if (preLen(0x30) == 0) {
                break;
            }
            int len = preLen(0x06);
            if (len == 0) {
                break;
            }
            if (len > 9) {
                oidType = -1;
                input.skip(len);
            } else {
                input.read(oid, 0, len);
                for (oidType = DN_STR.length - 1; oidType > -1; oidType--) {
                    for (len = OID_ARRAY[oidType].length - 1; len > -1; len--) {
                        if (oid[len] != OID_ARRAY[oidType][len]) {
                            break;
                        }
                    }
                    if (len < 0) {
                        break;
                    }
                }
            }
            Charset charset = (input.read() == 0x1e) 
                              ? StandardCharsets.UTF_16BE 
                              : StandardCharsets.UTF_8;

            len = preLen(-1);
            if (oidType > -1) {
                byte[] value = new byte[len];
                    input.read(value, 0, value.length);
                    if (builder.length() > 0) {
                        builder.append(',');
                    }
                    builder.append(DN_STR[oidType]).append('=')
                           .append(new String(value, charset));
            } else {
                input.skip(len);
            }
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private int preLen(int tag) {
        int itag;
        if (tag != -1) {
            itag = input.read();
            if (itag != tag) {
                return 0;
            }
        }
        itag = input.read();
        if (itag < 0x80) {
            return itag;
        }
        if (itag == 0x81) {
            return input.read();
        }
        if (itag == 0x82) {
            itag = input.read();
            return (itag << 8) + input.read();
        }
        return 0;
    }

}
