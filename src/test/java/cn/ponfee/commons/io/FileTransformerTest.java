package cn.ponfee.commons.io;

import org.junit.Test;

public class FileTransformerTest {

    @Test
    public void test() {
        //System.out.println(detectBytesCharset(Streams.file2bytes("D:\\test\\2.png")));
        //System.out.println(detectBytesCharset(Streams.file2bytes("D:\\test\\lib\\cache\\Cache.java")));

        /*FileTransformer transformer = new FileTransformer(MavenProjects.getMainJavaPath(""), "/Users/ponfee/test", "GBK");
        transformer.transform();
        System.out.println(transformer.getTransformLog());*/

        FileTransformer t = new FileTransformer("/Users/ponfee/scm/github/commons-core", "/Users/ponfee/scm/github/test111/commons-core");
        t.setReplaceEach(new String[]{"cn.ponfee.commons."}, new String[]{"cn.ponfee.commons."});
        t.transform();
    }

}
