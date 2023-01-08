package cn.ponfee.commons.io;

import cn.ponfee.commons.util.MavenProjects;

import java.io.File;

public class WrappedBufferedReaderTest {

    public static void main(String[] args) {
        try (WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(WrappedBufferedReaderTest.class))) {
            for (String str = reader.readLine(); str != null; str = reader.readLine()) {
                System.out.println(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
