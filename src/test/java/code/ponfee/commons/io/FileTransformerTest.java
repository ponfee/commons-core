package code.ponfee.commons.io;

public class FileTransformerTest {

    public static void main(String[] args) {
        //System.out.println(detectBytesCharset(Streams.file2bytes("D:\\test\\2.png")));
        //System.out.println(detectBytesCharset(Streams.file2bytes("D:\\test\\lib\\cache\\Cache.java")));

        FileTransformer transformer = new FileTransformer("D:\\test\\framework", "d:\\test\\framework2", "UTF-8");
        transformer.setReplaceEach(new String[] { "cn.ponfee.web.framework." }, new String[] { "com.sf.framework." });
        transformer.transform();
        System.out.println(transformer.getTransformLog());
    }
}
