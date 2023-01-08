/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * 扩展自GZIPOutputStream的giz压缩输出流
 * @author Ponfee
 */
public class ExtendedGZIPOutputStream extends GZIPOutputStream {

    public ExtendedGZIPOutputStream(OutputStream out) throws IOException {
        this(out, Deflater.DEFAULT_COMPRESSION);
    }

    public ExtendedGZIPOutputStream(OutputStream out, int level) throws IOException {
        super(out);
        super.def.setLevel(level);
    }
}
