package code.ponfee.commons.jce;

import com.google.common.collect.ImmutableBiMap;

/**
 * The Hamc Algorithms
 * @author Ponfee
 */
public enum HmacAlgorithms {

    HmacMD5(128), //

    HmacRipeMD128(128), HmacRipeMD160(160), // 
    HmacRipeMD256(256), HmacRipeMD320(320), // 

    HmacSHA1(160), HmacSHA224(224), // 
    HmacSHA256(256), HmacSHA384(384), // 
    HmacSHA512(512), // 

    // org.bouncycastle.crypto.digests.SM3Digest cannot support hmac algorithm
    //HmacSM3(256), 

    // org.bouncycastle.crypto.digests.SHAKEDigest cannot support hmac algorithm
    //HmacSHAKE128(128), HmacSHAKE256(256), 

    /**
     * @see org.bouncycastle.crypto.digests.KeccakDigest
     * @see org.bouncycastle.jcajce.provider.digest.Keccak
     */
    HmacKECCAK224(224), HmacKECCAK288(288), // 
    HmacKECCAK256(256), HmacKECCAK384(384), // 
    HmacKECCAK512(512), //

    HmacSKEIN_256_128("Skein-MAC-256-128", 128), // 
    HmacSKEIN_256_256("Skein-MAC-256-256", 256), // 
    HmacSKEIN_512_256("Skein-MAC-512-256", 256), // 
    HmacSKEIN_512_512("Skein-MAC-512-512", 512), // 
    HmacSKEIN_1024_512("Skein-MAC-1024-512", 512), // 
    HmacSKEIN_1024_1024("Skein-MAC-1024-1024", 1024), // 

    /**
     * @see org.bouncycastle.crypto.digests.SHA3Digest
     * @see org.bouncycastle.jcajce.provider.digest.SHA3
     */
    HmacSHA3_224("HmacSHA3-224", 224), HmacSHA3_256("HmacSHA3-256", 256), // 
    HmacSHA3_384("HmacSHA3-384", 384), HmacSHA3_512("HmacSHA3-512", 512), // 
    ;

    private final String algorithm;
    private final int byteSize;

    HmacAlgorithms(int bitLen) {
        this.algorithm = this.name();
        this.byteSize = bitLen >>> 3;
    }

    HmacAlgorithms(String algorithm, int bitLen) {
        this.algorithm = algorithm;
        this.byteSize = bitLen >>> 3;
    }

    public String algorithm() {
        return this.algorithm;
    }

    public int byteSize() {
        return this.byteSize;
    }

    public static final ImmutableBiMap<Integer, HmacAlgorithms> ALGORITHM_MAPPING =
        ImmutableBiMap.<Integer, HmacAlgorithms> builder()
            .put(1, HmacAlgorithms.HmacSHA256)
            .put(2, HmacAlgorithms.HmacSHA512)
            .put(3, HmacAlgorithms.HmacKECCAK256)
            .put(4, HmacAlgorithms.HmacKECCAK512)
            .put(5, HmacAlgorithms.HmacSHA3_256)
            .put(6, HmacAlgorithms.HmacSHA3_512)
            .build();
}
