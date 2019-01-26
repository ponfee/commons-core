package code.ponfee.commons.wechat;

import static code.ponfee.commons.concurrent.ThreadPoolExecutors.DISCARD_POLICY_SCHEDULER;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.JedisLock;

/**
 * 不用实现InitializingBean接口，用@PostConstruct也可以
 * 先后顺序：Constructor > `@PostConstruct > InitializingBean[afterPropertiesSet ] > init-method[xml config]
 * 
 * wechat global token and jsapi ticket manager
 * 微信accesstoken刷新会平滑过渡，官方文档给出的是5分钟
 * http://qydev.weixin.qq.com/wiki/index.php?title=%E4%B8%BB%E5%8A%A8%E8%B0%83%E7%94%A8
 * https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1445241432
 * 
 * @author Ponfee
 */
public class WechatTokenManager implements DisposableBean {

    /** wechat config of appid and secret */
    private static final Map<String, Wechat> WECHAT_CONFIGS = new HashMap<String, Wechat>() {
        private static final long serialVersionUID = 4891406751053897149L;

        // The init code block for constructor body(non-static code block)
        {
            put(new Wechat("wx0a7a9ac2a6e0f7e7", "4516f601af973d42cbfee3b7ca7cff34"));
            //put(new Wechat("appid", "secret"));
        }

        private void put(Wechat wechat) {
            super.put(wechat.appid, wechat);
        }
    };

    /** refresh token lock key(cluster servers refresh once) */
    private static final Map<String, JedisLock> JEDIS_LOCKS = new ConcurrentHashMap<>();

    /** maximum of 2000 request from wechat every day */
    private static final int REFRESH_PERIOD_SECONDS = (int) Math.ceil(86400.0D / 2000);

    /** the token effective time 7200 seconds */
    private static final int TOKEN_EXPIRE = 7200 - 60;

    /** refresh from cache of period */
    private static final int CACHE_REFRESH_SECONDS = 30;

    /** the wechat opendid cache key */
    private static final String WECHAT_OPENID_CACHE = "wechat:openid:cache:";

    private static Logger logger = LoggerFactory.getLogger(WechatTokenManager.class);

    private final JedisClient jedisClient;

