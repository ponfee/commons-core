package code.ponfee.commons.util;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一的币种定义枚举
 * <li>GB2659-94
 * <li>http://en.wikipedia.org/wiki/ISO_4217
 * <li>币种符号权威参考：http://www.xe.com/symbols.php
 * <li>世界各国和地区名称代码：https://baike.baidu.com/item/%E4%B8%96%E7%95%8C%E5%90%84%E5%9B%BD%E5%92%8C%E5%9C%B0%E5%8C%BA%E5%90%8D%E7%A7%B0%E4%BB%A3%E7%A0%81/6560023?fr=aladdin
 */
public enum Currencys {

    /* -------------------已确认的币种符号-------------------------- */

    /** 人民币 [￥] */
    CNY("CNY", "156", new String(new char[] { 0xffe5 })),

    /** 美元 [US $] */
    USD("USD", "840", new String(new char[] { 0x55, 0x53, 0x20, 0x24 })),

    /** 港元 [HK$] */
    HKD("HKD", "344", new String(new char[] { 0x48, 0x4b, 0x24 })),

    /** 台币 [NT$] */
    TWD("TWD", "901", new String(new char[] { 0x4e, 0x54, 0x24 })),

    /** 欧元 [€] */
    EUR("EUR", "978", new String(new char[] { 0x20ac })),

    /** 英镑 [￡] */
    GBP("GBP", "826", new String(new char[] { 0xffe1 })),

    /** 日元 [￥] */
    JPY("JPY", "392", new String(new char[] { 0xa5 })),

    /** 巴西雷亚尔 [R$] */
    BRL("BRL", "986", new String(new char[] { 0x52, 0x24 })),

    /** 卢布 [руб.] */
    RUB("RUB", "643", new String(new char[] { 0x440, 0x443, 0x431, 0x2e })),

    /** 澳元 [AU $] */
    AUD("AUD", "036", new String(new char[] { 0x41, 0x55, 0x20, 0x24 })),

    /** 加元 [C$]*/
    CAD("CAD", "124", new String(new char[] { 0x43, 0x24 })),

    /** 印度卢比 [Rs.] */
    INR("INR", "356", new String(new char[] { 0x52, 0x73, 0x2e })),

    /** 乌克兰里夫纳 [грн.] */
    UAH("UAH", "980", new String(new char[] { 0x433, 0x440, 0x43d, 0x2e })),

    /** 墨西哥比索 [MXN$] */
    MXN("MXN", "484", new String(new char[] { 0x4d, 0x58, 0x4e, 0x24 })),

    /** 瑞士法郎 [ch] */
    CHF("CHF", "756", new String(new char[] { 0x63, 0x68 })),

    /** 新加坡元 [SG$] */
    SGD("SGD", "702", new String(new char[] { 0x53, 0x47, 0x24 })),

    /** 波兰兹罗提 [zł] */
    PLN("PLN", "985", new String(new char[] { 0x7a, 0x142 })),

    /** 瑞典克朗 [SE kr] */
    SEK("SEK", "752", new String(new char[] { 0x53, 0x45, 0x20, 0x6b, 0x72 })),

    /** 智利比索 [CL $] */
    CLP("CLP", "152", new String(new char[] { 0x43, 0x4c, 0x20, 0x24 })),

    /** 韩元 [₩] */
    KRW("KRW", "410", new String(new char[] { 0x20a9 })),

    /** 肯尼亚先令 [KSh] */
    KES("KES", "404", new String(new char[] { 0x4b, 0x53, 0x68 })),

    /** 澳门元 [MOP] */
    MOP("MOP", "446", new String(new char[] { 0x4d, 0x4f, 0x50 })),

    /** 印度尼西亚卢比 [Rp] */
    IDR("IDR", "360", new String(new char[] { 0x52, 0x70 })),

    /** 沙特里亚尔 [﷼] */
    SAR("SAR", "682", new String(new char[] { 0xfdfc })),

