package code.ponfee.commons.util;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

/**
 * 币种枚举类
 *
 * <li>GB2659-94
 * <li>http://en.wikipedia.org/wiki/ISO_4217
 * <li>币种符号权威参考：http://www.xe.com/symbols.php
 * <li>世界各国和地区名称代码：https://baike.baidu.com/item/%E4%B8%96%E7%95%8C%E5%90%84%E5%9B%BD%E5%92%8C%E5%9C%B0%E5%8C%BA%E5%90%8D%E7%A7%B0%E4%BB%A3%E7%A0%81/6560023?fr=aladdin
 *
 * @author Ponfee
 */
public enum Currencys {

    // ----------------------------------------------------------已确认的币种符号
    /** 人民币 [￥] */
    CNY(new String(new char[] { 0xffe5 })),

    /** 美元 [US $] */
    USD(new String(new char[] { 0x55, 0x53, 0x20, 0x24 })),

    /** 港元 [HK$] */
    HKD(new String(new char[] { 0x48, 0x4b, 0x24 })),

    /** 台币 [NT$] */
    TWD(new String(new char[] { 0x4e, 0x54, 0x24 })),

    /** 欧元 [€] */
    EUR(new String(new char[] { 0x20ac })),

    /** 英镑 [￡] */
    GBP(new String(new char[] { 0xffe1 })),

    /** 日元 [￥] */
    JPY(new String(new char[] { 0xa5 })),

    /** 巴西雷亚尔 [R$] */
    BRL(new String(new char[] { 0x52, 0x24 })),

    /** 卢布 [руб.] */
    RUB(new String(new char[] { 0x440, 0x443, 0x431, 0x2e })),

    /** 澳元 [AU $] */
    AUD(new String(new char[] { 0x41, 0x55, 0x20, 0x24 })),

    /** 加元 [C$]*/
    CAD(new String(new char[] { 0x43, 0x24 })),

    /** 印度卢比 [Rs.] */
    INR(new String(new char[] { 0x52, 0x73, 0x2e })),

    /** 乌克兰里夫纳 [грн.] */
    UAH(new String(new char[] { 0x433, 0x440, 0x43d, 0x2e })),

    /** 墨西哥比索 [MXN$] */
    MXN(new String(new char[] { 0x4d, 0x58, 0x4e, 0x24 })),

    /** 瑞士法郎 [ch] */
    CHF(new String(new char[] { 0x63, 0x68 })),

    /** 新加坡元 [SG$] */
    SGD(new String(new char[] { 0x53, 0x47, 0x24 })),

    /** 波兰兹罗提 [zł] */
    PLN(new String(new char[] { 0x7a, 0x142 })),

    /** 瑞典克朗 [SE kr] */
    SEK(new String(new char[] { 0x53, 0x45, 0x20, 0x6b, 0x72 })),

    /** 智利比索 [CL $] */
    CLP(new String(new char[] { 0x43, 0x4c, 0x20, 0x24 })),

    /** 韩元 [₩] */
    KRW(new String(new char[] { 0x20a9 })),

    /** 肯尼亚先令 [KSh] */
    KES(new String(new char[] { 0x4b, 0x53, 0x68 })),

    /** 澳门元 [MOP] */
    MOP(new String(new char[] { 0x4d, 0x4f, 0x50 })),

    /** 印度尼西亚卢比 [Rp] */
    IDR(new String(new char[] { 0x52, 0x70 })),

    /** 沙特里亚尔 [﷼] */
    SAR(new String(new char[] { 0xfdfc })),

    /** 保加利亚列弗 [лв] */
    BGN(new String(new char[] { 0x43b, 0x432 })),

    /** 罗马尼亚新列伊 [lei] */
    RON(new String(new char[] { 0x6c, 0x65, 0x69 })),

    /** 捷克克朗 [Kč] */
    CZK(new String(new char[] { 0x4b, 0x10d })),

    /** 匈牙利福林 [Ft] */
    HUF(new String(new char[] { 0x46, 0x74 })),

    /** 越南盾 [₫] */
    VND(new String(new char[] { 0x20ab })),

    /** 马来西亚林吉特 [RM] */
    MYR(new String(new char[] { 0x52, 0x4d })),

    /** 菲律宾比索 [₱] */
    PHP(new String(new char[] { 0x20b1 })),

