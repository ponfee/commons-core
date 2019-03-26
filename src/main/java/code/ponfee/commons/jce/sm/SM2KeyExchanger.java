package code.ponfee.commons.jce.sm;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.SecureRandoms;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * SM2 key exchange implementation
 * the final, partA and partB get the same symmetric key
 * 
 * reference the internet code and refactor optimization
 * 
 * @author Ponfee
 */
public class SM2KeyExchanger implements Serializable {

    private static final long serialVersionUID = 8553046425593791291L;
    private static final BigInteger TWO = BigInteger.valueOf(2);

    private BigInteger rA;
    private ECPoint RA;
    private ECPoint V;
    private byte[] key;

    private final ECParameters ecParam;
    private final BigInteger w;
    private final ECPoint publicKey;
    private final BigInteger privateKey;
    private final byte[] Z;

    public SM2KeyExchanger(ECPoint publicKey, BigInteger privateKey) {
        this(null, publicKey, privateKey, ECParameters.SM2_BEST);
    }

    public SM2KeyExchanger(byte[] ida, ECPoint publicKey, BigInteger privateKey) {
        this(ida, publicKey, privateKey, ECParameters.SM2_BEST);
    }

    public SM2KeyExchanger(ECPoint publicKey, BigInteger privateKey, 
                           ECParameters ecParam) {
        this(null, publicKey, privateKey, ecParam);
    }

    public SM2KeyExchanger(byte[] ida, ECPoint publicKey, BigInteger privateKey, 
                           ECParameters ecParam) {
        this.ecParam = ecParam;
        this.w = TWO.pow((int) Math.ceil(ecParam.n.bitLength() * 1.0 / 2) - 1);
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.Z = SM2.calcZ(SM3Digest.getInstance(), ecParam, ida, publicKey);
    }

    /**
     * 密钥协商第一步（甲方）
     * @return TransportEntity
     */
    public TransportEntity step1PartA() {
        rA = SecureRandoms.random(ecParam.n);
        RA = ecParam.pointG.multiply(rA).normalize();
        return new TransportEntity(RA.getEncoded(false), null, Z, publicKey);
    }

    /**
     * 密钥协商第二步（乙方）
     * @param entity1 传输实体
     * @return TransportEntity
     */
    public TransportEntity step2PartB(TransportEntity entity1) {
        BigInteger rB = SecureRandoms.random(ecParam.n);
        ECPoint RB = ecParam.pointG.multiply(rB).normalize();
        this.rA = rB;
        this.RA = RB;

        BigInteger x2 = RB.getXCoord().toBigInteger();
        x2 = w.add(x2.and(w.subtract(BigInteger.ONE)));

        BigInteger tB = privateKey.add(x2.multiply(rB)).mod(ecParam.n);
        ECPoint RA = ecParam.curve.decodePoint(entity1.R).normalize();

        BigInteger x1 = RA.getXCoord().toBigInteger();
        x1 = w.add(x1.and(w.subtract(BigInteger.ONE)));

        ECPoint aPublicKey = ecParam.curve.decodePoint(entity1.K).normalize();
        ECPoint temp = aPublicKey.add(RA.multiply(x1).normalize()).normalize();
        ECPoint V = temp.multiply(ecParam.bcSpec.getH().multiply(tB)).normalize();
        if (V.isInfinity()) {
            throw new IllegalStateException();
        }
        this.V = V;

        byte[] xV = V.getXCoord().toBigInteger().toByteArray();
        byte[] yV = V.getYCoord().toBigInteger().toByteArray();
        key = kdf(Bytes.concat(xV, yV, entity1.Z, this.Z), 16);

        SM3Digest sm3 = SM3Digest.getInstance();
        byte[] data = digest(sm3, xV, entity1.Z, this.Z, RA, RB);

        sm3.update((byte) 0x02);
        sm3.update(yV);
        sm3.update(data);
        byte[] sB = sm3.doFinal();

        return new TransportEntity(RB.getEncoded(false), sB, this.Z, publicKey);
    }

