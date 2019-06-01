package code.ponfee.commons.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import code.ponfee.commons.exception.Throwables;
import code.ponfee.commons.limit.current.CurrentLimiter;
import code.ponfee.commons.util.ObjectUtils;

/**
 * <pre>
 *   1.开启spring切面特性：<aop:aspectj-autoproxy />
 *   2.编写子类：
 *     `@Component
 *     `@Aspect
 *     public class TestLogger extends LogRecorder {
 *         `@Around(value = "execution(public * cn.xxx.service.impl..*Impl..*(..)) 
 *                  && `@annotation(log)", argNames = "pjp,log")
 *         `@Override
 *         public Object around(ProceedingJoinPoint pjp, LogAnnotation log) throws Throwable {
 *             return super.around(pjp, log);
 *         }
 *     }
 * </pre>
 * 
 * 日志记录切处理
 * @author fupf
 */
public abstract class LogRecorder {

    private static final int DEFAULT_ALARM_THRESHOLD_MILLIS = 2000;
    private static Logger logger = LoggerFactory.getLogger(LogRecorder.class);

    private final int alarmThresholdMillis; // 告警阀值
    private final CurrentLimiter limiter; // 访问频率限制

    public LogRecorder() {
        this(DEFAULT_ALARM_THRESHOLD_MILLIS);
    }

    public LogRecorder(int alarmThresholdMillis) {
        this(alarmThresholdMillis, null);
    }

    public LogRecorder(CurrentLimiter circuitBreaker) {
        this(DEFAULT_ALARM_THRESHOLD_MILLIS, circuitBreaker);
    }

    public LogRecorder(int alarmThresholdMillis, CurrentLimiter circuitBreaker) {
        Preconditions.checkArgument(alarmThresholdMillis > 0);
        this.alarmThresholdMillis = alarmThresholdMillis;
        this.limiter = circuitBreaker;
    }

    /**
     * 日志拦截
     * @param pjp
     * @return
     * @throws Throwable
     */
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return this.around(pjp, null);
    }

    /**
     * 日志拦截
     * @param pjp
     * @param log
     * @return
     * @throws Throwable
     */
    public Object around(ProceedingJoinPoint pjp, LogAnnotation log) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String methodName = ms.getMethod().toGenericString();

        // request volume threshold
        if (limiter != null && log != null && log.enabled()
            && !limiter.checkpoint(methodName)) {
            throw new IllegalStateException("request denied");
        }

        LogInfo logInfo = new LogInfo(methodName);
        if (log != null) {
            logInfo.setType(log.type());
            logInfo.setDesc(log.desc());
        }

        String logs = getLogs(log);
        logInfo.setArgs(pjp.getArgs());
        if (logger.isInfoEnabled()) {
            logger.info("[exec-before]-[{}]{}-{}", methodName, logs, ObjectUtils.toString(logInfo.getArgs()));
        }
        long start = System.currentTimeMillis();
        try {
            Object retVal = pjp.proceed();
            logInfo.setCostTime((int) (System.currentTimeMillis() - start));
            logInfo.setRetVal(retVal);
            if (logger.isInfoEnabled()) {
                logger.info("[exec-after]-[{}]{}-[{}]", methodName, logs, ObjectUtils.toString(retVal));
            }
            if (logger.isWarnEnabled() && logInfo.getCostTime() > alarmThresholdMillis) {
                logger.warn("[exec-time]-[{}]{}-[cost {}]", methodName, logs, logInfo.getCostTime()); // 执行时间告警
            }
            return retVal;
        } catch (Throwable e) {
            logger.error("[exec-throw]-[{}]{}-{}", methodName, logs, ObjectUtils.toString(logInfo.getArgs()), e);
            logInfo.setCostTime((int) (System.currentTimeMillis() - start));
            logInfo.setException(Throwables.getStackTrace(e));
            throw e; // 向外抛
        } finally {
            try {
                log(logInfo);
            } catch (Throwable ex) {
                logger.error("log info error", ex);
            }
        }
    }

    /**
     * 日志记录（可用于记录到日志表）
     * @param logInfo
     */
    protected void log(LogInfo logInfo) {
        // no-thing to do
    }

    private String getLogs(LogAnnotation log) {
        if (log == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder("-[");
        builder.append(log.type());
        if (log.desc() != null) {
            builder.append(',').append(log.desc());
        }
        return builder.append(']').toString();
    }

}