    /** 保加利亚列弗 [лв] */
    BGN("BGN", "975", new String(new char[] { 0x43b, 0x432 })),

    /** 罗马尼亚新列伊 [lei] */
    RON("RON", "946", new String(new char[] { 0x6c, 0x65, 0x69 })),

    /** 捷克克朗 [Kč] */
    CZK("CZK", "203", new String(new char[] { 0x4b, 0x10d })),

    /** 匈牙利福林 [Ft] */
    HUF("HUF", "348", new String(new char[] { 0x46, 0x74 })),

    /** 越南盾 [₫] */
    VND("VND", "704", new String(new char[] { 0x20ab })),

    /** 马来西亚林吉特 [RM] */
    MYR("MYR", "458", new String(new char[] { 0x52, 0x4d })),

    /** 菲律宾比索 [₱] */
    PHP("PHP", "608", new String(new char[] { 0x20b1 })),

    /** 泰铢 [฿] */
    THB("THB", "764", new String(new char[] { 0xe3f })),

    /** 巴基斯坦卢比 [₨] */
    PKR("PKR", "586", new String(new char[] { 0x20a8 })),

    /** 挪威克朗 [kr] */
    NOK("NOK", "578", new String(new char[] { 0x6b, 0x72 })),

    /** 丹麦克朗 [kr] */
    DKK("DKK", "208", new String(new char[] { 0x6b, 0x72 })),

    /* -------------------未确认的币种符号-------------------------- */

    /** 阿联酋迪拉姆 [AED] */
    AED("AED", "784", new String(new char[] { 0x41, 0x45, 0x44 })),

    /** 阿富汗尼 [؋] */
    AFN("AFN", "971", new String(new char[] { 0x60b })),

    /** 阿尔巴尼列克 [Lek] */
    ALL("ALL", "008", new String(new char[] { 0x4c, 0x65, 0x6b })),

    /** 亚美尼亚德拉姆 [AMD] */
    AMD("AMD", "051", new String(new char[] { 0x41, 0x4d, 0x44 })),

    /** 荷兰盾 [ƒ] */
    ANG("ANG", "532", new String(new char[] { 0x192 })),

    /** 安哥拉宽扎 [AOA] */
    AOA("AOA", "973", new String(new char[] { 0x41, 0x4f, 0x41 })),

    /** 阿根廷比索 [$] */
    ARS("ARS", "032", new String(new char[] { 0x24 })),

    /** 阿鲁巴或荷兰盾 [ƒ] */
    AWG("AWG", "533", new String(new char[] { 0x192 })),

    /** 阿塞拜疆新马纳特 [ман] */
    AZN("AZN", "944", new String(new char[] { 0x43c, 0x430, 0x43d })),

    /** 波斯尼亚可兑换马尔卡 [KM] */
    BAM("BAM", "977", new String(new char[] { 0x4b, 0x4d })),

    /** 巴巴多斯元 [$] */
    BBD("BBD", "052", new String(new char[] { 0x24 })),

    /** 孟加拉国塔卡 [BDT] */
    BDT("BDT", "050", new String(new char[] { 0x42, 0x44, 0x54 })),

    /** 巴林第纳尔 [BHD] */
    BHD("BHD", "048", new String(new char[] { 0x42, 0x48, 0x44 })),

    /** 布隆迪法郎 [BIF] */
    BIF("BIF", "108", new String(new char[] { 0x42, 0x49, 0x46 })),

    /** 百慕大元 [$] */
    BMD("BMD", "060", new String(new char[] { 0x24 })),

    /** 文莱元 [$] */
    BND("BND", "096", new String(new char[] { 0x24 })),

    /** 玻利维亚诺 [$b] */
    BOB("BOB", "068", new String(new char[] { 0x24, 0x62 })),

    /** 巴哈马元 [$] */
    BSD("BSD", "044", new String(new char[] { 0x24 })),

    /** 不丹努尔特鲁姆 [BTN] */
    BTN("BTN", "064", new String(new char[] { 0x42, 0x54, 0x4e })),

