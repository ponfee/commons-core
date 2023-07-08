/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io;

import cn.ponfee.commons.tree.PlainNode;
import cn.ponfee.commons.tree.TreeNode;
import cn.ponfee.commons.tree.print.MultiwayTreePrinter;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件工具类
 * @author Ponfee
 */
public final class Files {

    public static final int EOF             = -1; // end of file read

    public static final int BUFF_SIZE       = 8192; // file buffer size

    public static final String TOP_PATH     = "..";

    public static final String CURRENT_PATH = ".";

    // ------------------------------------------------------------charset encoding
    public static final Charset DEFAULT_CHARSET = Charset.defaultCharset(); // default charset

    public static final String DEFAULT_CHARSET_NAME = DEFAULT_CHARSET.name(); // default charset name

    public static final String UTF_8 = "UTF-8"; // UTF-8 encoding

    // ------------------------------------------------------------file separator
    public static final String WINDOWS_FOLDER_SEPARATOR = "\\";

    public static final String UNIX_FOLDER_SEPARATOR = "/";

    public static final String SYSTEM_FOLDER_SEPARATOR = File.separator;

    // ------------------------------------------------------------line separator
    public static final String UNIX_LINE_SEPARATOR = "\n"; // unix file line separator spec \n  LF

    public static final String WINDOWS_LINE_SEPARATOR = "\r\n"; // windows file line separator spec \r\n  CRLF

    public static final String MAC_LINE_SEPARATOR = "\r"; // mac file line separator spec \r  CR

    public static final String SYSTEM_LINE_SEPARATOR; // system file line separator
    static {
        /*
        String separator = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("line.separator")
        );
        if (separator == null || separator.length() == 0) {
            separator = System.getProperty("line.separator", "\n");
        }
        SYSTEM_LINE_SEPARATOR = separator;
        */
        StringBuilderWriter buffer = new StringBuilderWriter(4);
        PrintWriter out = new PrintWriter(buffer);
        out.println();
        out.flush();
        SYSTEM_LINE_SEPARATOR = buffer.toString();
        out.close();
    }

    /**
     * 创建目录
     * @param filePath
     * @return
     */
    public static File mkdir(String filePath) {
        File file = new File(filePath);
        mkdir(file);
        return file;
    }

    /**
     * 创建目录
     *
     * @param file
     * @return
     */
    public static void mkdir(File file) {
        if (file.isFile()) {
            throw new IllegalStateException(file.getAbsolutePath() + " is a directory.");
        }

        if (file.exists()) {
            return;
        }

        if (file.mkdirs()) {
            file.setLastModified(System.currentTimeMillis());
        }
    }

    /**
     * 创建文件
     * @param file
     * @return
     */
    public static void touch(File file) {
        if (file.isDirectory()) {
            throw new IllegalStateException(file.getAbsolutePath() + " is a directory.");
        }

        if (file.exists()) {
            return;
        }

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            if (file.createNewFile()) {
                file.setLastModified(System.currentTimeMillis());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // --------------------------------------------------------------------------file to string

    public static String toString(File file) throws IOException {
        return toString(file, CharsetDetector.detect(file));
    }


    public static String toString(File file, Charset charset) throws IOException {
        ByteOrderMarks bom = ByteOrderMarks.of(charset, file);

        try (FileInputStream input = new FileInputStream(file);
             FileChannel channel = input.getChannel()
        ) {
            //FileLock lock = channel.lock();
            //lock.release();
            long offset = 0, length = channel.size();
            if (bom != null) {
                offset = bom.length();
                length -= offset;
            }
            ByteBuffer buffer = channel.map(MapMode.READ_ONLY, offset, length);
            return charset.decode(buffer).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads file to byte array
     *
     * @param file
     * @return
     */
    public static byte[] toByteArray(File file) {
        try (FileInputStream in = new FileInputStream(file);
             FileChannel channel = in.getChannel()
        ) {
            ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes, 0, bytes.length);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------readByteArray

    public static byte[] readByteArray(String filePath, int count) throws IOException {
        return readByteArray(new File(filePath), count);
    }

    public static byte[] readByteArray(File file, int count) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            return readByteArray(input, count);
        }
    }

    public static byte[] readByteArray(InputStream input, int count) throws IOException {
        byte[] bytes = new byte[count];
        int n, index = 0;
        while (index < count && (n = input.read(bytes, index, count - index)) != EOF) {
            index += n;
        }

        return (index == count) ? bytes : Arrays.copyOf(bytes, index);
    }

    public static TreeNode<Integer, File> listFiles(String filePath) {
        return listFiles(new File(filePath));
    }

    /**
     * 递归列出所有文件
     *
     * @param file
     * @return
     */
    public static TreeNode<Integer, File> listFiles(File file) {
        List<PlainNode<Integer, File>> files = new LinkedList<>();
        AtomicInteger counter = new AtomicInteger(1);
        Integer dummyRootPid = 0;
        Deque<PlainNode<Integer, File>> stack = new LinkedList<>();
        stack.push(new PlainNode<>(counter.getAndIncrement(), dummyRootPid, file));
        while (!stack.isEmpty()) {
            PlainNode<Integer, File> node = stack.pop();
            files.add(node);
            if (node.getAttach().isDirectory()) {
                Arrays.stream(node.getAttach().listFiles())
                      .sorted(Comparator.comparing(File::getName))
                      .forEach(f -> stack.push(new PlainNode<>(counter.getAndIncrement(), node.getNid(), f)));
            }
        }

        return TreeNode.<Integer, File>builder(dummyRootPid).build().mount(files).getChildren().get(0);
    }

    public static String tree(String filePath) throws IOException {
        return tree(new File(filePath));
    }

    /**
     * 打印文件树
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String tree(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        new MultiwayTreePrinter<>(builder, File::getName, f -> f.isDirectory() ? Arrays.asList(f.listFiles()) : null).print(file);
        return builder.toString();
    }

}
