/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * 文件编码转换与文本内容替换
 * 
 * @author Ponfee
 */
public class FileTransformer {

    private static final int FIX_LENGTH = 85;
    private static final String[] CHARSETS = {
        "GBK", "GB2312", "UTF-8", 
        "UTF-16", "UTF-16LE", "UTF-16BE", 
        "UTF-32", "UTF-32LE", "UTF-32BE"
    };

    private String includeFileExtensions = regexExtensions(
        "java", "txt", "properties", "xml", "sql", "html", "htm", "jsp", 
        "css", "js", "log", "bak", "ini", "csv", "yml", "yaml"
    );

    private final String sourcePath;
    private final String targetPath;
    private final String encoding;
    private final StringBuilder log = new StringBuilder(4096);
    private String[] searchList;
    private String[] replacementList;

    public FileTransformer(String source, String target) {
        this(source, target, null);
    }

    public FileTransformer(String source, String target, String encoding) {
        this.sourcePath = new File(source).getAbsolutePath();
        this.targetPath = Files.mkdir(target).getAbsolutePath();
        this.encoding = encoding;
    }

    /**
     * 文件后缀名，不加“.”
     * 
     * @param includeFileExtensions
     */
    public void setIncludeFileExtensions(String... includeFileExtensions) {
        this.includeFileExtensions = regexExtensions(includeFileExtensions);
    }

    public void setReplaceEach(String[] searchList, String[] replacementList) {
        this.searchList = searchList;
        this.replacementList = replacementList;
    }

    /**
     * 转换（移）
     */
    public void transform() {
        transform(new File(sourcePath));
    }

    public String getTransformLog() {
        return log.toString();
    }

    private void transform(File file) {
        if (file == null) {
            // nothing to do
        } else if (file.isDirectory()) {
            File[] subfiles = file.listFiles();
            if (subfiles != null) {
                for (File sub : subfiles) {
                    transform(sub);
                }
            }
        } else {
            String path = file.getAbsolutePath(), charset;
            File dest = new File(targetPath + path.substring(sourcePath.length()));
            boolean isMatch = file.getName().matches(includeFileExtensions);

            if (   isMatch 
                && StringUtils.isNotEmpty(encoding) 
                && ArrayUtils.contains(CHARSETS, charset = CharsetDetector.detect(path).name().toUpperCase())
                && !encoding.equalsIgnoreCase(charset)
            ) {
                log.append("转换：[").append(charset).append("]").append(StringUtils.rightPad(path, FIX_LENGTH)).append("  -->  ");
                transform(file, dest, charset, encoding, searchList, replacementList);
                log.append("[").append(encoding).append("]").append(dest.getAbsolutePath()).append("\n");
            } else if (isMatch && ArrayUtils.isNotEmpty(searchList)) {
                log.append("替换：").append(StringUtils.rightPad(path, FIX_LENGTH)).append("  -->  ");
                transform(file, dest, searchList, replacementList);
                log.append(dest.getAbsolutePath()).append("\n");
            } else {
                log.append("复制：").append(StringUtils.rightPad(path, FIX_LENGTH)).append("  -->  ");
                transform(file, dest);
                log.append(dest.getAbsolutePath()).append("\n");
            }
        }
    }

    /**
     * 采用nio方式转移
     * 
     * @param source
     * @param target
     */
    public static void transform(File source, File target) {
        try {
            target.getParentFile().mkdirs();
            //com.google.common.io.Files.copy(source, source);
            //sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
            java.nio.file.Files.copy(source.toPath(), target.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param source 源文件路径
     * @param target 输出文件路径
     * @param searchList  the Strings to search for, no-op if null
     * @param replacementList  the Strings to replace them with, no-op if null
     */
    public static void transform(File source, File target,
                                 String[] searchList, String[] replacementList) {
        Files.touch(target);
        try (WrappedBufferedReader reader = new WrappedBufferedReader(source);
             WrappedBufferedWriter writer = new WrappedBufferedWriter(target)
        ) {
            writeln(reader, writer, searchList, replacementList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transforms the source file to target file charset
     *
     * @param source
     * @param target
     * @param fromCharset
     * @param toCharset
     */
    public static void transform(File source, File target, 
                                 String fromCharset, String toCharset) {
        transform(source, target, fromCharset, toCharset, null, null);
    }

    /**
     * @param source 源文件路径
     * @param target 输出文件路径
     * @param fromCharset 源文件编码
     * @param toCharset 目标文件编码
     * @param searchList  the Strings to search for, no-op if null
     * @param replacementList  the Strings to replace them with, no-op if null
     */
    public static void transform(File source, File target, 
                                 String fromCharset, String toCharset,
                                 String[] searchList, String[] replacementList) {
        Files.touch(target);
        try (WrappedBufferedReader reader = new WrappedBufferedReader(source, fromCharset);
             WrappedBufferedWriter writer = new WrappedBufferedWriter(target, toCharset)
        ) {
            writeln(reader, writer, searchList, replacementList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeln(WrappedBufferedReader reader,
                                WrappedBufferedWriter writer,
                                String[] searchList,
                                String[] replacementList) throws IOException {
        String line;
        if (searchList != null && searchList.length > 0) {
            while ((line = reader.readLine()) != null) {
                writer.write(StringUtils.replaceEach(line, searchList, replacementList));
                writer.write(Files.UNIX_LINE_SEPARATOR);
            }
        } else {
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write(Files.UNIX_LINE_SEPARATOR);
            }
        }
    }

    private static String regexExtensions(String... fileExtensions) {
        return "(?i)^(.+\\.)(" + String.join("|", fileExtensions) + ")$";
    }
}
