package test.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Java8DateTimeTester {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @org.junit.Test
    public void testFormat() {
        System.out.println(DATE_FORMATTER.format(LocalDateTime.now()));
        //System.out.println(DATE_FORMATTER.format(Instant.now())); // error
        System.out.println(LocalDateTime.now().format(DATE_FORMATTER));

        System.out.println(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    }

    @org.junit.Test
    public void testLocalDate() {
        System.out.println(LocalDateTime.now());
        System.out.println(LocalDateTime.now().toLocalDate());
        System.out.println(LocalDateTime.now().toLocalTime());

        System.out.println(LocalDate.now());
        System.out.println(LocalDate.now().atStartOfDay());

        System.out.println(LocalTime.now());

    }
}
