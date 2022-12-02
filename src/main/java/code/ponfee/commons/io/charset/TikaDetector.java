package code.ponfee.commons.io.charset;

import code.ponfee.commons.io.CharsetDetector;
import code.ponfee.commons.io.Files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * <pre>
 *  <!-- dependencyManagement -->
 *  <dependency>
 *    <groupId>org.apache.tika</groupId>
 *    <artifactId>tika-bom</artifactId>
 *    <version>2.6.0</version>
 *    <type>pom</type>
 *    <scope>import</scope>
 *  </dependency>
 *
 *  <!-- dependencies -->
 *  <dependency>
 *    <groupId>org.apache.tika</groupId>
 *    <artifactId>tika-parsers-standard-package</artifactId>
 *  </dependency>
 * </pre>
 *
 * @author Ponfee
 */
public class TikaDetector {

    public static Charset detect(InputStream input, int length) throws IOException {
        org.apache.tika.parser.txt.CharsetDetector charsetDetector = new org.apache.tika.parser.txt.CharsetDetector();
        charsetDetector.setText(Files.readByteArray(input, length));
        org.apache.tika.parser.txt.CharsetMatch charsetMatch = charsetDetector.detect();
        return charsetMatch == null ? CharsetDetector.DEFAULT_CHARSET : Charset.forName(charsetMatch.getName());
    }

}