    /** 泰铢 [฿] */
    THB(new String(new char[] { 0xe3f })),

    /** 巴基斯坦卢比 [₨] */
    PKR(new String(new char[] { 0x20a8 })),

    /** 挪威克朗 [kr] */
    NOK(new String(new char[] { 0x6b, 0x72 })),

    /** 丹麦克朗 [kr] */
    DKK(new String(new char[] { 0x6b, 0x72 })),

    // ----------------------------------------------------------未确认的币种符号
    /** 阿联酋迪拉姆 [AED] */
    AED(new String(new char[] { 0x41, 0x45, 0x44 })),

    /** 阿富汗尼 [؋] */
    AFN(new String(new char[] { 0x60b })),

    /** 阿尔巴尼列克 [Lek] */
    ALL(new String(new char[] { 0x4c, 0x65, 0x6b })),

    /** 亚美尼亚德拉姆 [AMD] */
    AMD(new String(new char[] { 0x41, 0x4d, 0x44 })),

    /** 荷兰盾 [ƒ] */
    ANG(new String(new char[] { 0x192 })),

    /** 安哥拉宽扎 [AOA] */
    AOA(new String(new char[] { 0x41, 0x4f, 0x41 })),

    /** 阿根廷比索 [$] */
    ARS(new String(new char[] { 0x24 })),

    /** 阿鲁巴或荷兰盾 [ƒ] */
    AWG(new String(new char[] { 0x192 })),

    /** 阿塞拜疆新马纳特 [ман] */
    AZN(new String(new char[] { 0x43c, 0x430, 0x43d })),

    /** 波斯尼亚可兑换马尔卡 [KM] */
    BAM(new String(new char[] { 0x4b, 0x4d })),

    /** 巴巴多斯元 [$] */
    BBD(new String(new char[] { 0x24 })),

    /** 孟加拉国塔卡 [BDT] */
    BDT(new String(new char[] { 0x42, 0x44, 0x54 })),

    /** 巴林第纳尔 [BHD] */
    BHD(new String(new char[] { 0x42, 0x48, 0x44 })),

    /** 布隆迪法郎 [BIF] */
    BIF(new String(new char[] { 0x42, 0x49, 0x46 })),

    /** 百慕大元 [$] */
    BMD(new String(new char[] { 0x24 })),

    /** 文莱元 [$] */
    BND(new String(new char[] { 0x24 })),

    /** 玻利维亚诺 [$b] */
    BOB(new String(new char[] { 0x24, 0x62 })),

    /** 巴哈马元 [$] */
    BSD(new String(new char[] { 0x24 })),

    /** 不丹努尔特鲁姆 [BTN] */
    BTN(new String(new char[] { 0x42, 0x54, 0x4e })),

    /** 博茨瓦纳普拉 [P] */
    BWP(new String(new char[] { 0x50 })),

    /** 白俄罗斯卢布 [p.] */
    BYR(new String(new char[] { 0x70, 0x2e })),

    /** 伯利兹元 [BZ$] */
    BZD(new String(new char[] { 0x42, 0x5a, 0x24 })),

    /** 刚果法郎 [CDF] */
    CDF(new String(new char[] { 0x43, 0x44, 0x46 })),

    /** 哥伦比亚比索 [$] */
    COP(new String(new char[] { 0x24 })),

    /** 哥斯达黎加科朗 [₡] */
    CRC(new String(new char[] { 0x20a1 })),

    /** 古巴可兑换比索 [CUC] */
    CUC(new String(new char[] { 0x43, 0x55, 0x43 })),

    /** 古巴比索 [₱] */
    CUP(new String(new char[] { 0x20b1 })),

    /** 佛得角埃斯库多 [CVE] */
    CVE(new String(new char[] { 0x43, 0x56, 0x45 })),

    /** 吉布提法郎 [DJF] */
    DJF(new String(new char[] { 0x44, 0x4a, 0x46 })),

    /** 多米尼加比索 [RD$] */
    DOP(new String(new char[] { 0x52, 0x44, 0x24 })),

    /** 阿尔及利亚第纳尔 [DZD] */
    DZD(new String(new char[] { 0x44, 0x5a, 0x44 })),

    /** 埃及镑 [£] */
    EGP(new String(new char[] { 0xa3 })),

