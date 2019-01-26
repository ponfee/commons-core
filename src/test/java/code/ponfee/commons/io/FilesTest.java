package code.ponfee.commons.io;

import java.io.File;

public class FilesTest {

    public static void main(String[] args) {
        System.out.println(File.pathSeparator);
        System.out.println(File.separator);
        String s = Files.human(1152921504606846976L);
        System.out.println(s);
        System.out.println(Files.parseHuman(s));
        //System.out.println(guessFileType(new File("d:/代码走查问题表.xlsx")));
    }
}
