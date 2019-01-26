package code.ponfee.commons.jce.implementation.rsa;

/**
 * RSA crypto without padding
 * 
 * @author Ponfee
 */
public class RSANoPaddingCryptor extends AbstractRSACryptor {

    public RSANoPaddingCryptor() {
        super(false);
    }

}
