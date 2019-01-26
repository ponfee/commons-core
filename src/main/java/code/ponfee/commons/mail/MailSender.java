package code.ponfee.commons.mail;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.io.Files;
import code.ponfee.commons.mail.MailEnvelope.MailType;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.commons.util.RegexUtils;
import com.sun.mail.util.MailConnectException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import static code.ponfee.commons.util.ObjectUtils.isEmpty;

/**
 * <pre>
 *  mail.debug                  boolean     The initial debug mode. Default is false.
 *  
 *  mail.from                   String      The return email address of the current user, used by the InternetAddress methodgetLocalAddress.
 *  
 *  mail.mime.address.strict    boolean     The MimeMessage class uses the InternetAddress method parseHeader to parse headers in messages. 
 *                                          This property controls the strict flag passed to theparseHeader method. The default is true.
 * 
 *  mail.host                   String      The default host name of the mail server for both Stores and Transports. Used if themail.protocol.
 *                                          host property isn't set.
 * 
 *  mail.store.protocol         String      Specifies the default message access protocol. The Session methodgetStore() returns a Store object 
 *                                          that implements this protocol. By default the first Store provider in the configuration files is returned.
 * 
 *  mail.transport.protocol     String      Specifies the default message transport protocol. The Session methodgetTransport() returns a Transport 
 *                                          object that implements this protocol. By default the first Transport provider in the configuration files is returned.
 * 
 *  mail.user                   String      The default user name to use when connecting to the mail server. Used if the mail.protocol.user property isn't set.
 *  
 *  mail.protocol.class         String      Specifies the fully qualified class name of the provider for the specified protocol. Used in cases where more than one 
 *                                          provider for a given protocol exists; this property can be used to specify which provider to use by default. The provider 
 *                                          must still be listed in a configuration file.
 * 
 *  mail.protocol.host          String      The host name of the mail server for the specified protocol. Overrides the mail.host property.
 *  
 *  mail.protocol.port          int         The port number of the mail server for the specified protocol. If not specified the protocol's default port number is used.
 *  
 *  mail.protocol.user          String      The user name to use when connecting to mail servers using the specified protocol. Overrides themail.user property.
 * 
 *  {@link #MailSender(String, String, String, boolean, Integer, Integer)}
 * </pre>
 * 
 * {@link javax.mail.internet.ParameterList#splitLongParameters }
 * 
 * @see <a href="http://commons.apache.org/proper/commons-email">apache commons-email</a>
 * @see <a href="https://docs.spring.io/spring/docs/5.0.0.M5/spring-framework-reference/html/mail.html">springboot email</a>
 * 
 * smtp邮件发送
 * @author fupf
 */
public class MailSender {

    private static final int SEND_TIMEOUT_SLEEP = 2000;
    private static Logger logger = LoggerFactory.getLogger(MailSender.class);

    static {
        // 修复附件文件名过长显示为“ATT_0266BE.dat”的问题
        System.setProperty("mail.mime.splitlongparameters", "false");
    }

    private final String user;
    private final String password;
    private final String smtpHost;
    private int port = -1;
    private String nickname;
    private String charset = Files.UTF_8;
    private int retryTimes;
    private int validateTimes;

    /** 默认发送失败日志处理 {@link #setSentFailedLogger(MailSentFailedLogger)} */
    private MailSentFailedLogger sentFailedLogger = new DefaultMailSentFailedLogger();

    private final transient Session session;

