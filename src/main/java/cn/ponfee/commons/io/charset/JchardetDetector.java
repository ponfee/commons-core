/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

//package cn.ponfee.commons.io.charset;
//
//import cn.ponfee.commons.io.CharsetDetector;
//import org.apache.commons.lang3.StringUtils;
//import org.mozilla.intl.chardet.nsDetector;
//import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
//
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.Charset;
//import java.util.Arrays;
//
///**
// * <pre>
// *  <!-- https://nexus.nuiton.org/nexus/content/repositories/central/ -->
// *  <dependency>
// *    <groupId>net.sourceforge.jchardet</groupId>
// *    <artifactId>jchardet</artifactId>
// *    <version>1.0</version>
// *  </dependency>
// * </pre>
// *
// * @author Ponfee
// */
//public class JchardetDetector {
//
//    public static Charset detect(InputStream input, int length) throws IOException {
//        nsDetector detector = new nsDetector(nsDetector.ALL);
//        DetectorObserver observer = new DetectorObserver();
//        detector.Init(observer);
//        try (BufferedInputStream bufInput = new BufferedInputStream(input)) {
//            byte[] buf = new byte[length];
//            boolean isAscii = true;
//            int len, count = 0;
//            while ((len = bufInput.read(buf, 0, buf.length)) != -1) {
//                if (isAscii) {
//                    isAscii = detector.isAscii(buf, len);
//                }
//                if (!isAscii && detector.DoIt(buf, len, false)) {
//                    break;
//                }
//                count += len;
//                if (count >= length) {
//                    break;
//                }
//            }
//            detector.DataEnd();
//
//            if (isAscii) {
//                return CharsetDetector.DEFAULT_CHARSET;
//            } else if (observer.result != null) {
//                return Charset.forName(observer.result);
//            } else {
//                String[] probableCharsets = detector.getProbableCharsets();
//                String probableCharset = Arrays.stream(probableCharsets)
//                    .filter(s -> !StringUtils.startsWithAny(s, "UTF-16", "UTF-32", "GB18030"))
//                    .findAny()
//                    .orElse(probableCharsets[0]);
//                return Charset.forName(probableCharset);
//            }
//        }
//    }
//
//    private static class DetectorObserver implements nsICharsetDetectionObserver {
//        private String result = null;
//
//        @Override
//        public void Notify(String charset) {
//            this.result = charset;
//        }
//    }
//
//}
