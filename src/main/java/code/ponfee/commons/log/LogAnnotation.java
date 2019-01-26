package code.ponfee.commons.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果是日志入库，则无法用在service的只读事务方法上，需要新开启嵌套的事务
 * <p>
 * 
 * Ali开发手册：
 * 应用中的扩展日志（如打点、临时监控、访问日志等）命名方式：appName_logType_logName.log。
 * logType:日志类型，推荐分类有stats/desc/monitor/visit等；logName:日志描述。
 * 这种命名的好处：通过文件名就可知道日志文件属于什么应用，什么类型，什么目的，也有利于归类查找。
 * 
 * 可以使用warn日志级别来记录用户输入参数错误的情况，避免用户投诉时，无所适从。
 * 注意日志输出的级别，error级别只记录系统逻辑出错、异常等重要的错误信息。
 * 如非必要，请不要在此场景打出error级别。
 * <p>
 * 
 * 日志注解
 * @author fupf
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {

    LogType type() default LogType.UNDEFINED;

    boolean enabled() default false; // 是否开启熔断

    String desc() default "";

    enum LogType {
        UNDEFINED(0x0, null), ADD(0x1, "新增"), UPDATE(0x2, "更新"), 
        DELETE(0x3, "删除"), QUERY(0x4, "查询");

        private final int type;

        private final String comment;

        LogType(int type, String comment) {
            this.type = type;
            this.comment = comment;
        }

        public String comment() {
            return comment;
        }

        public int type() {
            return type;
        }
    }

}
