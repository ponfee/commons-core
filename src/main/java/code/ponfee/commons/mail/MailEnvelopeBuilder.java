package code.ponfee.commons.mail;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import code.ponfee.commons.mail.MailEnvelope.MailType;

/**
 * 邮件信封实体
 * @author fupf
 */
public class MailEnvelopeBuilder implements Serializable {

    private static final long serialVersionUID = 2375709603692620293L;

    private final MailType type;

    private String subject; // 主题
    private Object content; // 内容

    private String[] to; // 接收人
    private String[] cc; // 抄送
    private String[] bcc; // 密送
    private String[] reply; // 回复

    /** 邮件附件：key为邮件附件名 */
    private Map<String, DataSource> attachments;

    /** 正文图片：key为content-id，正文应包含：<img src="cid:content-id" /> */
    private Map<String, DataSource> contentImages;

    private MailEnvelopeBuilder(MailType type) {
        this.type = type;
    }

    public static MailEnvelopeBuilder newTextBuilder() {
        return new MailEnvelopeBuilder(MailType.TEXT);
    }

    public static MailEnvelopeBuilder newMimeBuilder() {
        return new MailEnvelopeBuilder(MailType.MIME);
    }

    public MailEnvelopeBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    public MailEnvelopeBuilder content(Object content) {
        this.content = content;
        return this;
    }

    public MailEnvelopeBuilder to(String... to) {
        this.to = to;
        return this;
    }

    public MailEnvelopeBuilder cc(String... cc) {
        this.cc = cc;
        return this;
    }

    public MailEnvelopeBuilder bcc(String... bcc) {
        this.bcc = bcc;
        return this;
    }

    public MailEnvelopeBuilder reply(String... reply) {
        this.reply = reply;
        return this;
    }

    /**
     * 添加附件
     * @param fileName
     * @param data
     */
    public MailEnvelopeBuilder addAttachment(String fileName, byte[] data) {
        checkMimeType(true);
        if (this.attachments.containsKey(fileName)) {
            throw new IllegalArgumentException("repeated attachment filename: " + fileName);
        }
        this.attachments.put(fileName, buildDataSource(data));
        return this;
    }

    public MailEnvelopeBuilder addAttachment(String filepath) {
        addAttachment(new File(filepath));
        return this;
    }

    public MailEnvelopeBuilder addAttachment(String fileName, String filepath) {
        addAttachment(fileName, new File(filepath));
        return this;
    }

    public MailEnvelopeBuilder addAttachment(File file) {
        addAttachment(FilenameUtils.getName(file.getAbsolutePath()), file);
        return this;
    }

    public MailEnvelopeBuilder addAttachment(String fileName, File file) {
        checkMimeType(true);
        if (this.attachments.containsKey(fileName)) {
            throw new IllegalArgumentException("repeated attachment filename: " + fileName);
        }
        this.attachments.put(fileName, new FileDataSource(file));
        return this;
    }

    /**
     * 添加正文图片
     * @param contentId
     * @param data
     */
    public MailEnvelopeBuilder addContentImage(String contentId, byte[] data) {
        checkMimeType(false);
        if (this.contentImages.containsKey(contentId)) {
            throw new IllegalArgumentException("repeated image content-id: " + contentId);
        }
        this.contentImages.put(contentId, buildDataSource(data));
        return this;
    }

    /**
     * 文件名为content-id
     * @param filepath
     */
    public MailEnvelopeBuilder addContentImage(String filepath) {
        addContentImage(FilenameUtils.getName(filepath), filepath);
        return this;
    }

    public MailEnvelopeBuilder addContentImage(String contentId, String filepath) {
        addContentImage(contentId, new File(filepath));
        return this;
    }

    public MailEnvelopeBuilder addContentImage(File file) {
        addContentImage(FilenameUtils.getName(file.getAbsolutePath()), file);
        return this;
    }

    public MailEnvelopeBuilder addContentImage(String contentId, File file) {
        checkMimeType(false);
        if (this.contentImages.containsKey(contentId)) {
            throw new IllegalArgumentException("repeated image content-id: " + contentId);
        }
        this.contentImages.put(contentId, new FileDataSource(file));
        return this;
    }

    public MailEnvelope build() {
        if (ArrayUtils.isEmpty(to) && ArrayUtils.isEmpty(cc) && ArrayUtils.isEmpty(bcc)) {
            throw new IllegalArgumentException("Recipient cannot not be all empty.");
        }
        return new MailEnvelope(type, subject, content, to, cc, bcc, 
                                reply, attachments, contentImages);
    }

    private DataSource buildDataSource(byte[] data) {
        return new ByteArrayDataSource(data, "application/octet-stream");
    }

    private void checkMimeType(boolean isAttachments) {
        if (this.type != MailType.MIME) {
            throw new IllegalArgumentException("operation must be mime type");
        }
        if (isAttachments && attachments == null) {
            attachments = new LinkedHashMap<>();
        } else if (!isAttachments && contentImages == null) {
            contentImages = new HashMap<>();
        }
    }

}