    /** 厄立特里亚纳克法 [ERN] */
    ERN(new String(new char[] { 0x45, 0x52, 0x4e })),

    /** 埃塞俄比亚比尔 [ETB] */
    ETB(new String(new char[] { 0x45, 0x54, 0x42 })),

    /** 斐济元 [$] */
    FJD(new String(new char[] { 0x24 })),

    /** 福克兰群岛镑 [£] */
    FKP(new String(new char[] { 0xa3 })),

    /** 格鲁吉亚拉里 [GEL] */
    GEL(new String(new char[] { 0x47, 0x45, 0x4c })),

    /** 加纳塞地 [GHS] */
    GHS(new String(new char[] { 0x47, 0x48, 0x53 })),

    /** 直布罗陀镑 [£] */
    GIP(new String(new char[] { 0xa3 })),

    /** 冈比亚达拉西 [GMD] */
    GMD(new String(new char[] { 0x47, 0x4d, 0x44 })),

    /** 几内亚法郎 [GNF] */
    GNF(new String(new char[] { 0x47, 0x4e, 0x46 })),

    /** 危地马拉格查尔 [Q] */
    GTQ(new String(new char[] { 0x51 })),

    /** 圭亚那元 [$] */
    GYD(new String(new char[] { 0x24 })),

    /** 洪都拉斯伦皮拉 [L] */
    HNL(new String(new char[] { 0x4c })),

    /** 克罗地亚库纳 [kn] */
    HRK(new String(new char[] { 0x6b, 0x6e })),

    /** 海地古德 [HTG] */
    HTG(new String(new char[] { 0x48, 0x54, 0x47 })),

    /** 以色列谢克尔 [₪] */
    ILS(new String(new char[] { 0x20aa })),

    /** 伊拉克第纳尔 [IQD] */
    IQD(new String(new char[] { 0x49, 0x51, 0x44 })),

    /** 伊朗里亚尔 [﷼] */
    IRR(new String(new char[] { 0xfdfc })),

    /** 冰岛克朗 [kr] */
    ISK(new String(new char[] { 0x6b, 0x72 })),

    /** 牙买加元 [J$] */
    JMD(new String(new char[] { 0x4a, 0x24 })),

    /** 约旦第纳尔 [JOD] */
    JOD(new String(new char[] { 0x4a, 0x4f, 0x44 })),

    /** 吉尔吉斯斯坦索姆 [лв] */
    KGS(new String(new char[] { 0x43b, 0x432 })),

    /** 柬埔寨瑞尔 [៛] */
    KHR(new String(new char[] { 0x17db })),

    /** 科摩罗法郎 [KMF] */
    KMF(new String(new char[] { 0x4b, 0x4d, 0x46 })),

    /** 朝鲜元 [₩] */
    KPW(new String(new char[] { 0x20a9 })),

    /** 科威特第纳尔 [KWD] */
    KWD(new String(new char[] { 0x4b, 0x57, 0x44 })),

    /** 开曼元 [$] */
    KYD(new String(new char[] { 0x24 })),

    /** 哈萨克斯坦坚戈 [лв] */
    KZT(new String(new char[] { 0x43b, 0x432 })),

    /** 老挝基普 [₭] */
    LAK(new String(new char[] { 0x20ad })),

    /** 黎巴嫩镑 [£] */
    LBP(new String(new char[] { 0xa3 })),

    /** 斯里兰卡卢比 [₨] */
    LKR(new String(new char[] { 0x20a8 })),

    /** 利比里亚元 [$] */
    LRD(new String(new char[] { 0x24 })),

    /** 巴索托洛蒂 [LSL] */
    LSL(new String(new char[] { 0x4c, 0x53, 0x4c })),

    /** 利比亚第纳尔 [LYD] */
    LYD(new String(new char[] { 0x4c, 0x59, 0x44 })),

    /** 摩洛哥迪拉姆 [MAD] */
    MAD(new String(new char[] { 0x4d, 0x41, 0x44 })),

    /** 摩尔多瓦列伊 [MDL] */
    MDL(new String(new char[] { 0x4d, 0x44, 0x4c })),

    /** 马尔加什阿里亚 [MGA] */
    MGA(new String(new char[] { 0x4d, 0x47, 0x41 })),

    /** 马其顿第纳尔 [ден] */
    MKD(new String(new char[] { 0x434, 0x435, 0x43d })),

