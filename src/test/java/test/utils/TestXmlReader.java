package test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.ponfee.commons.xml.XmlReader;

public class TestXmlReader {

    public static void main(String[] args) throws InterruptedException {
        XmlReader reader = XmlReader.create("<alipay><is_success>T</is_success><request><param name=\"trade_no\">2015031800001000970051218861</param><param name=\"_input_charset\">UTF-8</param><param name=\"service\">single_trade_query</param><param name=\"partner\">2088411293539364</param><param name=\"out_trade_no\">0515abcdbcd3831256</param></request><response><trade><body>abcd</body><buyer_email>1034792318@qq.com</buyer_email><buyer_id>2088212560609971</buyer_id><discount>0.00</discount><flag_trade_locked>0</flag_trade_locked><gmt_create>2015-03-18 15:22:26</gmt_create><gmt_last_modified_time>2015-03-18 15:43:11</gmt_last_modified_time><gmt_payment>2015-03-18 15:22:36</gmt_payment><gmt_refund>2015-03-18 15:43:11</gmt_refund><is_total_fee_adjust>F</is_total_fee_adjust><operator_role>B</operator_role><out_trade_no>20150318151412944</out_trade_no><payment_type>8</payment_type><price>0.10</price><quantity>1</quantity><refund_fee>0.03</refund_fee><refund_flow_type>1</refund_flow_type><refund_id>97622780097</refund_id><refund_status>REFUND_SUCCESS</refund_status><seller_email>piaokuan03@dfasfds.com</seller_email><seller_id>2088411293539364</seller_id><subject>abcd全国汽车票</subject><to_buyer_fee>0.03</to_buyer_fee><to_seller_fee>0.10</to_seller_fee><total_fee>0.10</total_fee><trade_no>2015031800001000970051218861</trade_no><trade_status>TRADE_SUCCESS</trade_status><use_coupon>F</use_coupon></trade></response><sign>39350dc1dd1a85815bfc2f153ae436e1</sign><sign_type>MD5</sign_type></alipay>");
        System.out.println(reader.getNodeFloat("price"));
        AtomicInteger flag = new AtomicInteger();
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            list.add(new Thread(() -> {
                while (flag.getAndIncrement() < 50000) {
                    reader.evaluate("//alipay/request/param/@name");
                    reader.evaluate("//alipay/request/param[@name='trade_no']");
                    reader.evaluate("//alipay/response/trade/seller_email");
                    //System.out.println(reader.evaluate("//alipay/request/param/@name"));
                    //System.out.println(reader.evaluate("//alipay/request/param[@name='trade_no']"));
                    //System.out.println(reader.evaluate("//alipay/response/trade/seller_email"));
                }
            }));
        }
        long start = System.currentTimeMillis();
        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
