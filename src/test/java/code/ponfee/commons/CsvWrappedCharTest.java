package code.ponfee.commons;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import code.ponfee.commons.io.WrappedBufferedWriter;
import code.ponfee.commons.util.Strings;

public class CsvWrappedCharTest {

    public static void main(String[] args) throws IOException {
        String file = "C:\\Users\\01367825\\AppData\\Local\\sfim\\NIM\\af1690f6e22097ee1c62875a8090eead\\download\\海信出库报表.csv";

        WrappedBufferedWriter writer = new WrappedBufferedWriter(new File("d:\\海信出库报表-修正.csv"), StandardCharsets.UTF_8);

        Files.asCharSource(new File(file), StandardCharsets.UTF_8).readLines(new LineProcessor<String>() {
            @Override
            public boolean processLine(String line) throws IOException {
                String[] array = new String[8];
                String[] s = line.split(",");

                array[0] = s[0];
                array[1] = s[1];
                array[2] = s[2];
                array[3] = s[3];

                array[7] = s[s.length - 1];
                array[6] = s[s.length - 2];
                array[5] = s[s.length - 3];

                array[4] = StringUtils.join(s, ",", 4, s.length - 3);

                String str = Strings.join(Arrays.asList(array), ",", "\"", "\"");
                System.out.println(str);

                writer.write(str);
                writer.write(code.ponfee.commons.io.Files.UNIX_LINE_SEPARATOR);

                return StringUtils.isNotBlank(line);
            }

            @Override
            public String getResult() {
                return null;
            }
        });

        writer.close();
    }
}
