package code.ponfee.commons.jce.pkcs;

import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bjca.asn1.ASN1InputStream;
import org.bjca.asn1.DERConstructedSequence;
import org.bjca.asn1.DERInteger;
import org.bjca.asn1.DERObject;
import org.bjca.asn1.DERObjectIdentifier;
import org.bjca.asn1.DEROctetString;
import org.bjca.asn1.DEROutputStream;
import org.bjca.asn1.DERSet;
import org.bjca.asn1.DERTaggedObject;
import org.bjca.asn1.pkcs.IssuerAndSerialNumber;
import org.bjca.asn1.x509.AlgorithmIdentifier;
import org.bjca.asn1.x509.X509Name;
import org.bjca.jce.fastparser.EncryptedContentInfo;
import org.bjca.jce.fastparser.EnvelopedData;
import org.bjca.jce.fastparser.FastPkcs7;
import org.bjca.jce.fastparser.Item;
import org.bjca.jce.fastparser.RecipientInfo;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.util.Arrays;

import code.ponfee.commons.io.Closeables;
import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.util.SecureRandoms;

/**
 * 数字信封（不带签名）
 * 支持的算法有：RC4，DESede
 * 
 * CryptoMessageSyntax可以解PKCS7Envelope加密的数字信封
 * 
 * @author fupf
 */
@SuppressWarnings("deprecation")
public final class PKCS7Envelope {

    private static final String OID_ENVELOPEDDATATYPE = "1.2.840.113549.1.7.3";
    private static final String OID_ENCRYPTCONTENT = "1.2.840.113549.1.7.1";
    private static final String OID_RSA_ECB_PKCS1PADDING = "1.2.840.113549.1.1.1";
    private static final String TRANSFORM = "RSA/ECB/PKCS1Padding";

    /**
     * 数字信封打包
     * @param plaindata
     * @param cert
     * @param alg
     * @return
     */
    public static byte[] envelop(byte[] plaindata, X509Certificate cert, AlgorithmMapping alg) {
        DEROutputStream dout = null;
        try {
            // 生成对称密钥
            SecretKey key = Providers.getKeyGenerator(alg.name).generateKey();

            // 公钥对对称密钥加密
            Cipher cipher = Providers.getCipher(TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
            byte[] encKey = cipher.doFinal(key.getEncoded());

            // 对称密钥对原文加密
            DERObject iv = alg.ivLen > 0 ? new DEROctetString(SecureRandoms.nextBytes(alg.ivLen)) : null;
            AlgorithmIdentifier derAlg = new AlgorithmIdentifier(new DERObjectIdentifier(alg.oid), iv);
            DEROctetString derParam = (DEROctetString) derAlg.getParameters();
            cipher = Providers.getCipher(alg.transform);
            if (derParam != null) {
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(derParam.getOctets()));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }
            byte[] encdata = cipher.doFinal(plaindata);

            // 填充接收者信息
            X509Principal p = new X509Principal(cert.getIssuerX500Principal().getEncoded());
            DERConstructedSequence recipientInfo = new DERConstructedSequence();
            recipientInfo.addObject(new DERInteger(0));
            recipientInfo.addObject(
                new IssuerAndSerialNumber(new X509Name(p.getName()), new DERInteger(cert.getSerialNumber()))
            );
            recipientInfo.addObject(
                new AlgorithmIdentifier(new DERObjectIdentifier(OID_RSA_ECB_PKCS1PADDING), null)
            );
            recipientInfo.addObject(new DEROctetString(encKey));
            DERSet recipientInfos = new DERSet(recipientInfo);

            // 加密内容、对称加密算法及向量
            DERConstructedSequence cont = new DERConstructedSequence();
            cont.addObject(new DERObjectIdentifier(OID_ENCRYPTCONTENT));
            cont.addObject(derAlg);
            cont.addObject(new DERTaggedObject(false, 0, new DEROctetString(encdata)));

            // 打包数字信封
            DERConstructedSequence env = new DERConstructedSequence();
            env.addObject(new DERInteger(0));
            env.addObject(recipientInfos);
            env.addObject(cont);

            // 封装成pkcs#7 format
            DERConstructedSequence pkcs7 = new DERConstructedSequence();
            pkcs7.addObject(new DERObjectIdentifier(OID_ENVELOPEDDATATYPE));
            pkcs7.addObject(new DERTaggedObject(true, 0, env));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dout = new DEROutputStream(baos);
            dout.writeObject(pkcs7);
            dout.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new SecurityException(e);
        } finally {
            Closeables.closeConsole(dout);
        }
    }

