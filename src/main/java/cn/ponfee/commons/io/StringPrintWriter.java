/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 字符串输出类
 * @see java.io.StringWriter
 * @author Ponfee
 */
public class StringPrintWriter extends PrintWriter {

    public StringPrintWriter() {
        super(new StringWriter());
    }

    public StringPrintWriter(int initialSize) {
        super(new StringWriter(initialSize));
    }

    public String getString() {
        flush();
        return super.out.toString();
    }

    @Override
    public String toString() {
        return getString();
    }

}
