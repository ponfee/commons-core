package code.ponfee.commons.jce.pkcs;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static code.ponfee.commons.jce.Providers.BC;

/**
 * 加密消息语法：Cryptography Message Syntax
 * 
 * @author Ponfee
 */
public final class CryptoMessageSyntax {

    // ---------------------------------------------------------------------------sign/verify
    /**
     * 附原文签名（单人）
     * @param data
     * @param key
     * @param certChain
     * @return
     */
    public static byte[] sign(byte[] data, PrivateKey key, X509Certificate[] certChain) {
        return sign(data, Collections.singletonList(key), Collections.singletonList(certChain));
    }

    /**
     * 附原文签名（多人）
     * @param data
     * @param keys
     * @param certs  证书链（多人list）
     * @return
     */
    public static byte[] sign(byte[] data, List<PrivateKey> keys, List<X509Certificate[]> certs) {
        try {
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            DigestCalculatorProvider dcp = new JcaDigestCalculatorProviderBuilder().setProvider(BC).build();
            for (int i = 0; i < keys.size(); i++) {
                gen.addCertificates(new JcaCertStore(Arrays.asList(certs.get(i))));

                ContentSigner signer = new JcaContentSignerBuilder(certs.get(i)[0].getSigAlgName())
                                                         .setProvider(BC).build(keys.get(i));
                JcaSignerInfoGeneratorBuilder jsBuilder = new JcaSignerInfoGeneratorBuilder(dcp);
                gen.addSignerInfoGenerator(jsBuilder.build(signer, certs.get(i)[0]));
            }
            return gen.generate(new CMSProcessableByteArray(data), true).getEncoded(); // true附原文
        } catch (OperatorCreationException | CertificateEncodingException | CMSException | IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 验签（附原文）
     * @param signed
     * @return
     */
    public static void verify(byte[] signed) {
        try {
            CMSSignedData sign = new CMSSignedData(signed); // 构建PKCS#7签名数据处理对象
            Store<?> store = sign.getCertificates();
            JcaSimpleSignerInfoVerifierBuilder builder = new JcaSimpleSignerInfoVerifierBuilder()
                                                                                 .setProvider(BC);
            for (SignerInformation signer : sign.getSignerInfos()) {
                @SuppressWarnings("unchecked") 
                Collection<X509CertificateHolder> chain = store.getMatches(signer.getSID()); // 证书链
                X509CertificateHolder cert = chain.iterator().next(); // 证书链的第一个为subject cert
                if (!signer.verify(builder.build(cert))) {
                    String sn = Hex.encodeHexString(cert.getSerialNumber().toByteArray());
                    String dn = cert.getSubject().toString();
                    throw new SecurityException("signature verify fail[" + sn + ", " + dn + "]");
                }
            }
        } catch (OperatorCreationException | CertificateException | CMSException e) {
            throw new SecurityException(e);
        }
    }

    // ---------------------------------------------------------------------------envelop/unenvelop
    /**
     * 构造数字信封
     * 
     * PKCSObjectIdentifiers#des_EDE3_CBC
     * PKCSObjectIdentifiers.RC2_CBC
     * 
     * new ASN1ObjectIdentifier("1.2.840.113549.3.2"); // RSA_RC2
     * new ASN1ObjectIdentifier("1.2.840.113549.3.4"); // RSA_RC4
     * 
     * new ASN1ObjectIdentifier("1.3.14.3.2.7"); // DES_CBC
     * new ASN1ObjectIdentifier("1.2.840.113549.3.7"); // DESede_CBC
     * 
     * new ASN1ObjectIdentifier("2.16.840.1.101.3.4.1.2"); // AES128_CBC
     * 
     * @param data
     * @param cert
     * @param alg
     * @return
     */
    public static byte[] envelop(byte[] data, X509Certificate cert, ASN1ObjectIdentifier alg) {
        try {
            //添加数字信封
            CMSEnvelopedDataGenerator edGen = new CMSEnvelopedDataGenerator();
            edGen.addRecipientInfoGenerator(
                new JceKeyTransRecipientInfoGenerator(cert).setProvider(BC)
            );

            return edGen.generate(
                new CMSProcessableByteArray(data),
                new JceCMSContentEncryptorBuilder(alg).setProvider(BC).build()
            ).getEncoded();
        } catch (CertificateEncodingException | CMSException | IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 解数字信封
     * @param enveloped
     * @param privateKey
     * @return
     */
    public static byte[] unenvelop(byte[] enveloped, X509Certificate cert, PrivateKey privateKey) {
        try {
            RecipientInformationStore ris = new CMSEnvelopedData(enveloped).getRecipientInfos();

            for (RecipientInformation rin : ris.getRecipients()) {
                KeyTransRecipientId rid = (KeyTransRecipientId) rin.getRID();
                // 匹配
                if (cert.getSerialNumber().equals(rid.getSerialNumber())) {
                    // 解密
                    return rin.getContent(
                        new JceKeyTransEnvelopedRecipient(privateKey).setProvider(BC)
                    );
                }
            }
            return null;
        } catch (CMSException e) {
            throw new SecurityException(e);
        }
    }

}