    /**
     * @param user 邮箱用户名
     * @param password 密码
     * @param smtpHost 主机地址
     */
    MailSender(String user, String password, String smtpHost, 
               boolean authRequire, Integer connTimeout, Integer readTimeout) {
        if (!RegexUtils.isEmail(user)) {
            throw new IllegalArgumentException("Illegal sender email: " + user);
        }
        //if (EmailValidator.verify(user)) {
        //    throw new IllegalArgumentException("Invaild sender email: " + user);
        //}

        if (smtpHost == null || smtpHost.length() == 0) {
            smtpHost = "smtp." + user.split("@")[1];
        }
        Properties props = new Properties();
        //props.setProperty("mail.debug", "true");
        //props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", smtpHost);

        // SSL setting
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        //props.setProperty("mail.transport.protocol", "smtps");
        //props.setProperty("mail.smtp.socketFactory.fallback", "false");
        //props.setProperty("mail.smtp.port", "465");
        //props.setProperty("mail.smtp.socketFactory.port", "465");
        //props.setProperty("mail.smtp.starttls.enable", "true"); // 网易邮箱设为false

        if (connTimeout != null) {
            props.setProperty("mail.smtp.connectiontimeout", connTimeout.toString());
        }
        if (readTimeout != null) {
            props.setProperty("mail.smtp.timeout", readTimeout.toString());
        }
        if (authRequire) {
            props.setProperty("mail.smtp.auth", "true");
            //this.session = Session.getDefaultInstance(props, new SmtpAuth(user, password));
            this.session = Session.getInstance(props, new SmtpAuth(user, password));
        } else {
            props.setProperty("mail.smtp.auth", "false");
            this.session = Session.getInstance(props);
        }
        this.user = user;
        this.password = password;
        this.smtpHost = smtpHost;
    }

