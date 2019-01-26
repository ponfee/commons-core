package test.wechat;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.wechat.FrequentlyRefreshException;
import code.ponfee.commons.wechat.WechatTokenManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jedis-cfg.xml" })
public class WechatTokenManagerTest {

    private @Resource JedisClient jedisClient;
    final String appid = "wx0a7a9ac2a6e0f7e7";

    @Before
    public void setup() {}

    @After
    public void teardown() {
        jedisClient.destroy();
    }

    @Test
    public void test1() throws InterruptedException {
        WechatTokenManager mg = new WechatTokenManager(jedisClient);
        for (int i = 0;; i++) {
            System.out.println("access_token: " + mg.getAccessToken(appid));
            System.out.println("jsapi_ticket: " + mg.getJsapiTicket(appid));
            Thread.sleep(11000);
            if (i % 3 == 0) {
                try {
                    mg.refreshAndGetAccessToken(appid);
                } catch (FrequentlyRefreshException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}
