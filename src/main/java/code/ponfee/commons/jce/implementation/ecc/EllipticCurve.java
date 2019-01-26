package code.ponfee.commons.jce.implementation.ecc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import code.ponfee.commons.jce.ECParameters;

/**
 * An implementation of an elliptic curve over a finite field.
 * 
 * @author Ponfee
 */
public class EllipticCurve {

    public static final BigInteger COEFA = new BigInteger("4");
    public static final BigInteger COEFB = new BigInteger("27");
    public static final BigInteger TWO = new BigInteger("2");
    public static final BigInteger THREE = new BigInteger("3");

    private static final int PRIME_SECURITY = 500;

    private final BigInteger a, b, p, n; // n为p的阶
    private final String name; // the curve name
    private final int pcs; // the compressed point size.

    private final ECPoint basePointG; // base point G
    private BigInteger psr2; // (p add one) >> 2

    /** 
     * Constructs an elliptic curve over the finite field of 'mod' elements.
     * The equation of the curve is on the form : y^2 = x^3 + ax + b.
     * @param a the value of 'a' where y^2 = x^3 + ax + b
     * @param b the value of 'b' where y^2 = x^3 + ax + b
     * @param p the elliptic curve point in finite field
     */
    public EllipticCurve(BigInteger a, BigInteger b, BigInteger p) {
        if (!p.isProbablePrime(PRIME_SECURITY)) {
            throw new IllegalArgumentException("the p is not prime");
        }
        if (isSingular(a, b, p)) {
            throw new IllegalArgumentException("is singular");
        }

        this.a = a;
        this.b = b;
        this.p = p;
        this.name = "cust";

        byte[] p0 = p.toByteArray();
        this.pcs = p0[0] == 0 ? p0.length : p0.length + 1;
        this.n = calculateN();
        this.basePointG = calculateBasePointG();
    }

    public EllipticCurve(ECParameters ecp) {
        if (!ecp.p.isProbablePrime(PRIME_SECURITY)) {
            throw new IllegalArgumentException("the p is not prime");
        }
        if (isSingular(ecp.a, ecp.b, ecp.p)) {
            throw new IllegalArgumentException("the ec parameter is singular");
        }

        this.a = ecp.a;
        this.b = ecp.b;
        this.p = ecp.p;
        this.name = ecp.toString();

        byte[] p0 = ecp.p.toByteArray();
        this.pcs = p0[0] == 0 ? p0.length : p0.length + 1;
        this.n = ecp.n;
        this.basePointG = new ECPoint(this, ecp.gx, ecp.gy); // the base point G
        this.basePointG.fastCache();
    }

    public EllipticCurve(DataInputStream input) throws IOException {
        byte[] ab = new byte[input.readInt()];
        input.read(ab);
        this.a = new BigInteger(1, ab);

        byte[] bb = new byte[input.readInt()];
        input.read(bb);
        this.b = new BigInteger(1, bb);

        byte[] pb = new byte[input.readInt()];
        input.read(pb);
        this.p = new BigInteger(1, pb);

        byte[] ob = new byte[input.readInt()];
        input.read(ob);
        this.n = new BigInteger(1, ob);

        byte[] gb = new byte[input.readInt()];
        input.read(gb);
        this.basePointG = new ECPoint(gb, this);

        byte[] ppb = new byte[input.readInt()];
        input.read(ppb);
        this.psr2 = new BigInteger(1, ppb);

        this.pcs = input.readInt();

        this.name = input.readUTF();

        this.basePointG.fastCache();
    }

    public void writeCurve(DataOutputStream output) throws IOException {
        byte[] a0 = a.toByteArray();
        output.writeInt(a0.length);
        output.write(a0);

        byte[] b0 = b.toByteArray();
        output.writeInt(b0.length);
        output.write(b0);

        byte[] p0 = p.toByteArray();
        output.writeInt(p0.length);
        output.write(p0);

        byte[] n0 = n.toByteArray();
        output.writeInt(n0.length);
        output.write(n0);

        byte[] pointG0 = basePointG.compress();
        output.writeInt(pointG0.length);
        output.write(pointG0);

        byte[] ppobf0 = getPSR2().toByteArray();
        output.writeInt(ppobf0.length);
        output.write(ppobf0);

        output.writeInt(pcs);

        output.writeUTF(name);
    }

    public boolean isOnCurve(ECPoint q) {
        if (q.isZero()) {
            return true;
        }

        BigInteger ySquare = (q.getY()).modPow(TWO, p);
        BigInteger xCube = (q.getX()).modPow(THREE, p);
        BigInteger dum = ((xCube.add(a.multiply(q.getX()))).add(b)).mod(p);
        return ySquare.compareTo(dum) == 0;
    }

    public BigInteger getN() {
        return n;
    }

    public ECPoint getZero() {
        return new ECPoint(this);
    }

    public BigInteger getA() {
        return a;
    }

    public BigInteger getB() {
        return b;
    }

    public BigInteger getP() {
        return p;
    }

    public int getPCS() {
        return pcs;
    }

    public ECPoint getBasePointG() {
        return basePointG;
    }

    public BigInteger getPSR2() {
        if (this.psr2 == null) {
            this.psr2 = this.p.add(BigInteger.ONE).shiftRight(2);
        }
        return this.psr2;
    }

    public @Override String toString() {
        if (name == null || name.length() == 0) {
            return "y^2 = x^3 + " + a + "x + " + b + " ( mod " + p + " )";
        } else {
            return name;
        }
    }

    public @Override boolean equals(Object obj) {
        if (!(obj instanceof EllipticCurve)) {
            return false;
        }
        EllipticCurve o = (EllipticCurve) obj;
        return new EqualsBuilder().append(this.a, o.a)
                                  .append(this.b, o.b)
                                  .append(this.p, o.p)
                                  .append(this.n, o.n)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.a)
                                    .append(this.b)
                                    .append(this.p)
                                    .append(this.n)
                                    .hashCode();
    }

    private static boolean isSingular(BigInteger a, BigInteger b, BigInteger p) {
        BigInteger a0 = a.pow(3);
        BigInteger b0 = b.pow(2);
        BigInteger result = a0.multiply(COEFA).add(b0.multiply(COEFB)).mod(p);
        return result.compareTo(BigInteger.ZERO) == 0;
    }

    /**
     * calculate mod n
     * @return
     */
    private BigInteger calculateN() {
        return null; // TODO
    }

    /**
     * calculate base point G
     * @return
     */
    private ECPoint calculateBasePointG() {
        return null; // TODO
        /*BigInteger x = BigInteger.ONE, y, 
        dum = (x.modPow(THREE, this.p).add(a.multiply(x)).add(b)).mod(p); // x^3 + ax + b
        long i = 0;
        do {
            y = BigInteger.valueOf(i++);
        } while (y.modPow(TWO, this.p).compareTo(dum) != 0);
        
        return new ECPoint(this, x, BigInteger.valueOf(i));*/
    }
}