    void setCharset(String charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    void setValidateTimes(int validateTimes) {
        this.validateTimes = validateTimes;
    }

    void setNickname(String nickname) {
        this.nickname = nickname;
    }

    void setPort(int port) {
        this.port = port;
    }

    void setSentFailedLogger(MailSentFailedLogger sentFailedLogger) {
        if (sentFailedLogger != null) {
            this.sentFailedLogger = sentFailedLogger;
        }
    }

    public boolean send(MailEnvelope envlop) {
        return send0(ObjectUtils.uuid22(), envlop, this.retryTimes);
    }

    private boolean send0(String logid, MailEnvelope envlop, int retries) {
        Transport transport = null;
        try {
            MimeMessage message = new MimeMessage(session);

            String from = StringUtils.isEmpty(nickname) 
                          ? user : MimeUtility.encodeText(nickname) + "<" + user + ">";
            message.setFrom(new InternetAddress(from));

            Address[] t = verifyEmails(envlop.getTo()); // 发送
            Address[] c = verifyEmails(envlop.getCc()); // 抄送
            Address[] b = verifyEmails(envlop.getBcc()); // 密送
            Address[] r = verifyEmails(envlop.getReply()); // 回复
            if (isEmpty(t) && isEmpty(c) && isEmpty(b)) {
                throw new IllegalArgumentException("to, cc and bcc cannot be all empty.");
            }

            if (!isEmpty(t)) {
                message.addRecipients(Message.RecipientType.TO, t);
            }
            if (!isEmpty(c)) {
                message.addRecipients(Message.RecipientType.CC, c);
            }
            if (!isEmpty(b)) {
                message.addRecipients(Message.RecipientType.BCC, b);
            }
            if (!isEmpty(r)) {
                message.setReplyTo(r);
            }

            if (envlop.getType() == MailType.TEXT) { // 文本
                String content = (String) envlop.getContent();
                if (StringUtils.isNotBlank(content)) {
                    message.setText(content);
                }
            } else { // 复杂格式Multipart
                MimeMultipart mainPart = new MimeMultipart("mixed");
                if (!isEmpty(envlop.getContent())) { // 正文
                    BodyPart contentPart = new MimeBodyPart();
                    contentPart.setContent(envlop.getContent(), "text/html;charset=" + charset);
                    mainPart.addBodyPart(contentPart);
                    mainPart.setSubType("alternative"); // 超文本内容

                    if (!isEmpty(envlop.getContentImages())) { // 正文嵌入图片
                        // <img src="cid:f32rf213fdsr31242fdsa" />
                        for (Entry<String, DataSource> image : envlop.getContentImages().entrySet()) {
                            BodyPart imagePart = new MimeBodyPart();
                            imagePart.setDataHandler(new DataHandler(image.getValue()));
                            imagePart.setHeader("Content-ID", image.getKey());
                            mainPart.addBodyPart(imagePart);
                        }
                        mainPart.setSubType("related"); // 内嵌资源（图片）

                        MimeBodyPart bodyPart = new MimeBodyPart();
                        bodyPart.setContent(mainPart);
                        mainPart = new MimeMultipart("mixed", bodyPart);
                    }
                }

                if (envlop.getAttachments() != null && !envlop.getAttachments().isEmpty()) { // 附件
                    for (Entry<String, DataSource> attachment : envlop.getAttachments().entrySet()) {
                        BodyPart attachmentPart = new MimeBodyPart();
                        attachmentPart.setDataHandler(new DataHandler(attachment.getValue()));
                        //attachmentPart.setFileName(MimeUtility.decodeText(attachment.getKey()));
                        //attachmentPart.setFileName(MimeUtility.encodeText(attachment.getKey()));
                        //attachmentPart.setFileName(MimeUtility.encodeWord(attachment.getKey()));
                        attachmentPart.setFileName(MimeUtility.encodeText(attachment.getKey(), charset, "B"));
                        mainPart.addBodyPart(attachmentPart);
                    }
                    mainPart.setSubType("mixed"); // 含有附件
                }

                message.setContent(mainPart);
            }

            message.setSubject(envlop.getSubject());
            message.setSentDate(new Date());
            message.saveChanges();

            //Transport.send(message);
            transport = session.getTransport("smtp");
            transport.connect(smtpHost, port, user, password);
            transport.sendMessage(message, message.getAllRecipients());
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            if (sentFailedLogger != null) try {
                sentFailedLogger.log(logid, retries, envlop, e);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            if (MailConnectException.class.isInstance(e) && --retries > 0) {
                try {
                    Thread.sleep(SEND_TIMEOUT_SLEEP); // 休眠一段时间后重试
                } catch (InterruptedException ignored) {
                }
                return send0(logid, envlop, retries); // 连接超时重试
            } else if (SendFailedException.class.isInstance(e)) {
                SendFailedException ex = (SendFailedException) e;

                // 已发送的邮箱地址
                if (logger.isInfoEnabled() && !isEmpty(ex.getValidSentAddresses())) {
                    String sent = Arrays.asList(ex.getValidSentAddresses()).toString();
                    logger.info("sent email address [{}] - {}", logid, sent);
                }

                // 无效的邮箱地址
                if (logger.isWarnEnabled() && !isEmpty(ex.getInvalidAddresses())) {
                    String invalid = Arrays.asList(ex.getInvalidAddresses()).toString();
                    logger.warn("invalid email address [{}] - {}", logid, invalid);
                }

                // 有效但未发送的邮箱地址
                if (!isEmpty(ex.getValidUnsentAddresses())) {
                    List<String> unsents = new ArrayList<>();
                    for (Address addr : ex.getValidUnsentAddresses()) {
                        unsents.add(addr.toString());
                    }
                    if (--retries > 0) {
                        // 发送失败重试
                        String[] unsents0 = unsents.toArray(new String[unsents.size()]);
                        envlop = envlop.copy(Collects.intersect(envlop.getTo(), unsents0), 
                                             Collects.intersect(envlop.getCc(), unsents0), 
                                             Collects.intersect(envlop.getBcc(), unsents0),
                                             null);
                        return send0(logid, envlop, retries);
                    } else {
                        logger.error("unsend email address [{}] - {}", logid, unsents.toString());
                    }
                }
            }

            return false;
        } finally {
            if (transport != null && transport.isConnected()) try {
                transport.close();
            } catch (MessagingException e) {
                logger.warn("mail transport close occur error", e);
            }
        }
    }

    /**
     * 处理邮箱地址
     * @param emails
     * @return
     * @throws AddressException
     */
    private Address[] verifyEmails(String[] emails) throws AddressException {
        if (emails == null || emails.length < 1) {
            return null;
        }

        List<Address> addresses = new ArrayList<>();
        for (String email : emails) {
            if (StringUtils.isBlank(email)) {
                // do-non
            } else if (!RegexUtils.isEmail(email)) {
                logger.warn("illegal email address[{}]", email);
            } else if (!EmailValidator.verify(email, this.validateTimes)) {
                logger.warn("invalid email address[{}]", email);
            } else {
                addresses.add(new InternetAddress(email));
            }
        }
        return addresses.toArray(new Address[addresses.size()]);
    }

    /**
     * 邮件权限验证
     */
    private static class SmtpAuth extends Authenticator {
        private final String user, password;

        SmtpAuth(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password);
        }
    }

}