    /**
     * 拆封数字信封
     * @param envelopeddata
     * @param cert
     * @param privateKey
     * @return
     */
    public static byte[] unenvelop(byte[] envelopeddata, X509Certificate cert, PrivateKey privateKey) {
        FastPkcs7 fastPkcs7 = new FastPkcs7();
        if (!fastPkcs7.pkcs7Data(envelopeddata)) {
            throw new SecurityException("can't decode PKCS7Envlope object");
        }
        EnvelopedData ed = fastPkcs7.getEnvelopedData();
        if (ed == null) {
            throw new SecurityException("invalid pkcs#7 envloped-data wrong header "
                                      + fastPkcs7.getContentType());
        }

        RecipientInfo recipientInfo = (RecipientInfo) ed.getVRecipientInfo().get(0);

        // 获取公钥证书序列号并校验
        Item item = recipientInfo.getIssuerAndSerialNumber();
        org.bjca.jce.fastparser.IssuerAndSerialNumber iasn =
                new org.bjca.jce.fastparser.IssuerAndSerialNumber(envelopeddata, item);
        /*byte[] ss = new byte[item.length];
        ss = DerUtil.getItemDataAndTag(envelopeddata, item);*/
        if (!cert.getSerialNumber().equals(iasn.getSerialNumber().getSerialNumber())) {
            throw new SecurityException("certificate is not match");
        }

        ASN1InputStream input = null;
        try {
            // 解密被加密的对称密钥
            byte[] encKey = recipientInfo.getEncKey();
            Cipher c = Providers.getCipher(TRANSFORM);
            c.init(2, privateKey);
            byte[] key = c.doFinal(encKey);

            // 获取对称加密算法
            EncryptedContentInfo eci = ed.getEncryptedContentInfoObject();
            item = eci.getContentEncryptionAlgorithm();
            byte[] bs = Arrays.copyOfRange(envelopeddata, item.offset, item.offset + item.length);
            input = new ASN1InputStream(bs);
            AlgorithmIdentifier alg = AlgorithmIdentifier.getInstance(input.readObject());
            String algoid = alg.getObjectId().getId();
            AlgorithmMapping algorithm = getAlgByOid(algoid);
            Cipher cipher = Providers.getCipher(algorithm.transform);

            // 对称加密向量参数
            item = eci.getIvParameter();
            byte[] iv = Arrays.copyOfRange(envelopeddata, item.offset, item.offset + item.length);

            // 获取密文
            item = eci.getEncryptedContent();
            byte[] encdata = Arrays.copyOfRange(envelopeddata, item.offset, item.offset + item.length);

            // 解密
            SecretKey secretKey = new SecretKeySpec(key, algorithm.name);
            if (iv.length < 1) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            }
            return cipher.doFinal(encdata);
        } catch (Exception e) {
            throw new SecurityException(e);
        } finally {
            Closeables.closeConsole(input);
        }
    }

    private static AlgorithmMapping getAlgByOid(String oid) {
        for (AlgorithmMapping alg : AlgorithmMapping.values()) {
            if (alg.oid.equals(oid)) {
                return alg;
            }
        }
        throw new IllegalArgumentException("unknown the alg oid: " + oid);
    }

    /**
     * 算法相关
     */
    public enum AlgorithmMapping {
        AES128_WRAP("2.16.840.1.101.3.4.1.5", "AES", "AES", 0), //
        AES192_WRAP("2.16.840.1.101.3.4.1.25", "AES", "AES", 0), //
        AES256_WRAP("2.16.840.1.101.3.4.1.45", "AES", "AES", 0), //

        RSA_RC2("1.2.840.113549.3.2", "RC2", "RC2", 0), //
        RSA_RC4("1.2.840.113549.3.4", "RC4", "RC4", 0), //

        DES_CBC("1.3.14.3.2.7", "DES", "DES/CBC/PKCS5Padding", 8), //
        DESede_CBC("1.2.840.113549.3.7", "DESede", "DESede/CBC/PKCS5Padding", 8), //

        AES128_CBC("2.16.840.1.101.3.4.1.2", "AES", "AES/CBC/PKCS5Padding", 16), //
        AES192_CBC("2.16.840.1.101.3.4.1.22", "AES", "AES/CBC/PKCS5Padding", 16), //
        AES256_CBC("2.16.840.1.101.3.4.1.42", "AES", "AES/CBC/PKCS5Padding", 16), //
        ;

        final String oid;
        final String name;
        final String transform;
        final int ivLen;

        AlgorithmMapping(String oid, String name, String transform, int ivLen) {
            this.oid = oid;
            this.name = name;
            this.transform = transform;
            this.ivLen = ivLen;
        }
    }

}
