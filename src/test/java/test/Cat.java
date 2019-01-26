package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

// java test.Cat -n  < Cat.java | java test.Cat -n
public class Cat {
    public static void main(String[] args) throws IOException {
        //是否显示行号，使用参数 -n 启用
        boolean showNumber = args.length > 0 && Arrays.asList(args).contains("-n");
        int num = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        while (line != null) {
            if (showNumber) {
                System.out.printf("%1$8s %2$s%n",  num++, line);
            } else {
                System.out.println(line);
            }
            line = reader.readLine();
        }
    }
}
