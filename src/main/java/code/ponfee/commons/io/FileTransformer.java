package code.ponfee.commons.io;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.ICodepageDetector;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

/**
 * 文件编码转换与文本内容替换
 * 
 * @author Ponfee
 */
public class FileTransformer {

    private static final int FIX_LENGTH = 85;

    private String includeFileExtensions = "(?i)^(.+\\.)(" + StringUtils.join(new String[] { "java", "txt",
         "properties", "xml", "sql", "html", "htm", "jsp", "css", "js", "log", "bak", "ini", "csv" }, "|") + ")$";

    private final File source;
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
        this.source = new File(source);
        this.sourcePath = this.source.getAbsolutePath();
        File targetDir = Files.mkdir(target);
        this.targetPath = targetDir.getAbsolutePath();
        this.encoding = encoding;
    }

    /**
     * 文件后缀名，不加“.”
     * @param includeFileExtensions
     */
    public void setIncludeFileExtensions(String... includeFileExtensions) {
        this.includeFileExtensions = "(?i)^(.+\\.)(" + StringUtils.join(includeFileExtensions, "|") + ")$";
    }

    public void setReplaceEach(String[] searchList, String[] replacementList) {
        this.searchList = searchList;
        this.replacementList = replacementList;
    }

    /**
     * 转换（移）
     */
    public void transform() {
        transform(this.source);
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
            String filepath = file.getAbsolutePath(), charset;
            File dest = Files.touch(targetPath + filepath.substring(sourcePath.length()));
            boolean isMatch = file.getName().matches(includeFileExtensions);

            if (   StringUtils.isNotEmpty(encoding) 
                && isMatch 
                && (charset = guessEncoding(filepath)) != null
                && !"void".equalsIgnoreCase(charset) 
                && !encoding.equalsIgnoreCase(charset)
            ) {
                log.append("转换　[").append(charset).append("]").append(StringUtils.rightPad(filepath, FIX_LENGTH)).append("　-->　");
                transform(file, dest, charset, encoding, searchList, replacementList);
                log.append("[").append(encoding).append("]").append(dest.getAbsolutePath()).append("\n");
            } else if (isMatch && searchList != null && searchList.length > 0) {
                log.append("替换　").append(StringUtils.rightPad(filepath, FIX_LENGTH)).append("　-->　");
                transform(file, dest, searchList, replacementList);
                log.append(dest.getAbsolutePath()).append("\n");
            } else {
                log.append("复制　").append(StringUtils.rightPad(filepath, FIX_LENGTH)).append("　-->　");
                transform(file, dest);
                log.append(dest.getAbsolutePath()).append("\n");
            }
        }
    }

    /**
     * 采用nio方式转移
     * @param source
     * @param target
     */
    public static void transform(File source, File target) {
        try ( FileInputStream sourceFile = new FileInputStream(source);
              FileChannel sourceChannel = sourceFile.getChannel();
              FileOutputStream targetFile = new FileOutputStream(target);
              FileChannel targetChannel = targetFile.getChannel()
        ) {
            sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
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
    public static void transform(File source, File target, String fromCharset, String toCharset) {
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
    public static void transform(File source, File target, String fromCharset, String toCharset,
                                 String[] searchList, String[] replacementList) {
        try (WrappedBufferedReader reader = new WrappedBufferedReader(source, fromCharset);
             WrappedBufferedWriter writer = new WrappedBufferedWriter(target, toCharset)
        ) {
            writeln(reader, writer, searchList, replacementList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String guessEncoding(String path) {
        try {
            return buildDetector().detectCodepage(new File(path).toURI().toURL()).name();
        } catch (IOException ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    public static String guessEncoding(byte[] bytes) {
        return guessEncoding(new ByteArrayInputStream(bytes));
    }

    /**
     * 探测文件编码类型
     * @param inputStream
     * @return
     */
    public static String guessEncoding(InputStream inputStream) {
        try (InputStream input = inputStream) {
            return buildDetector().detectCodepage(input, input.available()).name();
        } catch (IOException ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    /**
     * 利用第三方开源包cpdetector获取文件编码格式(cpdetector.jar)
     * detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
     * cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法 加进来，
     *           如ParsingDetector、JChardetFacade、ASCIIDetector、UnicodeDetector。
     * detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的字符集编码。
     * cpDetector是基于统计学原理的，不保证完全正确。
     * @return
     */
    private static ICodepageDetector buildDetector() {
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();

        // ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
        // 指示是否显示探测过程的详细信息，为false不显示。
        detector.add(new ParsingDetector(false));
        detector.add(new ByteOrderMarkDetector());

        // JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
        // 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
        // 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
        detector.add(JChardetFacade.getInstance()); // 用到antlr.jar、chardet.jar
        detector.add(ASCIIDetector.getInstance()); // ASCIIDetector用于ASCII编码测定
        detector.add(UnicodeDetector.getInstance()); // UnicodeDetector用于Unicode家族编码的测定
        return detector;
    }

    private static void writeln(WrappedBufferedReader reader,
                                WrappedBufferedWriter writer,
                                String[] searchList,
                                String[] replacementList) throws IOException {
        String line;
        if (searchList != null && searchList.length > 0) {
            while ((line = reader.readLine()) != null) {
                writer.writeln(StringUtils.replaceEach(line, searchList, replacementList));
            }
        } else {
            while ((line = reader.readLine()) != null) {
                writer.writeln(line);
            }
        }
    }

}
