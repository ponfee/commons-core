/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io.charset;

import cn.ponfee.commons.io.CharsetDetector;
import cn.ponfee.commons.io.Files;

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
