/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.cert;

/**
  * <pre>
  *  CN=测试证书,OU=20121219,O=XXXCA,L=深圳市,ST=广东省,C=CN
  *    CN：公用名称 (Common Name) 简称：CN 字段，对于 SSL 证书，一般为网站域名；而对于代码签名证书则为申请单位名称；而对于客户端证书则为证书申请者的姓名；
  *     O：单位名称 (Organization Name) ：简称：ON（O） 字段，对于 SSL 证书，一般为网站域名；而对于代码签名证书则为申请单位名称；而对于客户端单位证书则为证书申请者所在单位名称；
  *    OU：部门或分部的名称 (Organization Util)，简称：OU字段，一般为机构代码或个人身份证号码
  *     L：所在城市 (Locality) 简称：L 字段
  *    ST：所在省份 (State/Provice) 简称：S（ST） 字段
  *     C：所在国家 (Country) 简称：C 字段，只能是国家字母缩写，如中国：CN
  * 其它字段：
  *    电子邮件 (Email) 简称：E 字段
  *    多个姓名字段 简称：G 字段
  *    介绍：Description 字段
  *    电话号码：Phone 字段，格式要求 + 国家区号 城市区号 电话号码，如： +86 732 88888888
  *    地址：STREET  字段
  *    邮政编码：PostalCode 字段
  * </pre>
  * 
 * 证书信息枚举类
 * 
 * @author Ponfee
 */
public enum X509CertInfo {

    SUBJECT_DN("主题"), ISSUER_DN("颁发者主题"), //
    CERT_SN("序列号"), VERSION("版本"), ALG_NAME("算法名称"), //
    START_TM("生效时间(格式：yyyy-MM-dd'T'HH:mm:ss.SSSZ)"), //
    END_TM("失效时间(格式：yyyy-MM-dd'T'HH:mm:ss.SSSZ)"), //
    USAGE("密钥用法(signature签名，encipherment加密)"), //
    PUBLIC_KEY("公钥(base64编码)"), //

    SUBJECT_CN("CN", "证书主体(CN)"), SUBJECT_O("O", "证书主体(O)"), //
    SUBJECT_OU("OU", "证书主体(OU)"), SUBJECT_L("L", "证书主体(L)"), //
    SUBJECT_ST("ST", "证书主体(ST)"), SUBJECT_C("C", "证书主体(C)"), //

    ISSUER_CN("CN", "证书颁发者(CN)"), ISSUER_O("O", "证书颁发者(O)"), //
    ISSUER_OU("OU", "证书颁发者(OU)"), ISSUER_L("L", "证书颁发者(L)"), //
    ISSUER_ST("ST", "证书颁发者(ST)"), ISSUER_C("C", "证书颁发者(C)"); //

    private final String attr;
    private final String desc;

    X509CertInfo(String desc) {
        this(null, desc);
    }

    X509CertInfo(String attr, String desc) {
        this.attr = attr;
        this.desc = desc;
    }

    public String attr() {
        return this.attr;
    }

    public String desc() {
        return desc;
    }

}
