package cn.ponfee.commons.io;

import cn.ponfee.commons.resource.ResourceLoaderFacade;
import cn.ponfee.commons.util.MavenProjects;
import cn.ponfee.commons.util.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class FileTransformerTest {

    public static void main(String[] args) {
        //System.out.println(detectBytesCharset(Streams.file2bytes("D:\\test\\2.png")));
        //System.out.println(detectBytesCharset(Streams.file2bytes("D:\\test\\lib\\cache\\Cache.java")));

        /*FileTransformer transformer = new FileTransformer(MavenProjects.getMainJavaPath(""), "/Users/ponfee/test", "GBK");
        transformer.transform();
        System.out.println(transformer.getTransformLog());*/

        FileTransformer t = new FileTransformer("/Users/ponfee/scm/github/commons-core", "/Users/ponfee/scm/github/test111/commons-core");
        t.setReplaceEach(new String[] {"cn.ponfee.commons."}, new String[] {"cn.ponfee.commons."});
        t.transform();
    }

    @Test
    public void testAdd() throws IOException {
        String banner = IOUtils.toString(ResourceLoaderFacade.getResource("test.txt").getStream(), StandardCharsets.UTF_8);
        FileUtils.listFiles(new File("/Users/ponfee/scm/github/commons-core/src/main/java/code/ponfee/commons"), new String[]{"java"}, true)
            .forEach(e -> {
                try {
                    String text = IOUtils.toString(e.toURI(), StandardCharsets.UTF_8);
                    if (text.contains("@author Ponfee") && Strings.count(text, "@author") == 1) {
                        Writer writer = new FileWriter(e.getAbsolutePath());
                        IOUtils.write(banner, writer);
                        IOUtils.write(text, writer);
                        writer.flush();
                        writer.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
    }

    @Test
    public void testModify() throws IOException {
        String banner = IOUtils.toString(ResourceLoaderFacade.getResource("test.txt").getStream(), StandardCharsets.UTF_8);
        FileUtils.listFiles(new File("/Users/ponfee/scm/github/commons-core/src/main/java/code/ponfee/commons"), new String[]{"java"}, true)
            .forEach(e -> {
                try {
                    String text = IOUtils.toString(e.toURI(), StandardCharsets.UTF_8);
                    if (text.contains("@author Ponfee") && Strings.count(text, "@author") == 1) {
                        Writer writer = new FileWriter(e.getAbsolutePath());
                        IOUtils.write(banner, writer);
                        IOUtils.write(text.substring(84 * 7 + 1), writer);
                        writer.flush();
                        writer.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
    }

    @Test
    public void testCheck() throws IOException {
        String banner = IOUtils.toString(ResourceLoaderFacade.getResource("test.txt").getStream(), StandardCharsets.UTF_8);
        System.out.println(banner);
        FileUtils.listFiles(new File("/Users/ponfee/scm/gitee/distributed-scheduler"), new String[]{"java"}, true)
            .forEach(e -> {
                try {
                    String text = IOUtils.toString(e.toURI(), StandardCharsets.UTF_8);
                    if (!text.contains("@author Ponfee") || Strings.count(text, "@author") > 1) {
                        System.out.println(e.getAbsolutePath());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
    }

    @Test
    public void testCount() {
        System.out.println(Strings.count("abababababa", "aba"));
    }

}
