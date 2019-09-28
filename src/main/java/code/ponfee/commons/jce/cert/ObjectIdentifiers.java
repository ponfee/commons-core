package code.ponfee.commons.jce.cert;

/**
 * 证书扩展信息
 * 
 * @author Ponfee
 * @see org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
 */
public final class ObjectIdentifiers {

    public static final int[] SubjectDirectoryAttributes = new int[] { 2, 5, 29, 9 };

    /** Subject Key Identifier */
    public static final int[] SubjectKeyIdentifier = new int[] { 2, 5, 29, 14 };

    /** Key Usage */
    public static final int[] KeyUsage = new int[] { 2, 5, 29, 15 };

    /** Private Key Usage Period 私钥使用周期 */
    public static final int[] PrivateKeyUsagePeriod = new int[] { 2, 5, 29, 16 };

    /** Subject Alternative Name */
    public static final int[] SubjectAlternativeName = new int[] { 2, 5, 29, 17 };

    /** Issuer Alternative Name */
    public static final int[] IssuerAlternativeName = new int[] { 2, 5, 29, 18 };

    /** Basic Constraints */
    public static final int[] BasicConstraints = new int[] { 2, 5, 29, 19 };

    /** CRL Number */
    public static final int[] CRLNumber = new int[] { 2, 5, 29, 20 };

    /** Reason code */
    public static final int[] ReasonCode = new int[] { 2, 5, 29, 21 };

    /** Hold Instruction Code */
    public static final int[] InstructionCode = new int[] { 2, 5, 29, 23 };

    /** Invalidity Date */
    public static final int[] InvalidityDate = new int[] { 2, 5, 29, 24 };

    /** Delta CRL indicator */
    public static final int[] DeltaCRLIndicator = new int[] { 2, 5, 29, 27 };

    /** Issuing Distribution Point */
    public static final int[] IssuingDistributionPoint = new int[] { 2, 5, 29, 28 };

    /** Certificate Issuer */
    public static final int[] CertificateIssuer = new int[] { 2, 5, 29, 29 };

    /** Name Constraints */
    public static final int[] NameConstraints = new int[] { 2, 5, 29, 30 };

    /** CRL Distribution Points */
    public static final int[] CRLDistributionPoints = new int[] { 2, 5, 29, 31 };

    /** Certificate Policies */
    public static final int[] CertificatePolicies = new int[] { 2, 5, 29, 32 };

    /** Policy Mappings */
    public static final int[] PolicyMappings = new int[] { 2, 5, 29, 33 };

    /** Authority Key Identifier */
    public static final int[] AuthorityKeyIdentifier = new int[] { 2, 5, 29, 35 };

    /** Policy Constraints */
    public static final int[] PolicyConstraints = new int[] { 2, 5, 29, 36 };

    /** Extended Key Usage */
    public static final int[] ExtendedKeyUsage = new int[] { 2, 5, 29, 37 };

    /** Freshest CRL */
    public static final int[] FreshestCRL = new int[] { 2, 5, 29, 46 };

    /** Inhibit Any Policy */
    public static final int[] InhibitAnyPolicy = new int[] { 2, 5, 29, 54 };

    /** Authority Info Access */
    public static final int[] AuthorityInfoAccess = new int[] { 1, 3, 6, 1, 5, 5, 7, 1, 1 };

    /** Subject Info Access */
    public static final int[] SubjectInfoAccess = new int[] { 1, 3, 6, 1, 5, 5, 7, 1, 11 };

    /** Logo Type */
    public static final int[] LogoType = new int[] { 1, 3, 6, 1, 5, 5, 7, 1, 12 };

    /** BiometricInfo */
    public static final int[] BiometricInfo = new int[] { 1, 3, 6, 1, 5, 5, 7, 1, 2 };

    /** QCStatements */
    public static final int[] QCStatements = new int[] { 1, 3, 6, 1, 5, 5, 7, 1, 3 };

    /** Audit identity extension in attribute certificates */
    public static final int[] AuditIdentity = new int[] { 1, 3, 6, 1, 5, 5, 7, 1, 4 };

    /** NoRevAvail extension in attribute certificates */
    public static final int[] NoRevAvail = new int[] { 2, 5, 29, 56 };

    /** TargetInformation extension in attribute certificates */
    public static final int[] TargetInformation = new int[] { 2, 5, 29, 55 };

}
