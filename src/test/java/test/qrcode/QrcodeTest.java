package test.qrcode;

/**
 *QRcodeクラスライブラリ脱sample
 *
 *妈办苞眶をデ〖タとしたQRcodeを
 *テキストで叫蜗します
 */
public class QrcodeTest {
    public static void main(String[] args) {

        Qrcode x = new Qrcode();
        x.setQrcodeErrorCorrect('M'); //エラ〖柠赖レベルM
        x.setQrcodeEncodeMode('B'); //8bit byte モ〖ド
        boolean[][] matrix = x.calQrcode(args[0].getBytes());

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[j][i]) {
                    System.out.print("@");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.print("\n");
        }

    }
}
