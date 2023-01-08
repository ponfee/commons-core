package test.jce.ecc2;


import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;

public class CurveParameters {

    /**
     * Creates an ECDomainParameters from a named curve.
     * @param curveName The name of the curve to use
     */
    public static ECDomainParameters getCurveParametersByName(String curveName) {
        X9ECParameters x9ECParameters = SECNamedCurves.getByName(curveName);
        return new ECDomainParameters(
                x9ECParameters.getCurve(),
                x9ECParameters.getG(),
                x9ECParameters.getN(),
                x9ECParameters.getH());
    }

    public static final ECDomainParameters secp256k1 = getCurveParametersByName("secp256k1");
    public static final ECDomainParameters secp256r1 = getCurveParametersByName("secp256r1");
}
