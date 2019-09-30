package code.ponfee.commons.log;

import code.ponfee.commons.log.LogAnnotation.LogType;

/**
 * 日志信息
 * 
 * @author Ponfee
 */
public class LogInfo implements java.io.Serializable {
    private static final long serialVersionUID = -4824757481106145723L;

    private LogType type; // 日志类型
    private String desc; // 日志描述
    private String methodName; // 方法名称
    private Object args; // 调用参数
    private Object retVal; // 返回值
    private String exception; // 异常信息
    private int costTime; // 调用耗时（毫秒）

    public LogInfo() {}

    public LogInfo(String methodName) {
        this.methodName = methodName;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object getArgs() {
        return args;
    }

    public void setArgs(Object args) {
        this.args = args;
    }

    public Object getRetVal() {
        return retVal;
    }

    public void setRetVal(Object retVal) {
        this.retVal = retVal;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public int getCostTime() {
        return costTime;
    }

    public void setCostTime(int costTime) {
        this.costTime = costTime;
    }

}
