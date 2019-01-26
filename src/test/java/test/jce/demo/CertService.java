//package test.jce.demo;
//
//import java.util.Map;
//import java.util.Date;
//import java.util.Vector;
//import java.util.HashMap;
//import java.util.Hashtable;
//import java.math.BigInteger;
//import java.security.KeyPair;
//import java.security.Security;
//import java.security.PublicKey;
//import java.security.Signature;
//import java.io.FileOutputStream;
//import java.security.PrivateKey;
//import org.bouncycastle.asn1.DERSet;
//import java.security.KeyPairGenerator;
//import org.bouncycastle.asn1.DERUTCTime;
//import org.bouncycastle.asn1.DERInteger;
//import org.bouncycastle.asn1.DERSequence;
//import org.bouncycastle.asn1.DERBitString;
//import org.bouncycastle.asn1.x500.X500Name;
//import org.bouncycastle.asn1.x509.KeyUsage;
//import org.bouncycastle.asn1.x509.Attribute;
//import org.bouncycastle.asn1.DERTaggedObject;
//import org.bouncycastle.util.encoders.Base64;
//import org.bouncycastle.asn1.x509.GeneralName;
//import org.bouncycastle.asn1.x509.GeneralNames;
//import org.bouncycastle.asn1.x509.CRLDistPoint;
//import org.bouncycastle.asn1.x509.KeyPurposeId;
//import org.bouncycastle.asn1.DERPrintableString;
//import org.bouncycastle.asn1.DERGeneralizedTime;
//import org.bouncycastle.asn1.x509.GeneralSubtree;
//import org.bouncycastle.asn1.x509.PolicyMappings;
//import org.bouncycastle.asn1.DERObjectIdentifier;
//import org.bouncycastle.asn1.ASN1EncodableVector;
//import org.bouncycastle.asn1.x509.X509Extensions;
//import org.bouncycastle.asn1.x509.NameConstraints;
//import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
//import org.bouncycastle.asn1.x509.BasicConstraints;
//import org.bouncycastle.asn1.x509.AccessDescription;
//import org.bouncycastle.asn1.x509.PolicyInformation;
//import org.bouncycastle.asn1.x509.DistributionPoint;
//import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
//import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
//import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
//import org.bouncycastle.asn1.x509.DistributionPointName;
//import org.bouncycastle.asn1.x509.PrivateKeyUsagePeriod;
//import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
//import org.bouncycastle.asn1.x509.TBSCertificateStructure;
//import org.bouncycastle.asn1.x509.X509ExtensionsGenerator;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.asn1.x509.X509CertificateStructure;
//import org.bouncycastle.jce.provider.X509CertificateObject;
//import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
//import org.bouncycastle.asn1.x509.SubjectDirectoryAttributes;
//
//public class CertService {
//
//    public static void main(String[] agrs) throws Exception {
//
//        Security.addProvider(new BouncyCastleProvider()); // 加载BC Provider
//
//        int certValidity = 1; // 证书有效期
//
//        X500Name subject = new X500Name("CN=root,O=O,OU=OU"); //证书主题
//
//        X500Name issuer = new X500Name("CN=root,O=O,OU=OU"); //证书颁发者
//
//        KeyPair kp = genKeyPair("RSA"); // 产生RSA算法密钥对
//
//        PrivateKey priKey = kp.getPrivate();
//        PublicKey pubKey = kp.getPublic();
//
//        // 签发根证书
//        TBSCertificateStructure tbsCert = createTbsCert(certValidity, issuer, subject, pubKey, pubKey);
//
//        // 签名算法
//        AlgorithmIdentifier alg = tbsCert.getSignature();
//
//        // 对证书主题进行签名
//        byte[] signData = sign(pubKey.getAlgorithm(), tbsCert.getEncoded(), priKey);
//
//        // 构建证书
//        ASN1EncodableVector asn1Vector = new ASN1EncodableVector();
//        asn1Vector.add(tbsCert.getDERObject());
//        asn1Vector.add(alg.getDERObject());
//        asn1Vector.add(new DERBitString(signData));
//        X509CertificateObject cert = new X509CertificateObject(new X509CertificateStructure(new DERSequence(asn1Vector)));
//
//        // 打印证书的base64编码
//        System.out.println("certBuf:"
//            + new String(Base64.encode(cert.getEncoded())));
//        cert.verify(cert.getPublicKey()); // 验证签名,无异常验签通过
//
//        FileOutputStream fos = new FileOutputStream("cert.cer");
//        fos.write(cert.getEncoded());
//        fos.flush();
//        fos.close();
//    }
//
//    // 创建证书
//    public static TBSCertificateStructure createTbsCert(int certValidity,
//        X500Name IssuerName, X500Name subjectName, PublicKey certPubKey,
//        PublicKey issuerKey) throws Exception {
//        // 证书有效期
//        Date notBefore = new Date();
//        long validity = certValidity * 1000 * 60 * 60 * 24;
//        Date notAfter = new Date(notBefore.getTime() + validity);
//
//        // 证书公钥信息
//        SubjectPublicKeyInfo pubInfo = SubjectPublicKeyInfo.getInstance(certPubKey.getEncoded());
//
//        // 组装证书主体
//        V3TBSCertificateGenerator genCert = new V3TBSCertificateGenerator();
//        genCert.setStartDate(new DERUTCTime(notBefore));
//        genCert.setSubject(new X500Name("CN=root,O=O,OU=OU"));
//        genCert.setIssuer(new X500Name("CN=root,O=O,OU=OU"));
//        genCert.setEndDate(new DERUTCTime(notAfter));
//        genCert.setSerialNumber(new DERInteger((int) System.currentTimeMillis()));
//        genCert.setSubjectPublicKeyInfo(pubInfo);
//        genCert.setSignature(new AlgorithmIdentifier("1.2.840.113549.1.1.5")); // SHA1withRSA
//        X509Extensions exts = genCertExtensions(issuerKey, certPubKey); // 创建证书扩展项
//        genCert.setExtensions(exts);
//
//        return genCert.generateTBSCertificate();
//    }
//
//    @SuppressWarnings("deprecation")
//    public static X509Extensions genCertExtensions(PublicKey issuerKey,
//        PublicKey subjectKey) throws Exception {
//        X509ExtensionsGenerator extGen = new X509ExtensionsGenerator();
//
//        /**
//         * 基本用途限制
//         * 
//         * BasicConstraints := SEQUENCE { cA BOOLEAN DEFAULT FALSE, 是否是CA证书
//         * pathLenConstraint INTEGER (0..MAX) OPTIONAL 证书链长度约束 }
//         */
//        BasicConstraints basicConstraints = new BasicConstraints(false, 0);
//        extGen.addExtension(X509Extensions.BasicConstraints, true, basicConstraints);
//
//        /**
//         * 密钥用法 The KeyUsage object.
//         * 
//         *  id-ce-keyUsage OBJECT IDENTIFIER ::=  { id-ce 15 }
//         * 
//         *  KeyUsage ::= BIT STRING {
//         *   digitalSignature (0), 数据验签：除了签发证书/签发CRL之外的各种数字签名操作，
//         *     数据完整性、身份鉴别、数据源鉴别。检查算法可以做签名  digitalSignature位被断
//         *     言，当主题公开密钥用一数字的签名算法来支持安全 服务而非抗抵赖性（位1）、签名
//         *     证书（位5）或者签名撤销信息（位6） 的时候。数字的签名算法常常为实体和数据起源
//         *     （做）完整性验证。
//         *     
//         *   nonRepudiation   (1), 不可抵赖性:证书对应的私钥，用于生成非否认的证据，证书
//         *      用于验证非否认证据。
//         *      
//         *   keyEncipherment  (2), 密钥加密：用于加密传输其他的密钥。检查可以加密密钥
//         *        keyEncipherment位被断言，当主题公开密钥被用于密钥传输的时候。例如，当一
//         *        RSA密钥用于密钥管理时候，那么这位将被断言。
//         *        
//         *   dataEncipherment (3), 数据加密：用于直接加解密应用数据，
//         *     通常都是公钥->对称密钥->应用数据。一般很少用这种方式的应用，因为：在密钥长度
//         *     安全的情况下，公钥密钥计算都是慢于对称密钥计算。检查可以加密数据  当主题公开密
//         *     钥用于（除了密码学的密钥）将用户数据加密使用的时候，dataEncipherment位被断言。
//         *   keyAgreement     (4), 密钥协商:在通信方之间协商对称密钥,例如：TLS、
//         *      Diffie-Hellman的密钥协商。不同于keyEncipherment. KeyEncipherment是直接对
//         *      Session Key进行加密  KeyAgreement是协商，别公钥加密的数据并不是直接作为密钥，
//         *      而是经过了一个多次步骤的过程，再导出Session Key。
//         *      
//         *   keyCertSign      (5), 签发证书:用于签发CA证书。 keyAgreement位被断言，当主题
//         *       公开密钥为用于密钥协议的时候。例如，当一Diffie Hellman密钥是要为密钥管理被使
//         *       用的时候，那么这位将被断言。
//         *     
//         *   cRLSign          (6), 签发crl：签发CRL，CA或者CRL Issuer
//         *     
//         *   encipherOnly     (7), 证书公钥在密钥协商过程中，仅仅进行加密计算，配合
//         *       KeyAgreement用法才有意义
//         *     
//         *   decipherOnly     (8) }证书公钥在密钥协商过程中，仅仅进行解密计算，配合
//         *       KeyAgreement用法才有意义
//         *  }
//         * 
//         */
//
//        int usage = KeyUsage.digitalSignature;
//        usage += KeyUsage.nonRepudiation;
//        usage += KeyUsage.keyEncipherment;
//        usage += KeyUsage.dataEncipherment;
//        usage += KeyUsage.keyAgreement;
//        usage += KeyUsage.keyCertSign;
//        usage += KeyUsage.cRLSign;
//        usage += KeyUsage.encipherOnly;
//        usage += KeyUsage.decipherOnly;
//
//        KeyUsage keyUsage = new KeyUsage(usage);
//
//        extGen.addExtension(X509Extensions.KeyUsage, true, keyUsage);
//
//        /**
//         * 增强型密钥用法 The extendedKeyUsage object.
//         * 
//         * <pre>
//         *      extendedKeyUsage ::= SEQUENCE SIZE (1..MAX) OF KeyPurposeId
//         * </pre>
//         */
//        ASN1EncodableVector asn1ExtKeyUsage = new ASN1EncodableVector();
//        asn1ExtKeyUsage.add(KeyPurposeId.anyExtendedKeyUsage); // 任何用途
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_serverAuth); // SSL的服务器认证
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_clientAuth); // SSL的客户端认证
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_codeSigning); // 代码签名
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_emailProtection); // 电子邮件的加解密、签名等
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_ipsecEndSystem); //
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_ipsecTunnel); //
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_ipsecUser); //
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_timeStamping); // 时间戳 认证
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_OCSPSigning); // ocsp证书认证
//        /*
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_dvcs);
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_sbgpCertAAServerAuth);
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_scvp_responder);
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_eapOverPPP);
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_eapOverLAN);
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_scvpServer);
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_scvpClient);
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_ipsecIKE);
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_capwapAC);
//         * asn1ExtKeyUsage.add(KeyPurposeId.id_kp_capwapWTP);
//         */
//        asn1ExtKeyUsage.add(KeyPurposeId.id_kp_smartcardlogon);
//
//        ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(new DERSequence(asn1ExtKeyUsage));
//
//        extGen.addExtension(X509Extensions.ExtendedKeyUsage, true, extendedKeyUsage);
//
//        /**
//         * 证书撤销：已证书扩展的形式，给出了“检查本证书所需要的CRL文件，到上面地方获取” CRL
//         * DP中的信息是有多个DisributionPoint组成：每个DisributionPoint都存放CRL，
//         * CA可以在多个地方存放CRL
//         * 
//         * crl Produce an object suitable for an ASN1OutputStream.
//         * 
//         * <pre>
//         * CRLDistPoint ::= SEQUENCE SIZE {1..MAX} OF DistributionPoint
//         * </pre>
//         * 
//         * -- CRL distribution points extension OID and syntax
//         * 
//         * id-ce-cRLDistributionPoints OBJECT IDENTIFIER ::= {id-ce 31}
//         * 
//         * CRLDistributionPoints ::= SEQUENCE SIZE (1..MAX) OF DistributionPoint
//         * 
//         * DistributionPoint ::= SEQUENCE { distributionPoint [0]
//         * DistributionPointName OPTIONAL, reasons [1] ReasonFlags OPTIONAL,
//         * cRLIssuer [2] GeneralNames OPTIONAL }
//         * 
//         * DistributionPointName ::= CHOICE { fullName [0] GeneralNames,
//         * nameRelativeToCRLIssuer [1] RelativeDistinguishedName }
//         */
//        Map<Integer, String> map = new HashMap<Integer, String>();
//        // map.put(GeneralName.otherName, "cn=otherName");
//        map.put(GeneralName.rfc822Name, "cn=rfc822Name");
//        map.put(GeneralName.dNSName, "192.168.30.241");
//        // map.put(GeneralName.x400Address, "cn=x400Address");
//        map.put(GeneralName.directoryName, "cn=root");
//        // map.put(GeneralName.ediPartyName, "cn=ediPartyName");
//        map.put(GeneralName.uniformResourceIdentifier, "http://certService/crl");
//        map.put(GeneralName.iPAddress, "192.168.30.24");
//        map.put(GeneralName.registeredID, "1.1.0.2.1");
//
//        DistributionPoint[] dps = new DistributionPoint[map.size()];
//        int i = 0;
//        for (int key : map.keySet()) {
//            GeneralName gn = null;
//            if (key == GeneralName.otherName || key == GeneralName.ediPartyName) {
//                continue;
//            } else if (key == GeneralName.x400Address) {
//                continue;
//            } else {
//                gn = new GeneralName(key, map.get(key));
//            }
//
//            GeneralNames gns = new GeneralNames(gn);
//            DistributionPointName dpn = new DistributionPointName(gns);
//            DistributionPoint dp = new DistributionPoint(dpn, null, null);
//            dps[i] = dp;
//            i++;
//        }
//
//        CRLDistPoint crlDistPoint = new CRLDistPoint(dps);
//
//        extGen.addExtension(X509Extensions.CRLDistributionPoints, true, crlDistPoint);
//
//        /**
//         * 增量crl Produce an object suitable for an ASN1OutputStream.
//         * 
//         * <pre>
//         * CRLDistPoint ::= SEQUENCE SIZE {1..MAX} OF DistributionPoint
//         * </pre>
//         */
//
//        extGen.addExtension(X509Extensions.FreshestCRL, false, crlDistPoint);
//        /**
//         * 主题备用名称
//         */
//        GeneralName subjectAlternativeName = new GeneralName(GeneralName.directoryName, map.get(GeneralName.directoryName));
//        extGen.addExtension(X509Extensions.SubjectAlternativeName, false, new DERSequence(subjectAlternativeName));
//
//        /**
//         * 颁发者备用名称 ： 放置签发者的各种不同的命名，map 是各种名称形式
//         */
//        GeneralName issuerAlternativeName = new GeneralName(GeneralName.directoryName, map.get(GeneralName.directoryName));
//        extGen.addExtension(X509Extensions.IssuerAlternativeName, false, new DERSequence(issuerAlternativeName));
//
//        /**
//         * 密钥周期 ： 对应私钥的使用期限
//         * 
//         * <pre>
//         *    PrivateKeyUsagePeriod ::= SEQUENCE {
//         *      notBefore       [0]     GeneralizedTime OPTIONAL,
//         *      notAfter        [1]     GeneralizedTime OPTIONAL }
//         * </pre>
//         */
//        Date notAfter = new Date();
//        Date notBefter = new Date();
//        notBefter.setYear(notAfter.getYear() + 10);
//        DERGeneralizedTime notAfterKey = new DERGeneralizedTime(notAfter);
//        DERGeneralizedTime notBefterKey = new DERGeneralizedTime(notBefter);
//
//        DERTaggedObject dtoNotBefterKey = new DERTaggedObject(false, 0, notBefterKey);
//        DERTaggedObject dtoNotAfterKey = new DERTaggedObject(false, 1, notAfterKey);
//
//        ASN1EncodableVector aevPriKeyUsagePeriod = new ASN1EncodableVector();
//        aevPriKeyUsagePeriod.add(dtoNotBefterKey);
//        aevPriKeyUsagePeriod.add(dtoNotAfterKey);
//        PrivateKeyUsagePeriod pkup = PrivateKeyUsagePeriod.getInstance(new DERSequence(aevPriKeyUsagePeriod));
//        extGen.addExtension(X509Extensions.PrivateKeyUsagePeriod, false, pkup);
//
//        /**
//         * 策略限制 PolicyConstraints ::= SEQUENCE { requireExplicitPolicy [0]
//         * SkipCerts OPTIONAL, inhibitPolicyMapping [1] SkipCerts OPTIONAL }
//         */
//
//        int requireExplicitPolicy = 10; // 表明额外的证书的数量
//        int inhibitPolicyMapping = 10; // 应用程序支持数量
//        ASN1EncodableVector pcVector = new ASN1EncodableVector();
//        pcVector.add(new DERTaggedObject(false, 0, new DERInteger(requireExplicitPolicy)));
//        pcVector.add(new DERTaggedObject(false, 1, new DERInteger(inhibitPolicyMapping)));
//
//        extGen.addExtension(X509Extensions.PolicyConstraints, false, new DERSequence(pcVector));
//
//        /**
//         * 禁止任何策略：
//         * 扩展项的值是整数N,N表示：在证书路径中，本证书之下的N个证书可带有Any-Policy的证书
//         * （N+1之下的证书就不能有Any-policy）
//         * 
//         * id-ce-inhibitAnyPolicy OBJECT IDENTIFIER ::= { id-ce 54 }
//         * 
//         * InhibitAnyPolicy ::= SkipCerts
//         * 
//         * SkipCerts ::= INTEGER (0..MAX)
//         */
//        int inhibitAnyPolicy = 10;
//        extGen.addExtension(X509Extensions.InhibitAnyPolicy, false, new DERInteger(inhibitAnyPolicy));
//
//        /**
//         * 策略映射 扩展项仅仅存在于交叉证书中，说明了不同CA域之间的CP等级的相互映射关系
//         * 
//         * PolicyMappings ::= SEQUENCE SIZE (1..MAX) OF SEQUENCE {
//         * issuerDomainPolicy CertPolicyId, subjectDomainPolicy CertPolicyId }
//         */
//        Hashtable<String, String> policyHashMap = new Hashtable<String, String>();
//        policyHashMap.put("1.1.1.2.3.1", "1.1.1.2.3.4");
//        policyHashMap.put("1.1.1.2.3.2", "1.1.1.2.3.5");
//
//        PolicyMappings pms = new PolicyMappings(policyHashMap);
//
//        extGen.addExtension(X509Extensions.PolicyMappings, false, pms);
//
//        /**
//         * 使用者密钥标示符 SubjectKeyIdentifier ::= KeyIdentifier
//         */
//        extGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifier(SubjectPublicKeyInfo.getInstance(subjectKey.getEncoded())));
//        /**
//         * 颁发者密钥标示符 IssuerKeyIdentifier ::= KeyIdentifier
//         */
//
//        extGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifier(SubjectPublicKeyInfo.getInstance(subjectKey.getEncoded())));
//        /**
//         * 主题目录属性 原则上可以加入与Subject有关信息 因为使用了Atrribute Type的OID的、然后说明相对应的值
//         */
//        // http://asn1.elibel.tm.fr/cgi-bin/oid/display?oid=1.3.6.1.5.5.7.9&action=display
//        // PKIX personal data gender
//        String genderOidStr = "1.3.6.1.5.5.7.9.4";
//
//        // PKIX personal data dateOfBirth
//        String dateOfBirthOidStr = "1.3.6.1.5.5.7.9.1";
//
//        // 2.5.4.20 - id-at-telephoneNumber
//        // http://www.alvestrand.no/objectid/2.5.4.html
//        String streetAddressOidStr = "2.5.4.9";
//
//        String telephoneNumberOidStr = "2.5.4.20";
//        // http://oid.elibel.tm.fr/0.9.2342.19200300.100.1.41
//        String mobileTelephoneNumberOidStr = "0.9.2342.19200300.100.1.41";
//
//        Vector<Attribute> attributes = new Vector<Attribute>();
//
//        Attribute genderAttribute = new Attribute(new DERObjectIdentifier(genderOidStr), new DERSet(new DERPrintableString("汉族".getBytes("UTF-8"))));
//        Attribute dateOfBirthAttribute =
//            new Attribute(new DERObjectIdentifier(genderOidStr), new DERSet(new DERPrintableString("1992-02-20".getBytes("UTF-8"))));
//        Attribute streetAddressAttribute =
//            new Attribute(new DERObjectIdentifier(genderOidStr), new DERSet(new DERPrintableString("北京市大王庄胡同13号".getBytes("UTF-8"))));
//        Attribute telephoneNumberAttribute =
//            new Attribute(new DERObjectIdentifier(genderOidStr), new DERSet(new DERPrintableString("010-82961368".getBytes("UTF-8"))));
//        Attribute mobileTelephoneNumberAttribute =
//            new Attribute(new DERObjectIdentifier(genderOidStr), new DERSet(new DERPrintableString("13843838438".getBytes("UTF-8"))));
//
//        attributes.add(genderAttribute);
//        attributes.add(dateOfBirthAttribute);
//        attributes.add(streetAddressAttribute);
//        attributes.add(telephoneNumberAttribute);
//        attributes.add(mobileTelephoneNumberAttribute);
//
//        // 构建主题目录属性
//        SubjectDirectoryAttributes sda = new SubjectDirectoryAttributes(attributes);
//
//        extGen.addExtension(X509Extensions.SubjectDirectoryAttributes, false, sda);
//
//        /**
//         * 名称限制 ： 只在ca中出现，并不是出现在用户证书中，名称限制同时对Subject和SubjeectAltertiveName
//         * 起作用。可以对多种命名进行限制如Email、DNS、X509 DN 等
//         * 如果发现用户证书中的命名（Subject和SubjeectAltertiveName）与CA证书中的Name
//         * Constraints违背，就直接认为该证书无效。必须满足Name Constraints 中的多个不同类型命名的限制
//         * NameConstraints ::= SEQUENCE { permittedSubtrees [0] GeneralSubtrees
//         * OPTIONAL, excludedSubtrees [1] GeneralSubtrees OPTIONAL }
//         * GeneralSubtrees ::= SEQUENCE SIZE (1..MAX) OF GeneralSubtree
//         */
//        Vector<GeneralSubtree> permitted = new Vector<GeneralSubtree>(); // 允许名称列表
//        Vector<GeneralSubtree> excluded = new Vector<GeneralSubtree>(); // 限制名称列表
//
//        // 添加允许名称
//        GeneralName permitteedNcGn = new GeneralName(GeneralName.directoryName, map.get(GeneralName.directoryName));
//        GeneralSubtree permittedGsNcGn = new GeneralSubtree(permitteedNcGn, BigInteger.ONE, BigInteger.TEN);
//        permitted.add(permittedGsNcGn);
//        // 添加限制名称
//        GeneralName excludedNcGn = new GeneralName(GeneralName.directoryName, map.get(GeneralName.directoryName));
//        GeneralSubtree excludedGsNcGn = new GeneralSubtree(excludedNcGn, BigInteger.ONE, BigInteger.TEN);
//        excluded.add(excludedGsNcGn);
//
//        NameConstraints nc = new NameConstraints(permitted, excluded);
//
//        extGen.addExtension(X509Extensions.NameConstraints, false, nc);
//
//        /**
//         * 机构信息访问 id-pe-authorityInfoAccess OBJECT IDENTIFIER ::= { id-pe 1 }
//         * AuthorityInfoAccessSyntax ::= SEQUENCE SIZE (1..MAX) OF
//         * AccessDescription AccessDescription ::= SEQUENCE { accessMethod
//         * OBJECT IDENTIFIER, accessLocation GeneralName } id-ad OBJECT
//         * IDENTIFIER ::= { id-pkix 48 } id-ad-caIssuers OBJECT IDENTIFIER ::= {
//         * id-ad 2 } id-ad-ocsp OBJECT IDENTIFIER ::= { id-ad 1 }
//         */
//        ASN1EncodableVector authorityInnfoAccess = new ASN1EncodableVector();
//
//        DERObjectIdentifier id_ad_caIssuers = AccessDescription.id_ad_caIssuers;
//        DERObjectIdentifier id_ad_ocsp = AccessDescription.id_ad_ocsp;
//        DERObjectIdentifier id_ad_caRepository = new DERObjectIdentifier("1.3.6.1.5.5.7.48.5");
//
//        AccessDescription caIssuers =
//            new AccessDescription(id_ad_caIssuers, new GeneralName(GeneralName.uniformResourceIdentifier, "http://certService/caIssuers"));
//        AccessDescription ocsp = new AccessDescription(id_ad_caIssuers, new GeneralName(GeneralName.uniformResourceIdentifier, "http://certService/ocsp"));
//        AccessDescription caRepository =
//            new AccessDescription(id_ad_caIssuers, new GeneralName(GeneralName.uniformResourceIdentifier, "http://certService/caRepository"));
//
//        authorityInnfoAccess.add(caIssuers);
//        authorityInnfoAccess.add(ocsp);
//        authorityInnfoAccess.add(caRepository);
//
//        extGen.addExtension(X509Extensions.AuthorityInfoAccess, false, new DERSequence(authorityInnfoAccess));
//        /**
//         * 
//         */
//
//        /**
//         * 证书策略
//         * 
//         * <pre>
//         * 
//         * certificatePolicies ::= SEQUENCE SIZE (1..MAX) OF PolicyInformation
//         * 
//         * PolicyInformation ::= SEQUENCE {
//         *   policyIdentifier   CertPolicyId,
//         *   policyQualifiers   SEQUENCE SIZE (1..MAX) OF
//         *                           PolicyQualifierInfo OPTIONAL }
//         * 
//         * CertPolicyId ::= OBJECT IDENTIFIER
//         * 
//         * PolicyQualifierInfo ::= SEQUENCE {
//         *   policyQualifierId  PolicyQualifierId,
//         *   qualifier          ANY DEFINED BY policyQualifierId }
//         * 
//         * PolicyQualifierId ::=
//         *   OBJECT IDENTIFIER (id-qt-cps | id-qt-unotice)
//         * </pre>
//         * 
//         * @deprecated use an ASN1Sequence of PolicyInformation
//         */
//        PolicyInformation policyInfo1 = new PolicyInformation(new DERObjectIdentifier("1.1.1.2.3.1"));
//        PolicyInformation policyInfo2 = new PolicyInformation(new DERObjectIdentifier("1.1.1.2.3.2"));
//        PolicyInformation policyInfo3 = new PolicyInformation(new DERObjectIdentifier("1.1.1.2.3.3"));
//
//        ASN1EncodableVector certificatePolicies = new ASN1EncodableVector();
//        certificatePolicies.add(policyInfo1);
//        certificatePolicies.add(policyInfo2);
//        certificatePolicies.add(policyInfo3);
//
//        extGen.addExtension(X509Extensions.CertificatePolicies, true, new DERSequence(certificatePolicies));
//
//        return extGen.generate();
//    }
//
//    /**
//     * 产生密钥对
//     * @param alg  签名算法
//     * @return  密钥对
//     * @throws Exception
//     */
//    public static KeyPair genKeyPair(String alg) throws Exception {
//        KeyPairGenerator genKeyPair = KeyPairGenerator.getInstance(alg);
//        genKeyPair.initialize(2048);
//        return genKeyPair.genKeyPair();
//    }
//
//    /**
//     * 签名 
//     * @param alg  签名算法 
//     * @param planText 签名原文
//     * @param priKey   签名私钥
//     * @return   签名信息  byte[]
//     * @throws Exception
//     */
//    public static byte[] sign(String alg, byte[] planText, PrivateKey priKey)
//        throws Exception {
//        if (alg == null) return null;
//        if ("RSA".equals(alg)) alg = "SHA1withRSA";
//        else throw new Exception("不支持的算法。");
//
//        Signature sign = Signature.getInstance(alg);
//        sign.initSign(priKey);
//        sign.update(planText);
//        return sign.sign();
//    }
//
//}
