//package code.ponfee.commons.io.charset;
//
//import code.ponfee.commons.io.CharsetDetector;
//import info.monitorenter.cpdetector.io.*;
//
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.Charset;
//
///**
// * <pre>
// *  <!-- https://nexus.nuiton.org/nexus/content/repositories/thirdparty/ -->
// *  <dependency>
// *    <groupId>net.sourceforge.cpdetector</groupId>
// *    <artifactId>cpdetector</artifactId>
// *    <version>1.0.7</version>
// *  </dependency>
// *
// *  <!-- 自动依赖了antlr:antlr:2.7.7，排除则会报错"java.lang.NoClassDefFoundError: antlr/ANTLRException" -->
// *  <dependency>
// *    <groupId>antlr</groupId>
// *    <artifactId>antlr</artifactId>
// *    <version>2.7.7</version>
// *  </dependency>
// * </pre>
// *
// * @author Ponfee
// */
//public class CodepageDetector {
//
//    public static Charset detect(InputStream input, int length) throws IOException {
//        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
//        detector.add(new ByteOrderMarkDetector());   // 通过BOM来测定编码
//        detector.add(JChardetFacade.getInstance());  // 封装了由Mozilla提供的JChardet
//        detector.add(UnicodeDetector.getInstance()); // 用于Unicode家族编码的测定
//        detector.add(ASCIIDetector.getInstance());   // 用于ASCII编码测定
//        detector.add(new ParsingDetector(false));    // 用于检查HTML、XML等文件或字符流的编码
//        input = input.markSupported() ? input : new BufferedInputStream(input, length);
//        String charset = detector.detectCodepage(input, length).name();
//        return "void".equalsIgnoreCase(charset) ? CharsetDetector.DEFAULT_CHARSET : Charset.forName(charset);
//    }
//
//}