    /** 博茨瓦纳普拉 [P] */
    BWP("BWP", "072", new String(new char[] { 0x50 })),

    /** 白俄罗斯卢布 [p.] */
    BYR("BYR", "974", new String(new char[] { 0x70, 0x2e })),

    /** 伯利兹元 [BZ$] */
    BZD("BZD", "084", new String(new char[] { 0x42, 0x5a, 0x24 })),

    /** 刚果法郎 [CDF] */
    CDF("CDF", "976", new String(new char[] { 0x43, 0x44, 0x46 })),

    /** 哥伦比亚比索 [$] */
    COP("COP", "170", new String(new char[] { 0x24 })),

    /** 哥斯达黎加科朗 [₡] */
    CRC("CRC", "188", new String(new char[] { 0x20a1 })),

    /** 古巴可兑换比索 [CUC] */
    CUC("CUC", "931", new String(new char[] { 0x43, 0x55, 0x43 })),

    /** 古巴比索 [₱] */
    CUP("CUP", "192", new String(new char[] { 0x20b1 })),

    /** 佛得角埃斯库多 [CVE] */
    CVE("CVE", "132", new String(new char[] { 0x43, 0x56, 0x45 })),

    /** 吉布提法郎 [DJF] */
    DJF("DJF", "262", new String(new char[] { 0x44, 0x4a, 0x46 })),

    /** 多米尼加比索 [RD$] */
    DOP("DOP", "214", new String(new char[] { 0x52, 0x44, 0x24 })),

    /** 阿尔及利亚第纳尔 [DZD] */
    DZD("DZD", "012", new String(new char[] { 0x44, 0x5a, 0x44 })),

    /** 埃及镑 [£] */
    EGP("EGP", "818", new String(new char[] { 0xa3 })),

    /** 厄立特里亚纳克法 [ERN] */
    ERN("ERN", "232", new String(new char[] { 0x45, 0x52, 0x4e })),

    /** 埃塞俄比亚比尔 [ETB] */
    ETB("ETB", "230", new String(new char[] { 0x45, 0x54, 0x42 })),

    /** 斐济元 [$] */
    FJD("FJD", "242", new String(new char[] { 0x24 })),

    /** 福克兰群岛镑 [£] */
    FKP("FKP", "238", new String(new char[] { 0xa3 })),

    /** 格鲁吉亚拉里 [GEL] */
    GEL("GEL", "981", new String(new char[] { 0x47, 0x45, 0x4c })),

    /** 加纳塞地 [GHS] */
    GHS("GHS", "936", new String(new char[] { 0x47, 0x48, 0x53 })),

    /** 直布罗陀镑 [£] */
    GIP("GIP", "292", new String(new char[] { 0xa3 })),

    /** 冈比亚达拉西 [GMD] */
    GMD("GMD", "270", new String(new char[] { 0x47, 0x4d, 0x44 })),

    /** 几内亚法郎 [GNF] */
    GNF("GNF", "324", new String(new char[] { 0x47, 0x4e, 0x46 })),

    /** 危地马拉格查尔 [Q] */
    GTQ("GTQ", "320", new String(new char[] { 0x51 })),

    /** 圭亚那元 [$] */
    GYD("GYD", "328", new String(new char[] { 0x24 })),

    /** 洪都拉斯伦皮拉 [L] */
    HNL("HNL", "340", new String(new char[] { 0x4c })),

    /** 克罗地亚库纳 [kn] */
    HRK("HRK", "191", new String(new char[] { 0x6b, 0x6e })),

    /** 海地古德 [HTG] */
    HTG("HTG", "332", new String(new char[] { 0x48, 0x54, 0x47 })),

    /** 以色列谢克尔 [₪] */
    ILS("ILS", "376", new String(new char[] { 0x20aa })),

    /** 伊拉克第纳尔 [IQD] */
    IQD("IQD", "368", new String(new char[] { 0x49, 0x51, 0x44 })),

