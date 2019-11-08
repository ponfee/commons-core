package code.ponfee.commons.io;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class FilesTest {

    @Test
    public void test1() throws MalformedURLException {
        System.out.println("fbdp.xml -> "+CharacterEncodingDetector.detect("D:/temp/fbdp.xml"));
        System.out.println("fbdp3.xml -> "+CharacterEncodingDetector.detect("D:/temp/fbdp3.xml"));
        System.out.println("GBK.properties -> "+CharacterEncodingDetector.detect("D:/temp/GBK.properties"));
        System.out.println("UTF8.txt -> "+CharacterEncodingDetector.detect("D:/temp/UTF8.txt"));
        System.out.println("UTF8-WITH-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF8-WITH-BOM.xml"));
        System.out.println("UTF8-WITHOUT-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF8-WITHOUT-BOM.xml"));
        System.out.println("UTF16-BIG-ENDIAN-WITH-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF16-BIG-ENDIAN-WITH-BOM.xml"));
        System.out.println("UTF16-BIG-ENDIAN-WITHOUT-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF16-BIG-ENDIAN-WITHOUT-BOM.xml"));
        System.out.println("UTF16-WITH-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF16-WITH-BOM.xml"));
        System.out.println("UTF16-WITHOUT-BOM.xml -> "+CharacterEncodingDetector.detect("D:/temp/UTF16-WITHOUT-BOM.xml"));
    }

}
