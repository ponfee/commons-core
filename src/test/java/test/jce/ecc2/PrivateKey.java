package test.jce.ecc2;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static test.jce.ecc2.BaseConvert.byteArrayToBaseEncodedString;
import static test.jce.ecc2.Utils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECPoint;

final public class PrivateKey {
    private final BigInteger d;
    private final ECDomainParameters curveParameters;
    private final PublicKey publicKey;

    /**
     * Construct a PrivateKey
     *
     * @param curveParameters The parameters of the elliptic curve to be used
     * @param d               The coefficient to be used as a private key
     * @throws SecurityException Thrown if the specified private key coefficient is not a valid value permitted by the elliptic curve specified by curveParameters
     */
    public PrivateKey(ECDomainParameters curveParameters, BigInteger d) throws SecurityException {
        if (d.compareTo(BigInteger.ZERO) != 1)
            throw new SecurityException("Private key must be positive.");
        if (curveParameters.getN().compareTo(d) != 1)
            throw new SecurityException("Private key cannot be larger than the curve modulus.");
        this.d = d;
        this.curveParameters = curveParameters;
        this.publicKey = new PublicKey(curveParameters, curveParameters.getG().multiply(d).normalize());
    }

    /**
     * Parse a PrivateKey from a byte array
     *
     * @param curveParameters The parameters of the elliptic curve to be used
     * @param bytes           The byte array to be parsed
     * @return The private key corresponding to the specified bytes
     */
    public static PrivateKey fromByteArray(ECDomainParameters curveParameters, byte[] bytes) {
        return new PrivateKey(curveParameters, new BigInteger(1, bytes));
    }

    /**
     * Parse a PrivateKey from an encoded string
     *
     * @param curveParameters The parameters of the elliptic curve to be used
     * @param string          The encoded string, which is to be parsed into bytes specified by the radix
     * @param radix           The radix of the encoded string
     * @return The private key corresponding to the specified bytes
     * @throws UnsupportedBaseException Thrown if the specified radix is not a supported base for encoding strings
     */
    public static PrivateKey fromString(
            ECDomainParameters curveParameters,
            String string,
            int radix) throws UnsupportedBaseException {
        if (radix == 10)
            return new PrivateKey(curveParameters, new BigInteger(string, 10));
        return fromByteArray(curveParameters, BaseConvert.baseEncodedStringToByteArray(string, radix));
    }

    /**
     * Convert the coefficient of the private key into a byte array
     *
     * @return A byte array representing the coefficient of the private key
     */
    public byte[] toByteArray() {
        final byte[] data = d.toByteArray();
        return Arrays.copyOfRange(data, countLeadingZeroBytes(data), data.length);
    }

    /**
     * Convert the coefficient of the private key into a string encoding a byte array using the specified radix
     *
     * @param radix The radix to use for encoding the coefficient's representative byte array as a string
     * @return A string encoding a byte array representing the coefficient of the private key
     * @throws UnsupportedBaseException Thrown if the specified radix is not a supported base for encoding strings
     */
    public String toString(int radix) throws UnsupportedBaseException {
        if (radix == 10)
            return d.toString(10);
        return byteArrayToBaseEncodedString(toByteArray(), radix);
    }

    /**
     * Convert the coefficient of the private key into a hexadecimal string
     *
     * @return A hexadecimal string encoding a byte array representing the coefficient of the private key
     */
    @Override
    public String toString() {
        return printHexBinary(toByteArray()).toLowerCase();
    }

    /**
     * Get the public key for this private key
     *
     * @return The public key that corresponds to this private key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Create a new RFC 6979 deterministic nonce generator for deterministic ECDSA signatures
     *
     * @param hash       The hash of value to be signed
     * @param hashDigest A hashing function to use in signing
     * @return A cryptographic deterministic PRNG where the first returned value is compliant with RFC 6979
     * @throws SecurityException Thrown if a fresh hash digest cannot be generated from the one hash handed as an argument
     */
    private HMacDSAKCalculator deterministicKGenerator(
            byte[] hash,
            GeneralDigest hashDigest) throws SecurityException {
        final GeneralDigest freshHashDigest = freshDigestFromDigest(hashDigest);
        final HMacDSAKCalculator generator = new HMacDSAKCalculator(freshHashDigest);
        generator.init(curveParameters.getN(), d, hash);
        return generator;
    }