    /** 缅元 [MMK] */
    MMK(new String(new char[] { 0x4d, 0x4d, 0x4b })),

    /** 蒙古图格里克 [₮] */
    MNT(new String(new char[] { 0x20ae })),

    /** 毛里塔尼亚乌吉亚 [MRO] */
    MRO(new String(new char[] { 0x4d, 0x52, 0x4f })),

    /** 毛里塔尼亚卢比 [₨] */
    MUR(new String(new char[] { 0x20a8 })),

    /** 马尔代夫拉菲亚 [MVR] */
    MVR(new String(new char[] { 0x4d, 0x56, 0x52 })),

    /** 马拉维克瓦查 [MWK] */
    MWK(new String(new char[] { 0x4d, 0x57, 0x4b })),

    /** 莫桑比克梅蒂卡尔 [MT] */
    MZN(new String(new char[] { 0x4d, 0x54 })),

    /** 纳米比亚元 [$] */
    NAD(new String(new char[] { 0x24 })),

    /** 尼日利亚奈拉 [₦] */
    NGN(new String(new char[] { 0x20a6 })),

    /** 尼加拉瓜科多巴 [C$] */
    NIO(new String(new char[] { 0x43, 0x24 })),

    /** 尼泊尔卢比 [₨] */
    NPR(new String(new char[] { 0x20a8 })),

    /** 新西兰元 [NZ$] */
    NZD(new String(new char[] { 0x4e, 0x5a, 0x24 })),

    /** 阿曼里亚尔 [﷼] */
    OMR(new String(new char[] { 0xfdfc })),

    /** 巴拿马巴波亚 [B/.] */
    PAB(new String(new char[] { 0x42, 0x2f, 0x2e })),

    /** 秘鲁新索尔 [S/.] */
    PEN(new String(new char[] { 0x53, 0x2f, 0x2e })),

    /** 巴布亚新几内亚基那 [PGK] */
    PGK(new String(new char[] { 0x50, 0x47, 0x4b })),

    /** 巴拉圭瓜拉尼 [Gs] */
    PYG(new String(new char[] { 0x47, 0x73 })),

    /** 卡塔尔里亚尔 [﷼] */
    QAR(new String(new char[] { 0xfdfc })),

    /** 塞尔维亚第纳尔 [Дин.] */
    RSD(new String(new char[] { 0x414, 0x438, 0x43d, 0x2e })),

    /** 卢旺达法郎 [RWF] */
    RWF(new String(new char[] { 0x52, 0x57, 0x46 })),

    /** 所罗门群岛元 [$] */
    SBD(new String(new char[] { 0x24 })),

    /** 塞舌尔卢比 [₨] */
    SCR(new String(new char[] { 0x20a8 })),

    /** 苏丹镑 [SDG] */
    SDG(new String(new char[] { 0x53, 0x44, 0x47 })),

    /** 圣赫勒拿镑 [£] */
    SHP(new String(new char[] { 0xa3 })),

    /** 塞拉利昂利昂 [SLL] */
    SLL(new String(new char[] { 0x53, 0x4c, 0x4c })),

    /** 索马里先令 [S] */
    SOS(new String(new char[] { 0x53 })),

    /** 苏里南元 [$] */
    SRD(new String(new char[] { 0x24 })),

    /** 圣多美多布拉 [STD] */
    STD(new String(new char[] { 0x53, 0x54, 0x44 })),

    /** 叙利亚镑 [£] */
    SYP(new String(new char[] { 0xa3 })),

    /** 斯威士兰里兰吉尼 [SZL] */
    SZL(new String(new char[] { 0x53, 0x5a, 0x4c })),

    /** 塔吉克斯坦索莫尼 [TJS] */
    TJS(new String(new char[] { 0x54, 0x4a, 0x53 })),

    /** 土库曼斯坦马纳特 [TMT] */
    TMT(new String(new char[] { 0x54, 0x4d, 0x54 })),

    /** 突尼斯第纳尔 [TND] */
    TND(new String(new char[] { 0x54, 0x4e, 0x44 })),

    /** 汤加潘加 [TOP] */
    TOP(new String(new char[] { 0x54, 0x4f, 0x50 })),

    /** 土耳其里拉 [TRY] */
    TRY(new String(new char[] { 0x54, 0x52, 0x59 })),

