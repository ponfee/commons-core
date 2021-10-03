package code.ponfee.commons.jce.implementation.ecc;

import java.math.BigInteger;

/**
 * The EC point of lie on the curve
 * 
 * @author Ponfee
 */
public class ECPoint {

    private final EllipticCurve curve;
    private final boolean zero;

    private BigInteger x;
    private BigInteger y;

    // fastcache is an array of ECPoints
    private ECPoint[] fastcache = null;

    public void fastCache() {
        if (fastcache == null) {
            // fastcache initialised to 256 EC Points.
            fastcache = new ECPoint[256];
            // First point is null.
            fastcache[0] = new ECPoint(curve);
            for (int i = 1; i < fastcache.length; i++) { // [1, 256)
                // add the point repeatedly (Cumulative sum). P,2P,...
                fastcache[i] = fastcache[i - 1].add(this);
            }
        }
    }

    /** 
     * Constructs a point on an elliptic curve.
     * @param curve The elliptic curve on wich the point is surposed to lie
     * @param x     The x coordinate of the point
     * @param y     The y coordinate of the point
     */
    public ECPoint(EllipticCurve curve, BigInteger x, BigInteger y) {
        this.curve = curve;
        this.x = x;
        this.y = y;
        if (!curve.isOnCurve(this)) {
            throw new IllegalArgumentException("(x,y) is not on this curve!");
        }
        this.zero = false;
    }

    /**
     * Decompresses a compressed point stored in a byte-array into a new ECPoint.
     * @param bytes the array of bytes to be decompressed
     * @param curve the EllipticCurve the decompressed point is supposed to lie on.
     */
    public ECPoint(byte[] bytes, EllipticCurve curve) {
        this.curve = curve;
        if (bytes[0] == 2) {
            this.zero = true;
            return;
        }

        boolean ymt = bytes[0] != 0;
        bytes[0] = 0;
        this.x = new BigInteger(1, bytes);

        this.y = this.x.multiply(this.x).add(curve.getA()).multiply(this.x)
                       .add(curve.getB()).modPow(curve.getPSR2(), curve.getP());
        if (ymt != this.y.testBit(0)) {
            this.y = curve.getP().subtract(this.y);
        }
        this.zero = false;
    }

    /**
     * IMPORTANT this renders the values of x and y to be null! 
     * Use this constructor only to create instances of a Zero class!
     */
    public ECPoint(EllipticCurve e) {
        this.x = this.y = BigInteger.ZERO;
        this.curve = e;
        this.zero = true;
    }

    /**
     * compress the point as byte array data
     * @return byte array data of this point
     */
    public byte[] compress() { // 只导出x坐标，y坐标可由方程计算得到
        byte[] cmp = new byte[this.curve.getPCS()];
        if (this.zero) {
            cmp[0] = 2;
        }
        byte[] xb = this.x.toByteArray();
        System.arraycopy(xb, 0, cmp, this.curve.getPCS() - xb.length, xb.length);
        if (this.y.testBit(0)) {
            cmp[0] = 1;
        }
        return cmp;
    }

    /**
     * 在曲线上计算两点相加的第三个点：point c = point a + point b
     * @param q The point to be added
     * @return the sum of this point on the argument
     */
    public ECPoint add(ECPoint q) {
        if (!isSameCurve(q)) {
            throw new IllegalArgumentException(
                "the q point don't lie on the same elliptic curve.");
        }

        if (this.isZero()) {
            return q;
        } else if (q.isZero()) {
            return this;
        }

        BigInteger x1 = this.x, y1 = this.y;
        BigInteger x2 = q.getX(), y2 = q.getY();

        BigInteger alpha;
        if (x2.compareTo(x1) == 0) {
            if (y2.compareTo(y1) != 0) {
                return new ECPoint(curve); // return a zero point
            } else {
                alpha = ((x1.modPow(EllipticCurve.TWO, curve.getP())).multiply(EllipticCurve.THREE)).add(curve.getA());
                alpha = (alpha.multiply((EllipticCurve.TWO.multiply(y1)).modInverse(curve.getP()))).mod(curve.getP());
            }
        } else {
            BigInteger i = x2.subtract(x1).modInverse(curve.getP());
            alpha = y2.subtract(y1).multiply(i).mod(curve.getP());
        }

        BigInteger x3 = (((alpha.modPow(EllipticCurve.TWO, curve.getP())).subtract(x2)).subtract(x1)).mod(curve.getP());
        BigInteger y3 = ((alpha.multiply(x1.subtract(x3))).subtract(y1)).mod(curve.getP());

        return new ECPoint(curve, x3, y3);
    }

    /**
     * 计算k倍点
     * @param k
     * @return this * k
     */
    public ECPoint multiply(BigInteger k) {
        ECPoint result = this;
        for (int i = k.bitCount() - 1; i > 0; i--) {
            result = result.add(result);
            if (k.testBit(i)) {
                result = result.add(this);
            }
        }
        return result;
    }

    public boolean isZero() {
        return zero;
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    public EllipticCurve getCurve() {
        return curve;
    }

    @Override
    public String toString() {
        return "(" + x.toString() + ", " + y.toString() + ")";
    }

    private boolean isSameCurve(ECPoint p) {
        return this.curve.equals(p.getCurve());
    }

}
