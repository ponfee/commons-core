package code.ponfee.commons.jce.pkcs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Hex;

import code.ponfee.commons.jce.Providers;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.ParsingException;
import sun.security.pkcs.SignerInfo;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

/**
 * pkcs7工具类
 * @author fupf
 */
@SuppressWarnings("restriction")
public class PKCS7Signature {

    /*private static final Map<String, String> HASH_SIGN_ALG = ImmutableMap.<String, String>builder()
        .put("1.2.840.113549.1.1.4", "MD5")
        .put("1.2.840.113549.1.1.5", "SHA-1")
        .put("1.2.840.113549.1.1.11", "SHA-256")
        .put("1.2.840.113549.1.1.12", "SHA-384")
        .put("1.2.840.113549.1.1.13", "SHA-512")
        .build();*/

    /**
     * byte流数据签名（单人）
     * @param privKey
     * @param cert
     * @param data 是否附原文
     * @param attach
     * @return
     */
    public static byte[] sign(PrivateKey privKey, X509Certificate cert, byte[] data, boolean attach) {
        return sign(new PrivateKey[] { privKey }, new X509Certificate[] { cert }, data, attach);
    }

    /**
     * byte流数据签名（多人）
     * @param privKeys
     * @param certs
     * @param data
     * @param attach
     * @return
     */
    public static byte[] sign(PrivateKey[] privKeys, X509Certificate[] certs, byte[] data, boolean attach) {
        ContentInfo contentInfo;
        if (attach) {
            contentInfo = new ContentInfo(data);
        } else {
            contentInfo = new ContentInfo(ContentInfo.DATA_OID, null);
        }
        return sign(contentInfo, data, certs, privKeys);
    }

    /**
     * 文本签名（单人）
     * @param privKey
     * @param cert
     * @param data
     * @param attach 是否附原文
     * @return
     */
    public static byte[] sign(PrivateKey privKey, X509Certificate cert, String data, boolean attach) {
        return sign(new PrivateKey[] { privKey }, new X509Certificate[] { cert }, data, attach);
    }

    /**
     * 文本签名（多人）
     * @param privKeys
     * @param certs
     * @param data
     * @param attach
     * @return
     */
    public static byte[] sign(PrivateKey[] privKeys, X509Certificate[] certs, String data, boolean attach) {
        try {
            DerValue dv = null;
            if (attach) {
                dv = new DerValue(data);
            }
            ContentInfo contentInfo = new ContentInfo(ContentInfo.DATA_OID, dv);
            return sign(contentInfo, data.getBytes(), certs, privKeys);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 附原文的验签（pkcs7方式验签，可验证CMS格式签名）
     * @param pkcs7Data  the pkcs7 byte array data, with origin
     * @return the origin byte data
     */
    public static byte[] verify(byte[] pkcs7Data) {
        PKCS7 pkcs7 = getPkcs7(pkcs7Data);
        byte[] data = getContent(pkcs7);
        verify(pkcs7, data);
        return data;
    }

    /**
     * 不附原文的验签（pkcs7方式验签，可验证CMS格式签名）
     * @param pkcs7Data  the pkcs7 byte array data, without origin
     * @param data  the origin byte data
     * @return
     */
    public static void verify(byte[] pkcs7Data, byte[] data) {
        verify(getPkcs7(pkcs7Data), data);
    }

    public static void verify(PKCS7 pkcs7, byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("the origin data cannot be null.");
        }

        try {
            for (SignerInfo signed : pkcs7.getSignerInfos()) {
                if (pkcs7.verify(signed, data) == null) {
                    String certSN = Hex.encodeHexString(signed.getCertificateSerialNumber().toByteArray());
                    String certDN = signed.getCertificate(pkcs7).getSubjectX500Principal().getName();
                    //new X509Principal(signed.getCertificate(pkcs7).getSubjectX500Principal().getEncoded()).getName()
                    throw new SecurityException("验签失败[certSN：" + certSN + "；CertDN：" + certDN + "]");
                }
            }
        } catch (NoSuchAlgorithmException | SignatureException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 签名方法体
     * @param contentInfo
     * @param certs
     * @param keys
     * @return
     */
    private static byte[] sign(ContentInfo contentInfo, byte[] data, 
                               X509Certificate[] certs, PrivateKey[] keys) {
        SignerInfo[] signs = new SignerInfo[keys.length];
        AlgorithmId[] digestAlgorithmIds = new AlgorithmId[keys.length];
        for (int i = 0; i < keys.length; i++) {
            X509Certificate cert = certs[i];
            PrivateKey privKey = keys[i];
            try {
                /*AlgorithmId digAlg = AlgorithmId.get(HASH_SIGN_ALG.get(cert.getSigAlgOID()));
                AlgorithmId encAlg = new AlgorithmId(AlgorithmId.RSAEncryption_oid);*/
                AlgorithmId digAlg = AlgorithmId.get(AlgorithmId.getDigAlgFromSigAlg(cert.getSigAlgName()));
                AlgorithmId encAlg = AlgorithmId.get(AlgorithmId.getEncAlgFromSigAlg(cert.getSigAlgName()));
                digestAlgorithmIds[i] = digAlg;
                X500Name name = new X500Name(cert.getIssuerX500Principal().getEncoded());

                Signature signer = Providers.getSignature(cert.getSigAlgName());
                signer.initSign(privKey);
                signer.update(data); // signer.update(data, 0, data.length);
                signs[i] = new SignerInfo(name, cert.getSerialNumber(), digAlg, encAlg, signer.sign());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 构造PKCS7数据
        PKCS7 pkcs7 = new PKCS7(digestAlgorithmIds, contentInfo, certs, signs);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pkcs7.encodeSignedData(out);
            out.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get the pkcs7 from byte array data
     * @param pkcs7Data
     * @return
     */
    public static PKCS7 getPkcs7(byte[] pkcs7Data) {
        try {
            return new PKCS7(pkcs7Data);
        } catch (ParsingException e) {
            throw new IllegalArgumentException("Invalid pacs7 data", e);
        }
    }

    /**
     * get the origin byte array data from pkcs7
     * @param pkcs7
     * @return
     */
    public static byte[] getContent(PKCS7 pkcs7) {
        ContentInfo contentInfo = pkcs7.getContentInfo();
        try {
            byte[] data;
            if (contentInfo.getContent() == null) {
                data = contentInfo.getData();
            } else {
                try {
                    data = contentInfo.getContent().getOctetString();
                } catch (Exception e) {
                    data = contentInfo.getContent().getDataBytes();
                }
            }
            return data;
        } catch (IOException e) {
            throw new SecurityException("Get content from pkcs7 occur error", e);
        }
    }
}
