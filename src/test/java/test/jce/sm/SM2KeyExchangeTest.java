package test.jce.sm;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;

import cn.ponfee.commons.jce.ECParameters;
import cn.ponfee.commons.jce.sm.SM2;
import cn.ponfee.commons.jce.sm.SM2KeyExchanger;
import cn.ponfee.commons.jce.sm.SM2KeyExchanger.TransportEntity;

public class SM2KeyExchangeTest {

    public static void main(String[] args) {
        ECParameters ecParameter = ECParameters.SM2_BEST;

        System.out.println("=============================密钥协商============================");
        Map<String, byte[]> keyMap = SM2.generateKeyPair(ecParameter);
        byte[] id1 = "AAAAAAAAAAAAA".getBytes();
        SM2KeyExchanger aKeyExchange = new SM2KeyExchanger(id1, SM2.getPublicKey(ecParameter, SM2.getPublicKey(keyMap)), SM2.getPrivateKey(SM2.getPrivateKey(keyMap)));

        Map<String, byte[]> keyMap2 = SM2.generateKeyPair(ecParameter);
        byte[] id2 = "BBBBBBBBBBBBB".getBytes();
        SM2KeyExchanger bKeyExchange = new SM2KeyExchanger(id2, SM2.getPublicKey(ecParameter, SM2.getPublicKey(keyMap2)), SM2.getPrivateKey(SM2.getPrivateKey(keyMap2)));
        TransportEntity entity1 = aKeyExchange.step1PartA();
        TransportEntity entity2 = bKeyExchange.step2PartB(entity1);

        TransportEntity entity3 = aKeyExchange.step3PartA(entity2);
        System.out.println(Hex.encodeHexString(bKeyExchange.getKey()));

        if (!bKeyExchange.step4PartB(entity3)
            || !Arrays.equals(aKeyExchange.getKey(), bKeyExchange.getKey())) {
            System.err.println("FAIL!");
        }
        System.out.println(Hex.encodeHexString(aKeyExchange.getKey())); // 16 byte
    }
}
