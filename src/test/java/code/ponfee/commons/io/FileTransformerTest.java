package code.ponfee.commons.io;

import code.ponfee.commons.util.MavenProjects;

public class FileTransformerTest {

    public static void main(String[] args) {
        //System.out.println(detectBytesCharset(Streams.file2bytes("D:\\test\\2.png")));
        //System.out.println(detectBytesCharset(Streams.file2bytes("D:\\test\\lib\\cache\\Cache.java")));

        FileTransformer transformer = new FileTransformer(MavenProjects.getMainJavaPath(""), "/Users/ponfee/test", "GBK");
        transformer.transform();
        System.out.println(transformer.getTransformLog());
        
        
        /*FileTransformer t = new FileTransformer("D:\\test\\code", "D:\\test\\target", "UTF-8");
        t.setReplaceEach(new String[] {"code.ponfee.commons."}, new String[] {"com.sf.ddt.cache."});
        t.transform();*/
    }
}
