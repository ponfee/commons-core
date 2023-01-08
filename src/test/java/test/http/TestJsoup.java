package test.http;

import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import cn.ponfee.commons.http.Http;

/**
 * 
 * @author Ponfee
 */
public class TestJsoup {

    private static final String COOKIE =
        "";

    // ========================================================see
    @Test(timeout = 999999999)
    public void viewSeeme() {
        viewSee(0);
    }

    @Test(timeout = 999999999)
    public void deleteSeeme() { // 谁看过我
        deleteSee(0);
    }

    @Test(timeout = 999999999)
    public void viewMesee() {
        viewSee(1);
    }

    @Test(timeout = 999999999)
    public void deleteMesee() { // 我看过谁
        deleteSee(1);
    }

    // ========================================================send
    @Test(timeout = 999999999)
    public void viewSendme() { // 查看收件箱
        viewSend(1);
    }

    @Test(timeout = 999999999)
    public void deleteSendme() { // 删除收件箱
        delSend(1);
    }

    @Test(timeout = 999999999)
    public void viewMesend() { // 查看发件箱
        viewSend(4);
    }

    @Test(timeout = 999999999)
    public void deleteMesend() { // 删除发件箱
        delSend(4);
    }

    // ==================================================================
    private void viewSee(int type) {
        Http page = Http.get("http://profile.zhenai.com/v2/visit/ajax.do").addHeader("COOKIE", COOKIE).addParam("type", type);
        System.out.println(page.addParam("page", "1").request());

        Http delete = Http.get("http://profile.zhenai.com/v2/visit/delete.do").addHeader("COOKIE", COOKIE).addParam("type", type);
        System.out.println(delete.addParam("memberid", "999999999").request());
    }

    @SuppressWarnings("unchecked")
    private void deleteSee(int type) {
        Http page = Http.get("http://profile.zhenai.com/v2/visit/ajax.do").addHeader("COOKIE", COOKIE).addParam("type", type);
        Http delete = Http.get("http://profile.zhenai.com/v2/visit/delete.do").addHeader("COOKIE", COOKIE).addParam("type", type);
        for (;;) {
            try {
                page.addParam("page", 1);
                Map<String, Object> map = page.request(Map.class);
                if ((int) map.get("code") == 0) {
                    System.err.println("page fail");
                    break;
                }
                List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("data");
                for (Map<String, Object> item : list) {
                    System.out.print(item.get("memberId") + ", ");
                    Thread.sleep(50);
                    Map<String, Object> delRes = delete.addParam("memberid", item.get("memberId")).request(Map.class);
                    if ((int) delRes.get("data") == 0) {
                        System.err.println("del fail");
                    }
                }
                System.out.println();
                Thread.sleep(200);
            } catch (Exception e) {}
        }
    }

    // ==================================================================
    private void viewSend(int type) {
        Http page = Http.get("http://profile.zhenai.com/v2/mail/list.do").addHeader("COOKIE", COOKIE).addParam("showType", type);
        String html = page.addParam("pageNo", 1).request();
        Document doc = Jsoup.parse(html, "UTF-8");
        Elements mes = doc.select("section[class='mod-msg-item exp-mail-item'] > a[class='new-icon-close deleteMail-js']");
        for (Element elem : mes) {
            System.out.print(elem.attr("memberid") + ", ");
        }
        System.out.println();
        Http delete = Http.post("http://profile.zhenai.com/v2/mail/deleteMemberNew.do").addHeader("COOKIE", COOKIE);
        System.out.println(delete.data("memberId=999999999").request());
    }

    private void delSend(int type) {
        Http page = Http.get("http://profile.zhenai.com/v2/mail/list.do").addHeader("COOKIE", COOKIE).addParam("showType", type);
        for (;;) {
            try {
                String html = page.addParam("pageNo", 1).request();
                Document doc = Jsoup.parse(html, "UTF-8");
                Elements mes = doc.select("section[class='mod-msg-item exp-mail-item'] > a[class='new-icon-close deleteMail-js']");
                boolean found = false;
                for (Element elem : mes) {
                    found = true;
                    Http delete = Http.post("http://profile.zhenai.com/v2/mail/deleteMemberNew.do").addHeader("COOKIE", COOKIE);
                    String memberid = elem.attr("memberid");
                    System.out.print(memberid + ", ");
                    delete.data("memberId=" + memberid).request();
                    Thread.sleep(50);
                }
                System.out.println();
                if (!found) {
                    break;
                }
                Thread.sleep(200);
            } catch (Exception e) {}
        }
    }

}