    /** 特立尼达元 [TT$] */
    TTD(new String(new char[] { 0x54, 0x54, 0x24 })),

    /** 坦桑尼亚先令 [TZS] */
    TZS(new String(new char[] { 0x54, 0x5a, 0x53 })),

    /** 乌干达先令 [UGX] */
    UGX(new String(new char[] { 0x55, 0x47, 0x58 })),

    /** 乌拉圭比索 [$U] */
    UYU(new String(new char[] { 0x24, 0x55 })),

    /** 乌兹别克斯坦索姆 [лв] */
    UZS(new String(new char[] { 0x43b, 0x432 })),

    /** 委内瑞拉玻利瓦尔 [Bs] */
    VEF(new String(new char[] { 0x42, 0x73 })),

    /** 瓦努阿图瓦图 [VUV] */
    VUV(new String(new char[] { 0x56, 0x55, 0x56 })),

    /** 萨摩亚塔拉 [WST] */
    WST(new String(new char[] { 0x57, 0x53, 0x54 })),

    /** 中非金融合作法郎 [XAF] */
    XAF(new String(new char[] { 0x58, 0x41, 0x46 })),

    /** 银（盎司） [XAG] */
    XAG(new String(new char[] { 0x58, 0x41, 0x47 })),

    /** 金（盎司） [XAU] */
    XAU(new String(new char[] { 0x58, 0x41, 0x55 })),

    /** 东加勒比元 [$] */
    XCD(new String(new char[] { 0x24 })),

    /** 国际货币基金组织特别提款权 [XDR] */
    XDR(new String(new char[] { 0x58, 0x44, 0x52 })),

    /** CFA 法郎 [XOF] */
    XOF(new String(new char[] { 0x58, 0x4f, 0x46 })),

    /** 钯（盎司） [XPD] */
    XPD(new String(new char[] { 0x58, 0x50, 0x44 })),

    /** CFP 法郎 [XPF] */
    XPF( new String(new char[] { 0x58, 0x50, 0x46 })),

    /** 铂（盎司） [XPT] */
    XPT(new String(new char[] { 0x58, 0x50, 0x54 })),

    /** 也门里亚尔 [﷼] */
    YER(new String(new char[] { 0xfdfc })),

    /** 南非兰特 [R] */
    ZAR(new String(new char[] { 0x52 })),

    ;

    private static final Map<String, Currencys> CODES   = Enums.toMap(Currencys.class, Currencys::code);
    private static final Map<String, Currencys> NUMERIC = Enums.toMap(Currencys.class, Currencys::numeric);

    /** 币种代码 */
    private final String code;

    /** 币种符号 */
    private final String symbol;

    /** 世界各国和地区名称数字（代码） */
    private final String numeric;

    /** 币种 */
    private final Currency currency;

    /**
     * 构造函数
     *
     * <p>this.currency.getDisplayName(Locale.CHINA); -> 人民币</p>
     * <p>this.currency.getDisplayName(Locale.US)     -> Chinese Yuan</p>
     * <p>this.currency.getSymbol(Locale.CHINA)       -> ￥</p>
     *
     * @param symbol 币种符号
     */
    Currencys(String symbol) {
        this.code = this.name();
        this.symbol = symbol;
        this.currency = Currency.getInstance(this.code);
        this.numeric = String.format("%03d",this.currency.getNumericCode());
    }

    /**
     * @return code
     */
    public String code() {
        return code;
    }

    /**
     * @return numeric
     */
    public String numeric() {
        return numeric;
    }

    /**
     * @return symbol
     */
    public String symbol() {
        return symbol;
    }

    /**
     * @return object of java currency
     */
    public Currency currency() {
        return currency;
    }

    // --------------------------------------------------------------of methods
    /**
     * Returns Currencys by code
     *
     * @param code the code
     * @return Currencys
     */
    public static Currencys ofCode(String code) {
        return CODES.get(code);
    }

    /**
     * Returns Currencys by numeric
     *
     * @param numeric the numeric
     * @return Currencys
     */
    public static Currencys ofNumeric(String numeric) {
        return NUMERIC.get(numeric);
    }

    /**
     * Returns Currencys by currency
     *
     * @param currency the currency
     * @return Currencys
     */
    public static Currencys of(Currency currency) {
        return CODES.get(currency.getCurrencyCode());
    }

}