    /** 伊朗里亚尔 [﷼] */
    IRR("IRR", "364", new String(new char[] { 0xfdfc })),

    /** 冰岛克朗 [kr] */
    ISK("ISK", "352", new String(new char[] { 0x6b, 0x72 })),

    /** 牙买加元 [J$] */
    JMD("JMD", "388", new String(new char[] { 0x4a, 0x24 })),

    /** 约旦第纳尔 [JOD] */
    JOD("JOD", "400", new String(new char[] { 0x4a, 0x4f, 0x44 })),

    /** 吉尔吉斯斯坦索姆 [лв] */
    KGS("KGS", "417", new String(new char[] { 0x43b, 0x432 })),

    /** 柬埔寨瑞尔 [៛] */
    KHR("KHR", "116", new String(new char[] { 0x17db })),

    /** 科摩罗法郎 [KMF] */
    KMF("KMF", "174", new String(new char[] { 0x4b, 0x4d, 0x46 })),

    /** 朝鲜元 [₩] */
    KPW("KPW", "408", new String(new char[] { 0x20a9 })),

    /** 科威特第纳尔 [KWD] */
    KWD("KWD", "414", new String(new char[] { 0x4b, 0x57, 0x44 })),

    /** 开曼元 [$] */
    KYD("KYD", "136", new String(new char[] { 0x24 })),

    /** 哈萨克斯坦坚戈 [лв] */
    KZT("KZT", "398", new String(new char[] { 0x43b, 0x432 })),

    /** 老挝基普 [₭] */
    LAK("LAK", "418", new String(new char[] { 0x20ad })),

    /** 黎巴嫩镑 [£] */
    LBP("LBP", "422", new String(new char[] { 0xa3 })),

    /** 斯里兰卡卢比 [₨] */
    LKR("LKR", "144", new String(new char[] { 0x20a8 })),

    /** 利比里亚元 [$] */
    LRD("LRD", "430", new String(new char[] { 0x24 })),

    /** 巴索托洛蒂 [LSL] */
    LSL("LSL", "426", new String(new char[] { 0x4c, 0x53, 0x4c })),

    /** 利比亚第纳尔 [LYD] */
    LYD("LYD", "434", new String(new char[] { 0x4c, 0x59, 0x44 })),

    /** 摩洛哥迪拉姆 [MAD] */
    MAD("MAD", "504", new String(new char[] { 0x4d, 0x41, 0x44 })),

    /** 摩尔多瓦列伊 [MDL] */
    MDL("MDL", "498", new String(new char[] { 0x4d, 0x44, 0x4c })),

    /** 马尔加什阿里亚 [MGA] */
    MGA("MGA", "969", new String(new char[] { 0x4d, 0x47, 0x41 })),

    /** 马其顿第纳尔 [ден] */
    MKD("MKD", "807", new String(new char[] { 0x434, 0x435, 0x43d })),

    /** 缅元 [MMK] */
    MMK("MMK", "104", new String(new char[] { 0x4d, 0x4d, 0x4b })),

    /** 蒙古图格里克 [₮] */
    MNT("MNT", "496", new String(new char[] { 0x20ae })),

    /** 毛里塔尼亚乌吉亚 [MRO] */
    MRO("MRO", "478", new String(new char[] { 0x4d, 0x52, 0x4f })),

    /** 毛里塔尼亚卢比 [₨] */
    MUR("MUR", "480", new String(new char[] { 0x20a8 })),

    /** 马尔代夫拉菲亚 [MVR] */
    MVR("MVR", "462", new String(new char[] { 0x4d, 0x56, 0x52 })),

    /** 马拉维克瓦查 [MWK] */
    MWK("MWK", "454", new String(new char[] { 0x4d, 0x57, 0x4b })),

    /** 莫桑比克梅蒂卡尔 [MT] */
    MZN("MZN", "943", new String(new char[] { 0x4d, 0x54 })),

    /** 纳米比亚元 [$] */
    NAD("NAD", "516", new String(new char[] { 0x24 })),

