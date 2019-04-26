package code.ponfee.commons.jce.symmetric;

/**
 * <pre>
 *   对称密钥算法bit(位)
 *     DES                  key size must be equal to 64
 *     DESede(TripleDES)    key size must be equal to 112 or 168
 *     AES                  key size must be equal to 128, 192 or 256, but 192 and 256 bits may be unsupport
 *     Blowfish             key size must be multiple of 8, and can only range from 32 to 448 (inclusive)
 *     RC2                  key size must be between 40 and 1024 bits(block cipher, 曾经被考虑作为DES算法的替代品, 比DES快)
 *     RC4(ARCFOUR)         key size must be between 40 and 1024 bits(stream cipher)
 * </pre>
 * 
 * 速度排名：IDEA < DES < GASTI28 < GOST < AES < RC4 < TEA < Blowfish
 * 
 * 1、DES（Data Encryption Standard）：对称算法，数据加密标准，速度较快，适用于加密大量数据的场合； 
 * 2、3DES（Triple DES）：是基于DES的对称算法，对一块数据用三个不同的密钥进行三次加密，强度更高；
 * 3、RC2和RC4：对称算法，用变长密钥对大量数据进行加密，比 DES快；
 * 4、IDEA（International Data Encryption Algorithm）国际数据加密算法，使用128位密钥提供非常强的安全性；
 * 5、RSA：由 RSA 公司发明，是一个支持变长密钥的公共密钥算法，需要加密的文件块的长度也是可变的，非对称算法； 
 * 6、DSA（Digital Signature Algorithm）：数字签名算法，是一种标准的 DSS（数字签名标准），严格来说不算加密算法；
 * 7、AES（Advanced Encryption Standard）：高级加密标准，对称算法，是下一代的加密算法标准，速度快，安全级别高，在21世纪AES标准的一个实现是Rijndael算法；
 * 8、BLOWFISH，它使用变长的密钥，长度可达448位，运行速度很快；
 * 10、PKCS:The Public-Key Cryptography Standards (PKCS)是由美国RSA数据安全公司及其合作伙伴制定的一组公钥密码学标准，
 *    其中包括证书申请、证书更新、证书作废表发布、扩展证书内容以及数字签名、数字信封的格式等方面的一系列相关协议。
 * 11、SSF33，SSF28，SCB2(SM1)：国家密码局的隐蔽不公开的商用算法，在国内民用和商用的，除这些都不容许使用外，其他的都可以使用；
 * 12、ECC（Elliptic Curves Cryptography）：椭圆曲线密码编码学。
 * 13、TEA(Tiny Encryption Algorithm)简单高效的加密算法，加密解密速度快，实现简单。但安全性不如DES，QQ一直用tea加密
 * 
 * https://bouncycastle.org/documentation.html
 * https://downloads.bouncycastle.org/fips-java/BC-FJA-UserGuide-1.0.0.pdf
 * https://downloads.bouncycastle.org/fips-java/BC-FJA-(D)TLSUserGuide-1.0.3.pdf
 * 
 * @see org.bouncycastle.jcajce.provider.symmetric.ARC4
 * 
 * @author fupf
 */
public enum Algorithm {

    AES, DES, DESede, Blowfish, RC2, RC4, // RC4: ARC4, ARCFOUR
    RC5, IDEA, TEA, TDEA, Camellia, CAST5, //
    GOST, GOST3411, GOST28147, SEED, //
    Serpent, SHACAL2, Twofish, SM4, //
    ;

}
