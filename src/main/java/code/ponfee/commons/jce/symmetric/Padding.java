package code.ponfee.commons.jce.symmetric;

/**
 * encrypt padding
 * pkcs7Padding must be has BouncyCastleProvider support
 * PKCS7Padding：缺几个字节就补几个字节的0
 * PKCS5Padding：缺几个字节就补充几个字节的几，如缺6个字节就补充6个字节的6
 * 
 * @author Ponfee
 */
public enum Padding {

    NoPadding, PKCS5Padding, PKCS7Padding, // 
    ISO10126_Padding("ISO10126Padding"), //
    ISO10126_2Padding("ISO10126-2Padding"), // 
    ISO7816_4Padding("ISO7816-4Padding"), // 
    X9_23Padding("X9.23Padding"), TBCPadding, //
    CS1Padding, CS2Padding, CS3Padding // CS1Padding, CS2Padding may cannot support

    ;

    private final String padding;

    Padding() {
        this.padding = this.name();
    }

    Padding(String padding) {
        this.padding = padding;
    }

    public String padding() {
        return this.padding;
    }
}
