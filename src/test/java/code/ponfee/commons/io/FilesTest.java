package code.ponfee.commons.io;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class FilesTest {

    @Test
    public void test1() throws MalformedURLException {
        System.out.println("\n=========================test1");
        System.out.println(CharacterEncodingDetector.detect("D:/temp/fbdp.xml"));
        System.out.println(CharacterEncodingDetector.detect("D:/temp/fbdp3.xml"));
        System.out.println(CharacterEncodingDetector.detect("D:/temp/WrappedBufferedWriter.java"));
        System.out.println(CharacterEncodingDetector.detect("D:/temp/123.properties"));
        System.out.println(CharacterEncodingDetector.detect(new URL("https://www.baidu.com")));
    }

}
