package code.ponfee.commons.jce.implementation.ecc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import code.ponfee.commons.jce.implementation.Key;
import code.ponfee.commons.util.SecureRandoms;

/**
 * This is Elliptic Curve key
 * @author Ponfee
 */
public class ECKey implements Key {

    protected boolean secret; // 是否是私钥
    protected BigInteger dk; // decrypt key
    protected ECPoint beta; // the public key of ECPoint
    protected final EllipticCurve curve; // the Elliptic cCurve

    /**
     * ECKey generates a random secret key (contains also the public key)
     * @param ec
     */
    public ECKey(EllipticCurve ec) {
        this.curve = ec;
        this.secret = true;

        // dk is a random num.
        if (curve.getN() != null) {
            this.dk = SecureRandoms.random(this.curve.getN());
        } else {
            this.dk = SecureRandoms.random(ec.getP().bitLength() + 17);
        }

        // beta = pointG * dk
        this.beta = this.curve.getBasePointG().multiply(this.dk); // dk倍点beta
        this.beta.fastCache();
    }

    @Override
    public String toString() {
        String str = "";
        if (secret) {
            str = "Private key: " + dk + ", ";
        }
        return str + "Public key: " + beta + ", Curve: " + curve;
    }

    @Override
    public boolean isPublic() {
        return !secret;
    }

    @Override
    public void writeKey(OutputStream out) throws IOException {
        DataOutputStream output = new DataOutputStream(out);
        this.curve.writeCurve(output);
        output.writeBoolean(this.secret);
        if (this.secret) {
            byte[] dk0 = this.dk.toByteArray();
            output.writeInt(dk0.length);
            output.write(dk0);
        }
        byte[] beta0 = this.beta.compress();
        output.writeInt(beta0.length);
        output.write(beta0);
    }

    @Override
    public Key readKey(InputStream in) throws IOException {
        DataInputStream input = new DataInputStream(in);
        ECKey key = new ECKey(new EllipticCurve(input));
        key.secret = input.readBoolean();
        if (key.secret) {
            byte[] dk0 = new byte[input.readInt()];
            input.read(dk0);
            key.dk = new BigInteger(1, dk0);
        }
        byte[] beta0 = new byte[input.readInt()];
        input.read(beta0);
        key.beta = new ECPoint(beta0, key.curve);
        return key;
    }

    /**
     * get the public key
     */
    @Override
    public Key getPublic() {
        if (!this.secret) {
            return this;
        }

        ECKey pubKey = new ECKey(curve);
        pubKey.beta = beta;
        pubKey.dk = BigInteger.ZERO;
        pubKey.secret = false;
        return pubKey;
    }

}
