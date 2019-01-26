package code.ponfee.commons.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.io.Files;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * zip utility based zip4j
 * 
 * @see <a href="http://commons.apache.org/proper/commons-compress">apache commons-compress</a>
 * 
 * @author fupf
 */
public class ZipUtils {

    private static final String SUFFIX = ".zip";

    // -----------------------------------压缩-----------------------------------
    /**
     * 压缩指定文件到当前文件夹，压缩后的文件名为：待压缩文件名+.zip
     * @param src 要压缩的指定文件
     * @return 最终的压缩文件存放的绝对路径
     */
    public static String zip(String src) throws ZipException {
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            throw new ZipException("source file not found: " + src);
        }

        String dest = srcFile.getParent() + File.separator;
        if (srcFile.isFile()) {
            dest += FilenameUtils.getBaseName(srcFile.getName()); // 文件名去除后缀
        } else {
            dest += srcFile.getName(); // 以目录名作为文件名
        }

        return zip(src, dest + SUFFIX);
    }

    /**
     * 压缩文件到指定路径
     * @param src 待压缩的文件
     * @param dest 压缩文件存放路径
     * @return 最终的压缩文件存放的绝对路径
     */
    public static String zip(String src, String dest) throws ZipException {
        return zip(src, dest, null);
    }

    /**
     * 使用给定密码压缩文件到指定路径
     * @param src 要压缩的文件
     * @param dest 压缩文件存放路径
     * @param passwd 压缩使用的密码
     * @return 最终的压缩文件存放的绝对路径
     */
    public static String zip(String src, String dest, String passwd)
        throws ZipException {
        return zip(src, dest, true, passwd, null);
    }

    /**
     * 压缩文件到指定路径
     * @param src        待压缩的文件名或文件夹路径名
     * @param dest       压缩文件存放路径
     * @param recursion  是否递归压缩（只对待压缩文件为文件夹时有效）：true是；false否；
     * @param passwd     压缩使用的密码
     * @param comment    注释信息
     * @return 最终的压缩文件存放的绝对路径
     */
    public static String zip(String src, String dest, boolean recursion,
                             String passwd, String comment) throws ZipException {
        // validate source file
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            throw new ZipException("source file not found: " + src);
        }

        // validate dest file
        if (StringUtils.isEmpty(dest)) {
            throw new ZipException("dest file cannot be null");
        }
        File destFile = new File(dest);
        if (destFile.exists()) {
            throw new ZipException("dest file exists: " + dest);
        }
        Files.mkdir(destFile.getParent()); // 创建父路径（如果不存在）

        // create zip parameters
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // 压缩方式
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); // 压缩级别
        if (!StringUtils.isEmpty(passwd)) {
            parameters.setPassword(passwd.toCharArray());
            parameters.setEncryptFiles(true);
            //parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD); // 加密方式
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES); // 加密方式
            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_128);
        }

        // 开始压缩
        ZipFile zipFile = new ZipFile(destFile);
        if (srcFile.isFile()) { // 压缩文件
            zipFile.addFile(srcFile, parameters);
        } else { // 压缩目录
            File[] files = srcFile.listFiles();
            if (files == null || files.length == 0) {
                return null;
            }
            for (File file : files) {
                if (file.isFile()) {
                    zipFile.addFile(file, parameters);
                } else if (recursion) { // 递归压缩目录
                    zipFile.addFolder(file, parameters);
                }
            }
        }

        if (comment != null) {
            zipFile.setComment(comment);
        }
        return destFile.getAbsolutePath();
    }

    // -----------------------------------解压缩-----------------------------------
    /**
     * 解压缩文件到当前目录
     * @param zipFile 压缩文件
     * @return 解压后文件数组
     * @throws ZipException
     */
    public static String unzip(String zipFile) throws ZipException {
        if (StringUtils.isBlank(zipFile)) {
            throw new ZipException("zip file cannot be null");
        }

        String lowercasePath = zipFile.toLowerCase();
        if (lowercasePath.endsWith(SUFFIX)) {
            String dest = zipFile.substring(0, lowercasePath.indexOf(SUFFIX));
            unzip(zipFile, dest);
            return dest;
        } else {
            throw new ZipException("the zip file name must be end with .zip");
        }
    }

    /**
     * 解压缩文件到指定目录
     * @param zipFile 指定的压缩文件
     * @param dest 解压缩存放的目录
     * @return  解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public static File[] unzip(String zipFile, String dest) throws ZipException {
        return unzip(zipFile, dest, null);
    }

    /**
     * 使用给定密码解压指定的压缩文件到指定目录<p>
     * 如果指定目录不存在，可以自动创建，不合法的路径将导致异常被抛出
     * @param zipFile 指定的压缩文件
     * @param dest 解压目录
     * @param passwd 压缩文件的密码
     * @return 解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public static File[] unzip(String zipFile, String dest, 
                               String passwd) throws ZipException {
        return unzip(new File(zipFile), dest, passwd, Files.UTF_8);
    }

    /**
     * 使用给定密码解压指定的压缩文件到指定目录<p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     * @param zipFile 指定的压缩文件
     * @param dest 解压目录
     * @param passwd 压缩文件的密码
     * @param charset 字符编码
     * @return  解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    @SuppressWarnings("unchecked")
    public static File[] unzip(File zipFile, String dest, String passwd, 
                               String charset) throws ZipException {
        // validate zip file
        if (!zipFile.exists()) {
            throw new ZipException("zip file not found: " + zipFile.getAbsolutePath());
        }
        ZipFile zFile = new ZipFile(zipFile);
        if (!zFile.isValidZipFile()) {
            throw new ZipException("invalid zip file.");
        }

        // validate dest file path
        if (StringUtils.isEmpty(dest)) {
            throw new ZipException("dest file path can't be null");
        } else if (new File(dest).exists()) {
            throw new ZipException("dest file is exists: " + dest);
        } else {
            Files.mkdir(dest); // 校验并创建解压缩存放目录
        }

        // unpack zip file
        if (!StringUtils.isEmpty(charset)) {
            zFile.setFileNameCharset(charset);
        }
        if (zFile.isEncrypted()) {
            if (StringUtils.isEmpty(passwd)) {
                throw new ZipException("passwd can't be null");
            } else {
                zFile.setPassword(passwd.toCharArray());
            }
        }
        zFile.extractAll(dest);
        List<File> fileEntries = new ArrayList<>();
        for (FileHeader fileHeader : (List<FileHeader>) zFile.getFileHeaders()) {
            if (!fileHeader.isDirectory()) {
                fileEntries.add(new File(dest, fileHeader.getFileName()));
            }
        }
        return fileEntries.toArray(new File[fileEntries.size()]);
    }

}
