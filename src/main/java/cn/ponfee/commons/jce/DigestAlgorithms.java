/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce;

/**
 * The Digest Algorithms<p>
 * 
 * SHA-3
 *   <li>https://www.oschina.net/translate/keccak-the-new-sha-3-encryption-standard </li>
 *   <li>http://www.cnblogs.com/dacainiao/p/5554756.html</li>
 *   <li>SHA256算法原理：https://zhuanlan.zhihu.com/p/94619052</li>
 * 
 * @author Ponfee
 */
public enum DigestAlgorithms {

    MD5(128), // 

    RipeMD128(128), RipeMD160(160), RipeMD256(256), RipeMD320(320), // 

    SHA1("SHA-1", 160), SHA224("SHA-224", 224), SHA256("SHA-256", 256), // 
    SHA384("SHA-384", 384), SHA512("SHA-512", 512), // 

    /**
     * @see org.bouncycastle.crypto.digests.SM3Digest
     * @see org.bouncycastle.jcajce.provider.digest.SM3
     */
    SM3(256), // 

    // SHAKE128 algorithm only support use in org.bouncycastle.crypto.digests.SHAKEDigest
    //SHAKE128(128), SHAKE256(256),

    // -----------------------SHA-3 Finalists: BLAKE, Grstl, JH, Keccak and Skein
    /**
     * @see org.bouncycastle.crypto.digests.Blake2sDigest
     * @see org.bouncycastle.jcajce.provider.digest.Blake2s
     */
    BLAKE2S128("BLAKE2S-128", 128), BLAKE2S160("BLAKE2S-160", 160), //
    BLAKE2S224("BLAKE2S-224", 224), BLAKE2S256("BLAKE2S-256", 256), //

    /**
     * @see org.bouncycastle.crypto.digests.Blake2bDigest
     * @see org.bouncycastle.jcajce.provider.digest.Blake2b
     */
    BLAKE2B160("BLAKE2B-160", 160), BLAKE2B256("BLAKE2B-256", 256), //
    BLAKE2B384("BLAKE2B-384", 384), BLAKE2B512("BLAKE2B-512", 512), //

    /**
     * @see org.bouncycastle.crypto.digests.KeccakDigest
     * @see org.bouncycastle.jcajce.provider.digest.Keccak
     */
    KECCAK224("KECCAK-224", 224), KECCAK256("KECCAK-256", 256), // 
    KECCAK288("KECCAK-288", 288), KECCAK384("KECCAK-384", 384), // 
    KECCAK512("KECCAK-512", 512), //

    /**
     * @see org.bouncycastle.crypto.digests.SkeinDigest
     * @see org.bouncycastle.jcajce.provider.digest.Skein
     */
    SKEIN_256_128 ("Skein-256-128",  128), SKEIN_256_256  ("Skein-256-256",    256), // 
    SKEIN_512_256 ("Skein-512-256",  256), SKEIN_512_512  ("Skein-512-512",    512), // 
    SKEIN_1024_512("Skein-1024-512", 512), SKEIN_1024_1024("Skein-1024-1024", 1024), // 

    /**
     * @see org.bouncycastle.crypto.digests.SHA3Digest
     * @see org.bouncycastle.jcajce.provider.digest.SHA3
     */
    SHA3_224("SHA3-224", 224), SHA3_256("SHA3-256", 256), // 
    SHA3_384("SHA3-384", 384), SHA3_512("SHA3-512", 512), //
    ;

    private final String algorithm;
    private final int byteSize;

    DigestAlgorithms(int bitLen) {
        this.algorithm = this.name();
        this.byteSize = bitLen >>> 3;
    }

    DigestAlgorithms(String algorithm, int bitLen) {
        this.algorithm = algorithm;
        this.byteSize = bitLen >>> 3;
    }

    public String algorithm() {
        return this.algorithm;
    }

    public int byteSize() {
        return byteSize;
    }

}
