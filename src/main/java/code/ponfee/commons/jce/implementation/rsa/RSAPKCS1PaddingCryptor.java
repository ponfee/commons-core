package code.ponfee.commons.jce.implementation.rsa;

/**
 * RSA crypto with PKCS1 padding
 * 
 * @author Ponfee
 */
public class RSAPKCS1PaddingCryptor extends AbstractRSACryptor {

    public RSAPKCS1PaddingCryptor() {
        super(true);
    }

    @Override
    public int getOriginBlockSize(RSAKey rsaKey) {
        return rsaKey.n.bitLength() / 8 - 11;
    }

}
