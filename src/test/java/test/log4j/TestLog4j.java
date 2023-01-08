package test.log4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ponfee.commons.util.MavenProjects;

public class TestLog4j {

    private static Logger logger = LoggerFactory.getLogger(TestLog4j.class);
    public static void main(String[] args) {
        logger.trace("abcd");
        logger.info("abcd");
        logger.info(MavenProjects.getTestJavaFileAsLineString(TestLog4j.class));
        logger.info("abcd");
        logger.info("abcd");
        logger.warn("abcd");
        logger.error("abcd");
    }
}
