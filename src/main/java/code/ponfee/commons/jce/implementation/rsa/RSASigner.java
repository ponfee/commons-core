package code.ponfee.commons.jce.implementation.rsa;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.jce.DigestAlgorithms;
import code.ponfee.commons.jce.digest.DigestUtils;

/**
 * RSA sign
 * https://www.cnblogs.com/jintianhu/p/5051169.html
 * 
 * @see org.bouncycastle.crypto.signers.RSADigestSigner
 * 
 * @author Ponfee
 */
public class RSASigner {

    private static final Map<String, ASN1ObjectIdentifier> HASH_OID_MAPPING =
        ImmutableMap.<String, ASN1ObjectIdentifier> builder() // 
            .put("RIPEMD128", TeleTrusTObjectIdentifiers.ripemd128) // 
            .put("RIPEMD160", TeleTrusTObjectIdentifiers.ripemd160) // 
            .put("RIPEMD256", TeleTrusTObjectIdentifiers.ripemd256) // 

            .put("SHA-1", X509ObjectIdentifiers.id_SHA1) // 
            .put("SHA-224", NISTObjectIdentifiers.id_sha224) // 
            .put("SHA-256", NISTObjectIdentifiers.id_sha256) // 
            .put("SHA-384", NISTObjectIdentifiers.id_sha384) // 
            .put("SHA-512", NISTObjectIdentifiers.id_sha512) // 
            .put("SHA-512/224", NISTObjectIdentifiers.id_sha512_224) // 
            .put("SHA-512/256", NISTObjectIdentifiers.id_sha512_256) // 

            .put("SHA3-224", NISTObjectIdentifiers.id_sha3_224) // 
            .put("SHA3-256", NISTObjectIdentifiers.id_sha3_256) // 
            .put("SHA3-384", NISTObjectIdentifiers.id_sha3_384) // 
            .put("SHA3-512", NISTObjectIdentifiers.id_sha3_512) // 

            .put("MD2", PKCSObjectIdentifiers.md2) // 
            .put("MD4", PKCSObjectIdentifiers.md4) // 
            .put("MD5", PKCSObjectIdentifiers.md5) // 
            .build();

    private final AsymmetricBlockCipher rsaEngine = new PKCS1Encoding(new RSABlindedEngine());
    private final RSAKey rsaKey;

    public RSASigner(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
        if (rsaKey.secret) {
            // 签名
            rsaEngine.init(true, new RSAKeyParameters(true, rsaKey.n, rsaKey.d));
        } else {
            // 验签
            rsaEngine.init(false, new RSAKeyParameters(false, rsaKey.n, rsaKey.e));
        }
    }

    public byte[] signSha1(byte[] data) {
        return sign(data, DigestAlgorithms.SHA1);
    }

    public boolean verifySha1(byte[] data, byte[] signature) {
        return verify(data, signature, DigestAlgorithms.SHA1);
    }

    public byte[] signSha256(byte[] data) {
        return sign(data, DigestAlgorithms.SHA256);
    }

    public boolean verifySha256(byte[] data, byte[] signature) {
        return verify(data, signature, DigestAlgorithms.SHA256);
    }

    public byte[] sign(byte[] data, DigestAlgorithms alg) {
        if (!this.rsaKey.isSecret()) {
            throw new IllegalArgumentException("Sign must use private key.");
        }

        ASN1ObjectIdentifier oid = HASH_OID_MAPPING.get(alg.algorithm());
        if (oid == null) {
            throw new IllegalArgumentException("Invalid hash algorithm " + alg.name());
        }

        // data hash
        byte[] hash = DigestUtils.digest(alg, data);

        try {
            byte[] result = derEncode(hash, oid);
            return rsaEngine.processBlock(result, 0, result.length);
        } catch (InvalidCipherTextException | IOException e) {
            throw new SecurityException(e);
        }
    }

    public boolean verify(byte[] data, byte[] signature, DigestAlgorithms alg) {
        if (this.rsaKey.isSecret()) {
            throw new IllegalArgumentException("Verify signature must use public key.");
        }

        ASN1ObjectIdentifier oid = HASH_OID_MAPPING.get(alg.algorithm());
        if (oid == null) {
            throw new IllegalArgumentException("Invalid hash algorithm " + alg.name());
        }

        // hash data
        byte[] hash = DigestUtils.digest(alg, data);

        byte[] sig;
        byte[] expected;
        try {
            expected = derEncode(hash, oid);
            sig = rsaEngine.processBlock(signature, 0, signature.length);
        } catch (InvalidCipherTextException | IOException e) {
            return false;
        }

        if (sig.length == expected.length) {
            return Arrays.equals(sig, expected);
        } else if (sig.length == expected.length - 2) { // NULL left out
            int sigOffset = sig.length - hash.length - 2;
            int expOffset = expected.length - hash.length - 2;

            expected[1] -= 2; // adjust lengths
            expected[3] -= 2;
            for (int i = 0; i < sigOffset; i++) {
                // check header less NULL
                if (sig[i] != expected[i]) {
                    return false;
                }
            }

            for (int i = 0; i < hash.length; i++) {
                // check hash data
                if (sig[sigOffset + i] != expected[expOffset + i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * DER :: {
     *     BERTags.SEQUENCE|BERTags.CONSTRUCTED, 
     *     totalLength,
     *     BERTags.OBJECT_IDENTIFIER,   -- {@link ASN1ObjectIdentifier#encode}
     *     algLength,
     *     algBody,
     *     BERTags.OCTET_STRING,        -- {@link org.bouncycastle.asn1.DEROctetString#encode}
     *     digestLength,
     *     digestBody
     * }
     * 
     * @param hash
     * @param digestOid
     * @return the byte array of der encoded
     * @throws IOException
     * @see org.bouncycastle.asn1.DERSequence
     * @see org.bouncycastle.asn1.x509.DigestInfo
     * @see org.bouncycastle.asn1.BERTags
     */
    private byte[] derEncode(byte[] hash, ASN1ObjectIdentifier digestOid) 
        throws IOException {
        AlgorithmIdentifier algId = new AlgorithmIdentifier(digestOid, DERNull.INSTANCE);
        DigestInfo dInfo = new DigestInfo(algId, hash);
        return dInfo.getEncoded(ASN1Encoding.DER);
    }
}
