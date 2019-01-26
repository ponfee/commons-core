package code.ponfee.commons.io;

import java.io.File;

public class WrappedBufferedReaderTest {

    public static void main(String[] args) {
        try (WrappedBufferedReader reader = new WrappedBufferedReader(new File("d:/编辑6.txt"))) {
            for (String str = reader.readLine(); str != null; str = reader.readLine()) {
                System.out.println(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