    /**
     * Compute a recovery byte for a compressed ECDSA signature given R and S parameters
     *
     * @param kp The elliptic curve point computed as part of the ECDSA algorithm that must be recovered
     * @param r  The R value of the ECDSA signature
     * @param s  The S value of the ECDSA signature
     * @return The recovery byte, following the convention in BitCoin
     */
    private byte computeRecoveryByte(ECPoint kp, BigInteger r, BigInteger s, boolean canonical) {
        final BigInteger n = curveParameters.getN();
        final boolean bigR = r.compareTo(n) >= 0;
        final boolean bigS = canonical && s.add(s).compareTo(n) >= 0;
        final boolean yOdd = kp.getYCoord().toBigInteger().testBit(0);
        return (byte) (0x1B + ((bigS != yOdd) ? 1 : 0) + (bigR ? 2 : 0));
    }

    private static SecureRandom rng = new SecureRandom();

    private static class TimeStampAndNonce {
        final long timeStamp;
        final long nonce;

        private TimeStampAndNonce() {
            this.timeStamp = System.currentTimeMillis();
            this.nonce = rng.nextLong();
        }
    }

    /**
     * A configuration that is read when constructing ECDSA signatures
     */
    public static class SignatureConfig {
        final boolean recover;
        final TimeStampAndNonce timeStampAndNonce;
        final boolean canonical;
        final GeneralDigest rfc6979Digest;
        final GeneralDigest messageDigest;

        public SignatureConfig(
                boolean recover,
                boolean timeStampAndNonce,
                boolean canonical,
                GeneralDigest rfc6979Digest,
                GeneralDigest messageDigest) throws IllegalArgumentException {
            if (timeStampAndNonce && !recover) {
                throw new IllegalArgumentException(
                        "Cannot configure signatures to include a timestamp and nonce without a recovery byte");
            }
            this.recover = recover;
            this.canonical = canonical;
            this.timeStampAndNonce = timeStampAndNonce ? new TimeStampAndNonce() : null;
            this.rfc6979Digest = rfc6979Digest;
            this.messageDigest = messageDigest;
        }
    }

    /**
     * A builder for configurations for ECDSA signatures
     */
    public static class SignatureConfigBuilder {
        private boolean recover = true;
        private boolean timeStampAndNonce = true;
        private boolean canonical = true;
        private GeneralDigest rfc6979Digest = new SHA256Digest();
        private GeneralDigest messageDigest = new SHA256Digest();

        public SignatureConfigBuilder() {
        }

        public SignatureConfigBuilder setRecover(boolean recover) {
            this.recover = recover;
            if (!recover)
                this.timeStampAndNonce = false;
            return this;
        }

        public SignatureConfigBuilder setTimeStampAndNonce(boolean timeStampAndNonce) {
            if (timeStampAndNonce && !recover)
                throw new IllegalArgumentException(
                        "Cannot configure signatures to include a timestamp and nonce without a recovery byte");
            this.timeStampAndNonce = timeStampAndNonce;
            return this;
        }

        public SignatureConfigBuilder setCanonical(boolean canonical) {
            this.canonical = canonical;
            return this;
        }

        public SignatureConfigBuilder setRfc6979Digest(GeneralDigest rfc6979Digest) {
            this.rfc6979Digest = rfc6979Digest;
            return this;
        }

        public SignatureConfigBuilder setMessageDigest(GeneralDigest messageDigest) {
            this.messageDigest = messageDigest;
            return this;
        }

        public SignatureConfig build() {
            return new SignatureConfig(recover, timeStampAndNonce, canonical, rfc6979Digest, messageDigest);
        }
    }

    /**
     * A default configuration for ECDSA signatures
     */
    public static SignatureConfig getDefaultSignatureConfig() {
        return new SignatureConfigBuilder().build();
    }

