package cn.ponfee.commons.serial;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.junit.Test;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import cn.ponfee.commons.json.JacksonDate;
import cn.ponfee.commons.date.JavaUtilDateFormat;

public class JacksonObjectMapperTest {

    static int round = 9999999;

    @Test
    public void test1() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        Date date = new Date();
        System.out.println(mapper.writeValueAsString(date));
        for (int i = 0; i < round; i++) {
            mapper.writeValueAsString(date);
        }
    }

    @Test
    public void test2() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new JavaUtilDateFormat("yyyy-MM-dd HH:mm:ss"));
        Date date = new Date();
        System.out.println(mapper.writeValueAsString(date));
        for (int i = 0; i < round; i++) {
            mapper.writeValueAsString(date);
        }
    }

    @Test
    public void test3() throws Exception {
        String json = "{\"id\": 0,\"title\": \"\",\"content\": \"xxx\",\"email\": \"ponfee.cn@gmail.com\",\"createDate\": \"20140202\",\"updateDate\": \"2019-10-18 16:02:52\"}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        JavaUtilDateFormat format = new JavaUtilDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleModule module = new SimpleModule();
        JacksonDate jacksonDate = new JacksonDate(format);
        module.addSerializer(java.util.Date.class, jacksonDate.serializer());
        module.addDeserializer(java.util.Date.class, jacksonDate.deserializer());
        mapper.registerModule(module);

        //mapper.setConfig(mapper.getDeserializationConfig().with(format));

        Article a = mapper.readValue(json, Article.class);
        System.out.println(a.getCreateDate());
        System.out.println(a.getUpdateDate());
    }

    public static class Article implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 编号 */
        private int id;

        /** 标题 */
        @NotNull(message = "标题不能为空")
        private String title;

        /** 内容 */
        @Length(min = 10, max = 100, message = "内容不能少于10个字符")
        @NotBlank(message = "内容不能为空")
        private String content;

        @NotEmpty(message = "邮箱不能为空")
        @Email(regexp = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$", message = "邮箱格式错误")
        private String email;

        @DateTimeFormat(pattern = "yyyy-MM-dd") // 字符串转日期（需引入joda-time）
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8") // 日期转字符串（覆盖spring-mvc配置）
        private Date createDate;

        private Date updateDate = new Date(); // 日期转字符串（使用spring-mvc配置）

        public Article() {}

        public Article(int id, String title, String content) {
            super();
            this.id = id;
            this.title = title;
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Date getCreateDate() {
            return createDate;
        }

        public void setCreateDate(Date createDate) {
            this.createDate = createDate;
        }

        public Date getUpdateDate() {
            return updateDate;
        }

        public void setUpdateDate(Date updateDate) {
            this.updateDate = updateDate;
        }

        @Override
        public String toString() {
            return "Article [id=" + id + ", title=" + title + ", content=" + content + "]";
        }
    }

}
