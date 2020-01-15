package test.jce;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;

public class Argon2Test {

    @Test
    public void test1() throws IOException {
     // Create instance
        Argon2 argon2 = Argon2Factory.create();

        // Read password from user
        char[] password = "passwd".toCharArray();

        try {
            // Hash password
            String hash = argon2.hash(8, 65536, 1, password);
            System.out.println(hash);
            // Verify password
            if (argon2.verify(hash, password)) {
                // Hash matches password
            } else {
                // Hash doesn't match password
            }
        } finally {
            // Wipe confidential data
            argon2.wipeArray(password);
        }
    }
    
    @Test @Ignore
    public void test2() throws IOException {
        Argon2 argon2 = Argon2Factory.create();
        // 1000 = The hash call must take at most 1000 ms
        // 65536 = Memory cost
        // 1 = parallelism
        int iterations = Argon2Helper.findIterations(argon2, 1000, 65536, 1);
        System.out.println("Optimal number of iterations: " + iterations);
    }
    
    @Test @Ignore
    public void test3() throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i = 0; i < 10; i++) {
            Argon2Factory.create().hash(8, 65536, 1, "findIterations".toCharArray());
        }
        System.out.println(stopwatch.stop().toString());
    }
}