    /** 尼日利亚奈拉 [₦] */
    NGN("NGN", "566", new String(new char[] { 0x20a6 })),

    /** 尼加拉瓜科多巴 [C$] */
    NIO("NIO", "558", new String(new char[] { 0x43, 0x24 })),

    /** 尼泊尔卢比 [₨] */
    NPR("NPR", "524", new String(new char[] { 0x20a8 })),

    /** 新西兰元 [NZ$] */
    NZD("NZD", "554", new String(new char[] { 0x4e, 0x5a, 0x24 })),

    /** 阿曼里亚尔 [﷼] */
    OMR("OMR", "512", new String(new char[] { 0xfdfc })),

    /** 巴拿马巴波亚 [B/.] */
    PAB("PAB", "590", new String(new char[] { 0x42, 0x2f, 0x2e })),

    /** 秘鲁新索尔 [S/.] */
    PEN("PEN", "604", new String(new char[] { 0x53, 0x2f, 0x2e })),

    /** 巴布亚新几内亚基那 [PGK] */
    PGK("PGK", "598", new String(new char[] { 0x50, 0x47, 0x4b })),

    /** 巴拉圭瓜拉尼 [Gs] */
    PYG("PYG", "600", new String(new char[] { 0x47, 0x73 })),

    /** 卡塔尔里亚尔 [﷼] */
    QAR("QAR", "634", new String(new char[] { 0xfdfc })),

    /** 塞尔维亚第纳尔 [Дин.] */
    RSD("RSD", "941", new String(new char[] { 0x414, 0x438, 0x43d, 0x2e })),

    /** 卢旺达法郎 [RWF] */
    RWF("RWF", "646", new String(new char[] { 0x52, 0x57, 0x46 })),

    /** 所罗门群岛元 [$] */
    SBD("SBD", "090", new String(new char[] { 0x24 })),

    /** 塞舌尔卢比 [₨] */
    SCR("SCR", "690", new String(new char[] { 0x20a8 })),

    /** 苏丹镑 [SDG] */
    SDG("SDG", "938", new String(new char[] { 0x53, 0x44, 0x47 })),

    /** 圣赫勒拿镑 [£] */
    SHP("SHP", "654", new String(new char[] { 0xa3 })),

    /** 塞拉利昂利昂 [SLL] */
    SLL("SLL", "694", new String(new char[] { 0x53, 0x4c, 0x4c })),

    /** 索马里先令 [S] */
    SOS("SOS", "706", new String(new char[] { 0x53 })),

    /** 苏里南元 [$] */
    SRD("SRD", "968", new String(new char[] { 0x24 })),

    /** 圣多美多布拉 [STD] */
    STD("STD", "678", new String(new char[] { 0x53, 0x54, 0x44 })),

    /** 叙利亚镑 [£] */
    SYP("SYP", "760", new String(new char[] { 0xa3 })),

    /** 斯威士兰里兰吉尼 [SZL] */
    SZL("SZL", "748", new String(new char[] { 0x53, 0x5a, 0x4c })),

    /** 塔吉克斯坦索莫尼 [TJS] */
    TJS("TJS", "972", new String(new char[] { 0x54, 0x4a, 0x53 })),

    /** 土库曼斯坦马纳特 [TMT] */
    TMT("TMT", "934", new String(new char[] { 0x54, 0x4d, 0x54 })),

    /** 突尼斯第纳尔 [TND] */
    TND("TND", "788", new String(new char[] { 0x54, 0x4e, 0x44 })),

    /** 汤加潘加 [TOP] */
    TOP("TOP", "776", new String(new char[] { 0x54, 0x4f, 0x50 })),

    /** 土耳其里拉 [TRY] */
    TRY("TRY", "949", new String(new char[] { 0x54, 0x52, 0x59 })),

    /** 特立尼达元 [TT$] */
    TTD("TTD", "780", new String(new char[] { 0x54, 0x54, 0x24 })),