    private byte[] signHash(byte[] hash, SignatureConfig config)
            throws SecurityException {
        if ((hash.length << 3) > curveParameters.getN().bitLength())
            throw new SecurityException("Hash must not have more bytes than the curve specifies");
        final HMacDSAKCalculator rng = deterministicKGenerator(hash, config.rfc6979Digest);
        final BigInteger z = new BigInteger(1, hash);
        while (true) {
            final BigInteger k = rng.nextK();
            final BigInteger n = curveParameters.getN();
            final ECPoint kp = curveParameters.getG().multiply(k).normalize();
            final BigInteger r = kp.getXCoord().toBigInteger().mod(n);
            if (r.equals(BigInteger.ZERO)) continue;
            final BigInteger s_ = k.modInverse(n).multiply(r.multiply(d).add(z)).mod(n);
            if (s_.equals(BigInteger.ZERO)) continue;
            final BigInteger s = config.canonical && (s_.add(s_).compareTo(n) >= 0) ? n.subtract(s_) : s_;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                final DERSequenceGenerator derSequenceGenerator = new DERSequenceGenerator(bos);
                derSequenceGenerator.addObject(new ASN1Integer(r));
                derSequenceGenerator.addObject(new ASN1Integer(s));
                if (config.recover || config.timeStampAndNonce != null)
                    derSequenceGenerator.addObject(new ASN1Integer(computeRecoveryByte(kp, r, s_, config.canonical)));
                if (config.timeStampAndNonce != null) {
                    derSequenceGenerator.addObject(new ASN1Integer(config.timeStampAndNonce.timeStamp));
                    derSequenceGenerator.addObject(new ASN1Integer(config.timeStampAndNonce.nonce));
                }
                derSequenceGenerator.close();
                return bos.toByteArray();
            } catch (IOException e) {
                throw new SecurityException(e);
            }
        }
    }

    public byte[] sign(byte[] data, SignatureConfig config)
            throws SecurityException {
        GeneralDigest hashDigest = freshDigestFromDigest(config.messageDigest);
        final byte[] hash = new byte[hashDigest.getDigestSize()];
        if (config.timeStampAndNonce != null) {
            final byte[] timeStampBytes = longToBytes(config.timeStampAndNonce.timeStamp);
            hashDigest.update(timeStampBytes, 0, timeStampBytes.length);
            final byte[] nonceBytes = longToBytes(config.timeStampAndNonce.nonce);
            hashDigest.update(nonceBytes, 0, nonceBytes.length);
        }
        hashDigest.update(data, 0, data.length);
        hashDigest.doFinal(hash, 0);
        return signHash(hash, config);
    }

    public byte[] sign(byte[] data) throws SecurityException {
        return sign(data, getDefaultSignatureConfig());
    }

    public byte[] signUTF8String(String string, SignatureConfig config)
            throws SecurityException {
        return sign(stringToUTF8Bytes(string), config);
    }

    public byte[] signUTF8String(String string)
            throws SecurityException {
        return signUTF8String(string, getDefaultSignatureConfig());
    }

    public byte[] diffieHelmanSharedSecret(PublicKey publicKey) throws SecurityException {
        if (!(curveParameters.getCurve().equals(publicKey.curveParameters.getCurve())
                && curveParameters.getG().equals(publicKey.curveParameters.getG())
                && curveParameters.getN().equals(publicKey.curveParameters.getN())
                && curveParameters.getH().equals(publicKey.curveParameters.getH())))
            throw new SecurityException("Public key does not have the same curve parameters as private key");
        return publicKey.point.multiply(this.d).getEncoded(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PrivateKey privateKey = (PrivateKey) o;

        return d.equals(privateKey.d)
                && curveParameters.getCurve().equals(privateKey.curveParameters.getCurve())
                && curveParameters.getG().equals(privateKey.curveParameters.getG())
                && curveParameters.getN().equals(privateKey.curveParameters.getN())
                && curveParameters.getH().equals(privateKey.curveParameters.getH());

    }

    @Override
    public int hashCode() {
        int code = d.hashCode();
        code = 31 * code + curveParameters.getCurve().hashCode();
        code = 37 * code + curveParameters.getG().hashCode();
        code = 41 * code + curveParameters.getN().hashCode();
        code = 43 * code + curveParameters.getH().hashCode();
        return code;
    }
}
