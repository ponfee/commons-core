package test.log4j;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.util.MavenProjects;

public class TestLog4j {

    private static Logger logger = LoggerFactory.getLogger(TestLog4j.class);
    public static void main(String[] args) {
        logger.trace("abcd");
        logger.info("abcd");
        logger.info(Files.toString(MavenProjects.getTestJavaFile(TestLog4j.class)));
        logger.info("abcd");
        logger.info("abcd");
        logger.warn("abcd");
        logger.error("abcd");
    }
}
