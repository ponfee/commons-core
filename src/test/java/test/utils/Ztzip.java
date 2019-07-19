package test.utils;

import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.ZipUtils;

public class Ztzip {

    public static void main(String[] args) throws Exception {
        //org.zeroturnaround.zip.ZipUtil.pack(new File("D:\\tmp"), new File("d:/demo.zip"));
        //org.zeroturnaround.zip.ZipUtil.unexplode(new File("D:\\Recv Files.zip"));

        //org.zeroturnaround.zip.ZipUtil.addOrReplaceEntries(new File("d:/demo.zip"), new ZipEntrySource[] {new ByteSource("README.md", "readme!!!!!!!!!!!!!!!!!!!".getBytes())});
        //jodd.io.ZipUtil.unzip("D:\\sql script", "d:/demo1.zip");
        //jodd.io.ZipUtil.zip("D:\\sql script");
        //jodd.io.ZipUtil.gzip("D:\\demo.zip");
        
        
        //ZipUtils.zip(MavenProjects.getProjectBaseDir(), MavenProjects.getProjectBaseDir() + "\\..\\commons-code.zip", true, "123456", "test123");
        ZipUtils.unzip("E:\\commons-code\\commons-code.zip", "d:\\commons-code", "123456");
    }
}
