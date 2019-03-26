package code.ponfee.commons.limit.request;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Mac;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.jce.HmacAlgorithms;
import code.ponfee.commons.jce.digest.HmacUtils;

/**
 * Request limiter, like as send sms and so on
 * 
 * @author Ponfee
 */
public abstract class RequestLimiter {

    private static final byte[] SALT_PREFIX = "{;a*9)p<?T".getBytes();

    /** limit operation key */
    public static final String CHECK_FREQ_KEY = "req:lmt:fre:";
    public static final String CHECK_THRE_KEY = "req:lmt:thr:";

    /** validation code verify key */
    public static final String CACHE_CODE_KEY = "req:cah:code:";
    public static final String CHECK_CODE_KEY = "req:chk:code:";

    /** image captcha code verify key */
    public static final String CACHE_CAPTCHA_KEY = "req:cah:cap:";

    /** count operation action key */
    public static final String TRACE_ACTION_KEY = "req:cnt:act:";

    // ----------------------------------------------------------------用于请求限制
    public final RequestLimiter limitFrequency(String key, int period) 
        throws RequestLimitException {
        return limitFrequency(key, period, "请求频繁，请" + format(period) + "后再试！");
    }

    /**
     * 访问频率限制：一个周期内最多允许访问1次<p>
     * 比如短信60秒内只能发送一次
     * 
     * @param key the key
     * @param period the period
     * @param message the message
     * 
     * @return the caller, chain program
     * 
     * @throws RequestLimitException if over limit occurs 
     */
    public abstract RequestLimiter limitFrequency(String key, int period, String message) 
        throws RequestLimitException;

    public final RequestLimiter limitThreshold(String key, int period, int limit) 
        throws RequestLimitException {
        return limitThreshold(key, period, limit, 
                              "请求超限，请" + RequestLimiter.format(period) + "后再试！");
    }

    /**
     * 访问次数限制：一个周期内最多允许访问limit次
     * 比如一个手机号一天只能发10次
     * 
     * @param key    the key
     * @param period the period
     * @param limit  the limit
     * @param message  the message
     * 
     * @return the caller, chain program
     * 
     * @throws RequestLimitException if over limit occurs 
     */
    public abstract RequestLimiter limitThreshold(String key, int period, 
                                                  int limit, String message) 
        throws RequestLimitException;

    // ----------------------------------------------------------------用于验证码校验（如手机验证码）
    /**
     * cache for the server generate validation code
     * 
     * @param key   the cache key
     * @param code  the validation code of server generate
     * @param ttl   the expire time
     */
    public abstract void cacheCode(String key, String code, int ttl);

    /**
     * check the validation code of user input is equals server cache
     * 
     * @param key   the cache key
     * @param code  the validation code of user input
     * @param limit the maximum fail input times
     * 
     * @return the caller, chain program
     * 
     * @throws RequestLimitException if over limit occurs
     */
    public abstract RequestLimiter checkCode(String key, String code, int limit) 
        throws RequestLimitException;

    // ----------------------------------------------------------------用于缓存图片验证码
    /**
     * cache captcha of server generate
     * 
     * @param key
     * @param captcha the image captcha code of server generate
     * @param expire  缓存有效时间
     */
    public abstract void cacheCaptcha(String key, String captcha, int expire);

    public final boolean checkCaptcha(String key, String captcha) {
        return this.checkCaptcha(key, captcha, false);
    }

    /**
     * check captcha of user input
     * 
     * @param key  the cache key
     * @param captcha  the captcha
     * @param caseSensitive  is case sensitive
     * 
     * @return true|false
     */
    public abstract boolean checkCaptcha(String key, String captcha, 
                                         boolean caseSensitive);

    // ------------------------------------------------------------------------行为计数（用于登录失败限制）
    /**
     * 计数周期内的行为<p>
     * 用于登录失败达到一定次数后锁定账户等场景<p>
     *
     * @param key
     * @param period
     */
    public abstract void recordAction(String key, int period);

    /**
     * 统计周期内的行为量<p>
     * 用于登录失败达到一定次数后锁定账户等场景<p>
     * 
     * @param key the key
     * @return action count number
     */
    public abstract long countAction(String key);

    /**
     * 重置行为
     * 
     * @param key the key
     */
    public abstract void resetAction(String key);

    // ----------------------------------------------------------------用于验证码校验
    /**
     * 生成nonce校验码（返回到用户端）
     *
     * @param code a string like as captcha code
     * @param salt a string like as mobile phone
     * 
     * @return a check code
     */
    public static String buildNonce(String code, String salt) {
        //byte[] key = Bytes.fromLong(new Random(code.hashCode()).nextLong()); // 第一个nextLong值是固定的
        byte[] key = code.getBytes();
        Mac mac = HmacUtils.getInitializedMac(HmacAlgorithms.HmacMD5, key);
        mac.update(SALT_PREFIX);
        return Hex.encodeHexString(mac.doFinal(salt.getBytes()));
    }

    /**
     * 校验nonce
     * 
     * @param nonce the nonce
     * @param code  the code
     * @param salt  the salt
     * 
     * @return {@code true} is verify success
     */
    public static boolean verifyNonce(String nonce, String code, String salt) {
        return StringUtils.isNotEmpty(nonce) && nonce.equals(buildNonce(code, salt));
    }

    /**
     * 时间格式化，5/6 rate
     * 
     * @param seconds
     * @return
     */
    public static String format(int seconds) {
        int days = seconds / 86400;
        if (days > 365) { // 年
            return (days / 365 + ((days % 365) / 30 + 10) / 12) + "年";
        }
        if (days > 30) { // 月
            return (days / 30 + (days % 30 + 25) / 30) + "个月";
        }

        seconds %= 86400;
        int hours = seconds / 3600;
        if (days > 0) { // 日
            return (days + (hours + 20) / 24) + "天";
        }

        seconds %= 3600;
        int minutes = seconds / 60;
        if (hours > 0) { // 时
            return (hours + (minutes + 50) / 60) + "小时";
        }

        seconds %= 60;
        if (minutes > 0) { // 分
            return (minutes + (seconds + 50) / 60) + "分钟";
        }

        return seconds + "秒"; // 秒
    }

    static long expire(int ttl) {
        return System.currentTimeMillis() + ttl * 1000;
    }

    static class CacheValue<T> implements Serializable {
        private static final long serialVersionUID = 8615157453929878610L;

        final T value;
        final long expireTimeMillis;
        final AtomicInteger count;

        public CacheValue(T value, long expireTimeMillis) {
            this.value = value;
            this.expireTimeMillis = expireTimeMillis;
            this.count = new AtomicInteger(1);
        }

        int increment() {
            return count.incrementAndGet();
        }

        int count() {
            return count.get();
        }

        T get() {
            return value;
        }

        boolean isExpire() {
            return expireTimeMillis < System.currentTimeMillis();
        }

        boolean isExpire(long timeMillis) {
            return expireTimeMillis < timeMillis;
        }
    }

}