    /**
     * 密钥协商第三步（甲方）
     * @param entity2 传输实体
     * @return TransportEntity
     */
    public TransportEntity step3PartA(TransportEntity entity2) {
        BigInteger x1 = RA.getXCoord().toBigInteger();
        x1 = w.add(x1.and(w.subtract(BigInteger.ONE)));

        BigInteger tA = privateKey.add(x1.multiply(rA)).mod(ecParam.n);
        ECPoint RB = ecParam.curve.decodePoint(entity2.R).normalize();

        BigInteger x2 = RB.getXCoord().toBigInteger();
        x2 = w.add(x2.and(w.subtract(BigInteger.ONE)));

        ECPoint bPublicKey = ecParam.curve.decodePoint(entity2.K).normalize();
        ECPoint temp = bPublicKey.add(RB.multiply(x2).normalize()).normalize();
        ECPoint U = temp.multiply(ecParam.bcSpec.getH().multiply(tA)).normalize();
        if (U.isInfinity()) {
            throw new IllegalStateException();
        }
        this.V = U;

        byte[] xU = U.getXCoord().toBigInteger().toByteArray();
        byte[] yU = U.getYCoord().toBigInteger().toByteArray();
        key = kdf(Bytes.concat(xU, yU, this.Z, entity2.Z), 16);

        SM3Digest sm3 = SM3Digest.getInstance();
        byte[] data = digest(sm3, xU, this.Z, entity2.Z, RA, RB);

        sm3.update((byte) 0x02);
        sm3.update(yU);
        sm3.update(data);
        data = sm3.doFinal();
        if (!Arrays.equals(entity2.S, data)) {
            return null;
        }

        data = digest(sm3, xU, this.Z, entity2.Z, RA, RB);

        sm3.update((byte) 0x03);
        sm3.update(yU);
        sm3.update(data);
        byte[] sA = sm3.doFinal();
        return new TransportEntity(RA.getEncoded(false), sA, this.Z, publicKey);
    }

    /**
     * 密钥协商最后一（第四）步（乙方）
     * @param entity3 传输实体
     */
    public boolean step4PartB(TransportEntity entity3) {
        byte[] xV = V.getXCoord().toBigInteger().toByteArray();
        byte[] yV = V.getYCoord().toBigInteger().toByteArray();
        ECPoint RA = ecParam.curve.decodePoint(entity3.R).normalize();

        SM3Digest sm3 = SM3Digest.getInstance();
        byte[] data = digest(sm3, xV, entity3.Z, this.Z, RA, this.RA);

        sm3.update((byte) 0x03);
        sm3.update(yV);
        sm3.update(data);
        return Arrays.equals(entity3.S, sm3.doFinal());
    }

    
    public byte[] getKey() {
        return key;
    }

    /**
     * 传输实体类
     */
    public static class TransportEntity implements Serializable {
        private static final long serialVersionUID = 3657694935421411649L;

        private final byte[] R; // R点
        private final byte[] S; // 验证S
        private final byte[] Z; // 用户标识
        private final byte[] K; // 公钥

        TransportEntity(byte[] r, byte[] s, byte[] z, ECPoint pKey) {
            this(r, s, z, pKey.getEncoded(false));
        }

        TransportEntity(byte[] r, byte[] s, byte[] z, byte[] publicKey) {
            R = r;
            S = s;
            Z = z;
            K = publicKey;
        }

        public byte[] getR() {
            return R;
        }

        public byte[] getS() {
            return S;
        }

        public byte[] getZ() {
            return Z;
        }

        public byte[] getK() {
            return K;
        }
    }

    /**
     * 连接数据
     * @param x
     * @param z1
     * @param z2
     * @param a
     * @param b
     * @return
     */
    private static byte[] digest(SM3Digest sm3, byte[] x, byte[] z1, 
                                 byte[] z2, ECPoint a, ECPoint b) {
        sm3.reset();
        sm3.update(x);
        sm3.update(z1);
        sm3.update(z2);
        sm3.update(a.getXCoord().toBigInteger().toByteArray());
        sm3.update(a.getYCoord().toBigInteger().toByteArray());
        sm3.update(b.getXCoord().toBigInteger().toByteArray());
        sm3.update(b.getYCoord().toBigInteger().toByteArray());
        return sm3.doFinal();
    }

    /**
     * 密钥派生函数
     * @param Z
     * @param klen 生成klen字节数长度的密钥
     * @return
     */
    private static byte[] kdf(byte[] Z, int klen) {
        int ct = 1, end = (int) Math.ceil(klen * 1.0D / 32.0D);
        SM3Digest sm3 = SM3Digest.getInstance();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 1; i < end; i++) {
            sm3.update(Z);
            byte[] data = sm3.doFinal(Bytes.fromInt(ct));
            baos.write(data, 0, data.length);
            ct++;
        }

        sm3.update(Z);
        sm3.update(Bytes.fromInt(ct));
        byte[] last = sm3.doFinal();
        int len = klen & 0x1F; // klen % 32
        baos.write(last, 0, (len == 0) ? last.length : len);
        return baos.toByteArray();
    }

}
