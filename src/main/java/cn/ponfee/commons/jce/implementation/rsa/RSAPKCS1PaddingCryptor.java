/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.implementation.rsa;

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
