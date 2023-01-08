package cn.ponfee.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import cn.ponfee.commons.util.MavenProjects;

public class BeforeReadInputStreamTest {

    @Test
    public void test() throws IOException {
        File f = MavenProjects.getTestJavaFile(BeforeReadInputStreamTest.class);
        //System.out.println(Files.toString(f));
        byte[] fb, bb;

        /*BeforeReadInputStream binput = new BeforeReadInputStream(new FileInputStream(f), 200);
        System.out.println(new String(binput.getArray()).replaceAll("\n|\r\n", ""));
        fb = IOUtils.toByteArray(new FileInputStream(f));
        bb = IOUtils.toByteArray(binput);
        Assert.assertArrayEquals(fb, bb);*/

        for (int i = 1, n = (int) f.length() + 500; i < n; i += 7) {
            try (InputStream input1 = new FileInputStream(f);
                 InputStream input2 = new PrereadInputStream(new FileInputStream(f), i)
            ){
                fb = IOUtils.toByteArray(input1);
                bb = IOUtils.toByteArray(input2);
                Assert.assertArrayEquals(fb, bb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