    public WechatTokenManager(JedisClient jedisClient) {
        this.jedisClient = jedisClient;

        // refresh token from wechat schedule
        DISCARD_POLICY_SCHEDULER.scheduleAtFixedRate(() -> {
            for (Wechat wechat : WECHAT_CONFIGS.values()) {
                try {
                    refreshToken(wechat);
                } catch (FrequentlyRefreshException e) {
                    logger.error(e.getMessage());
                } catch (Throwable t) {
                    logger.error("refresh token occur error", t);
                }
            }
        }, 0, TOKEN_EXPIRE / 2 - 1, TimeUnit.SECONDS);

        // load token and ticket from redis cache schedule
        DISCARD_POLICY_SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                for (Wechat wx : WECHAT_CONFIGS.values()) {
                    String accessToken = jedisClient.valueOps().get(wx.accessTokenKey);
                    if (StringUtils.isNotEmpty(accessToken)) {
                        wx.accessToken = accessToken;
                    }

                    String jsapiTicket = jedisClient.valueOps().get(wx.jsapiTicketKey);
                    if (StringUtils.isNotEmpty(jsapiTicket)) {
                        wx.jsapiTicket = jsapiTicket;
                    }
                }
                logger.info("---load token form cache---");
            } catch (Throwable t) {
                logger.error("load token from cache occur error", t);
            }
        }, 2, CACHE_REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 获取accessToken
     * @param appid
     * @return token
     */
    public final String getAccessToken(String appid) {
        return getWechat(appid).accessToken;
    }

    /**
     * 获取jsapiTicket
     * @param appid
     * @return ticket
     */
    public final String getJsapiTicket(String appid) {
        return getWechat(appid).jsapiTicket;
    }

    /**
     * 手动刷新accessToken
     * @param appid
     * @return
     */
    public String refreshAndGetAccessToken(String appid)
        throws FrequentlyRefreshException {
        Wechat wechat = getWechat(appid);
        refreshToken(wechat);
        return wechat.accessToken;
    }

    /**
     * 手动刷新jsapiTicket
     * @param appid
     * @return
     */
    public String refreshAndGetJsapiTicket(String appid)
        throws FrequentlyRefreshException {
        Wechat wechat = getWechat(appid);
        refreshToken(wechat);
        return wechat.jsapiTicket;
    }

    /**
     * 缓存openId：主要是解决获取openid时若网络慢会同时出现多次请求，导致错误：
     * {"errcode":40029,"errmsg":"invalid code, hints: [ req_id: raY0187ns82 ]"}
     * 当调用{@link Wechats#getOAuth2(String, String, String)}时，如果返回此错误则从缓存获取
     * 如果获取成功则缓存到此缓存
     * @param code
     * @param openid
     */
    public void cacheOpenIdByCode(String code, String openid) {
        jedisClient.valueOps().set(WECHAT_OPENID_CACHE + code, openid, 15);
    }

    /**
     * 加载openId
     * @param code
     * @return the cache of openid
     */
    public String loadOpenIdByCode(String code) {
        return jedisClient.valueOps().get(WECHAT_OPENID_CACHE + code);
    }

    public @Override void destroy() {
        // do nothing
    }

    // -----------------------------------private methods--------------------------------- //
    private Wechat getWechat(String appid) {
        Wechat wechat = WECHAT_CONFIGS.get(appid);
        if (wechat != null) {
            return wechat;
        }
        throw new IllegalArgumentException("invalid wechat appid: " + appid);
    }

    /**
     * 主动刷新token（已限制频率）
     * @param wx
     * @return
     * @throws FrequentlyRefreshException
     */
    private void refreshToken(Wechat wx) throws FrequentlyRefreshException {
        // limit refresh frequency: set the minimum period seconds
        if (getLock(wx.lockRefreshKey, REFRESH_PERIOD_SECONDS).tryLock()) {

            try {
                String accessToken = Wechats.getAccessToken(wx.appid, wx.secret);
                if (StringUtils.isNotEmpty(accessToken)) {
                    wx.accessToken = accessToken;
                    jedisClient.valueOps().set(wx.accessTokenKey, accessToken, TOKEN_EXPIRE);
                }
            } catch (Throwable t) {
                logger.error("refresh access token occur error", t);
            }

            try {
                String jsapiTicket = Wechats.getJsapiTicket(wx.accessToken);
                if (StringUtils.isNotEmpty(jsapiTicket)) {
                    wx.jsapiTicket = jsapiTicket;
                    jedisClient.valueOps().set(wx.jsapiTicketKey, jsapiTicket, TOKEN_EXPIRE);
                }
            } catch (Throwable t) {
                logger.error("refresh jsapi ticket occur error", t);
            }

            logger.info("--- refresh wechat token appid: {} ---", wx.appid);

        } else {
            throw new FrequentlyRefreshException("wechat token refreshed is too frequent, "
                                               + "plz try again later.");
        }
    }

    /**
     * Returns the distributed lock
     * 
     * @param lockKey
     * @param timeout
     * @return
     */
    private JedisLock getLock(String lockKey, int timeout) {
        JedisLock lock = JEDIS_LOCKS.get(lockKey);
        if (lock == null) {
            synchronized (WechatTokenManager.class) {
                lock = JEDIS_LOCKS.computeIfAbsent(
                    lockKey, k -> new JedisLock(jedisClient, k, timeout)
                );
            }
        }
        return lock;
    }

    /**
     * Wechat
     */
    private static final class Wechat {
        final String appid;
        final String secret;
        final String accessTokenKey;
        final String jsapiTicketKey;
        final String lockRefreshKey;

        volatile String accessToken = null;
        volatile String jsapiTicket = null;

        Wechat(String appid, String secret) {
            this.appid = appid;
            this.secret = secret;
            this.accessTokenKey = "wx:access:token:" + appid;
            this.jsapiTicketKey = "wx:jsapi:ticket:" + appid;
            this.lockRefreshKey = "wx:token:refrsh:" + appid;
        }
    }

}
