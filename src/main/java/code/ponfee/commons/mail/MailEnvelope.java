package code.ponfee.commons.mail;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import javax.activation.DataSource;

/**
 * The Mail Envelope
 * 
 * @author fupf
 */
public class MailEnvelope implements Serializable {

    private static final long serialVersionUID = 2375709603692620293L;

    private static final int MAX_LEN = 100;

    public enum MailType { TEXT, MIME }

    private final MailType type;
    private final String subject; // 主题
    private final Object content; // 内容
    private final String[] to; // 接收人
    private final String[] cc; // 抄送
    private final String[] bcc; // 密送
    private final String[] reply; // 回复

    /** 邮件附件：key为邮件附件名 */
    private final Map<String, DataSource> attachments;

    /** 正文图片：key为content-id，正文应包含：<img src="cid:content-id" /> */
    private final Map<String, DataSource> contentImages;

    public MailEnvelope(MailType type, String subject, Object content, 
                        String[] to, String[] cc, String[] bcc, String[] reply, 
                        Map<String, DataSource> attachments,
                        Map<String, DataSource> contentImages) {
        this.type = type;
        this.subject = subject;
        this.content = content;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.reply = reply;
        this.attachments = attachments;
        this.contentImages = contentImages;
    }

    public MailType getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public Object getContent() {
        return content;
    }

    public String[] getTo() {
        return to;
    }

    public String[] getCc() {
        return cc;
    }

    public String[] getBcc() {
        return bcc;
    }

    public String[] getReply() {
        return reply;
    }

    public Map<String, DataSource> getAttachments() {
        return attachments;
    }

    public Map<String, DataSource> getContentImages() {
        return contentImages;
    }

    public MailEnvelope copy(String[] to, String[] cc, 
                             String[] bcc, String[] reply) {
        return new MailEnvelope(type, subject, content, to, cc, bcc, 
                                reply, attachments, contentImages);
    }

    @Override
    public String toString() {
        return new StringBuilder(256)
                .append("{type=").append(type)
                .append(", subject=").append(toString(subject))
                .append(", content=").append(toString(content))
                .append(", to=").append(Arrays.toString(to))
                .append(", cc=").append(Arrays.toString(cc))
                .append(", bcc=").append(Arrays.toString(bcc))
                .append(", attachments=").append(toString(attachments))
                .append(", contentImages=").append(toString(contentImages))
                .append("}").toString();
    }

    private String toString(String str) {
        if (str == null) {
            return null;
        }

        if (str.length() > MAX_LEN) {
            StringBuilder builder = new StringBuilder(str);
            builder.setLength(MAX_LEN - 3);
            str = builder.append("...").toString();
        }
        return str;
    }

    private String toString(Object o) {
        if (o == null) {
            return null;
        }
        return toString(o.toString());
    }

}