    /** 坦桑尼亚先令 [TZS] */
    TZS("TZS", "834", new String(new char[] { 0x54, 0x5a, 0x53 })),

    /** 乌干达先令 [UGX] */
    UGX("UGX", "800", new String(new char[] { 0x55, 0x47, 0x58 })),

    /** 乌拉圭比索 [$U] */
    UYU("UYU", "858", new String(new char[] { 0x24, 0x55 })),

    /** 乌兹别克斯坦索姆 [лв] */
    UZS("UZS", "860", new String(new char[] { 0x43b, 0x432 })),

    /** 委内瑞拉玻利瓦尔 [Bs] */
    VEF("VEF", "937", new String(new char[] { 0x42, 0x73 })),

    /** 瓦努阿图瓦图 [VUV] */
    VUV("VUV", "548", new String(new char[] { 0x56, 0x55, 0x56 })),

    /** 萨摩亚塔拉 [WST] */
    WST("WST", "882", new String(new char[] { 0x57, 0x53, 0x54 })),

    /** 中非金融合作法郎 [XAF] */
    XAF("XAF", "950", new String(new char[] { 0x58, 0x41, 0x46 })),

    /** 银（盎司） [XAG] */
    XAG("XAG", "961", new String(new char[] { 0x58, 0x41, 0x47 })),

    /** 金（盎司） [XAU] */
    XAU("XAU", "959", new String(new char[] { 0x58, 0x41, 0x55 })),

    /** 东加勒比元 [$] */
    XCD("XCD", "951", new String(new char[] { 0x24 })),

    /** 国际货币基金组织特别提款权 [XDR] */
    XDR("XDR", "960", new String(new char[] { 0x58, 0x44, 0x52 })),

    /** CFA 法郎 [XOF] */
    XOF("XOF", "952", new String(new char[] { 0x58, 0x4f, 0x46 })),

    /** 钯（盎司） [XPD] */
    XPD("XPD", "964", new String(new char[] { 0x58, 0x50, 0x44 })),

    /** CFP 法郎 [XPF] */
    XPF("XPF", "953", new String(new char[] { 0x58, 0x50, 0x46 })),

    /** 铂（盎司） [XPT] */
    XPT("XPT", "962", new String(new char[] { 0x58, 0x50, 0x54 })),

    /** 也门里亚尔 [﷼] */
    YER("YER", "886", new String(new char[] { 0xfdfc })),

    /** 南非兰特 [R] */
    ZAR("ZAR", "710", new String(new char[] { 0x52 })),

    ;

    /** 币种代码 */
    private final String code;

    /** 世界各国和地区名称代码 */
    private final String value;

    /** 币种符号 */
    private final String label;

    /** 币种 */
    private final Currency currency;

    /**
     * 构造函数
     *
     * @param code   币种代码
     * @param value  世界各国和地区名称代码
     * @param label  币种符号
     */
    Currencys(String code, String value, String label) {
        this.code = code;
        this.value = value;
        this.label = label;
        this.currency = Currency.getInstance(code);
        Hide.CODES.put(this.code, this);
        Hide.VALUES.put(this.value, this);
    }

    /**
     * @return currency code
     */
    public String code() {
        return code;
    }

    /**
     * @return currency value
     */
    public String value() {
        return value;
    }

    /**
     * @return currency label
     */
    public String label() {
        return label;
    }

    /**
     * @return object of java currency
     */
    public Currency currency() {
        return currency;
    }

    /**
     * Returns Currencys by currency code
     *
     * @param code the currency code
     * @return Currencys
     */
    public static Currencys ofCode(String code) {
        return Hide.CODES.get(code);
    }

    /**
     * Returns Currencys by currency value
     *
     * @param value the currency value
     * @return Currencys
     */
    public static Currencys ofValue(String value) {
        return Hide.VALUES.get(value);
    }

    private static class Hide {
        private static final Map<String, Currencys> CODES = new HashMap<>();
        private static final Map<String, Currencys> VALUES = new HashMap<>();
    }

}
