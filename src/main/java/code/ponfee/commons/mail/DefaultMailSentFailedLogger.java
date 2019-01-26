package code.ponfee.commons.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 邮件发送失败日志接口默认实现类
 * @author fupf
 */
public class DefaultMailSentFailedLogger implements MailSentFailedLogger {
    private static Logger logger = LoggerFactory.getLogger(DefaultMailSentFailedLogger.class);

    @Override
    public void log(String logid, int retries, MailEnvelope envelope, Exception e) {
        logger.error("mail sent fail [{}] - [{}] - {}", logid, retries, envelope, e);
    }

}
