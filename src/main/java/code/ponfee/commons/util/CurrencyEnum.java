package code.ponfee.commons.util;

import java.util.Currency;
import java.util.Map;

/**
 * <b>Currency enum definition.</b>
 *
 * <ul>
 *   <li><a href="http://en.wikipedia.org/wiki/ISO_4217">币种代码(维基百科，需要翻墙)</a></li>
 *   <li><a href="http://www.xe.com/symbols.php">币种符号</a></li>
 *   <li><a href="https://baike.baidu.com/item/%E4%B8%96%E7%95%8C%E5%90%84%E5%9B%BD%E5%92%8C%E5%9C%B0%E5%8C%BA%E5%90%8D%E7%A7%B0%E4%BB%A3%E7%A0%81/6560023">世界各国和地区名称代码(GB/T 2659-2000，百度百科)</a></li>
 * </ul>
 *
 * @author Ponfee
 * @see java.util.Currency
 */
public enum CurrencyEnum {

    /** 人民币 [¥], previous: {0xffe5} */
    CNY(new char[]{0xa5}),

    /** 美元 [US$] */
    USD(new char[]{0x55, 0x53, 0x24}),

    /** 港元 [HK$] */
    HKD(new char[]{0x48, 0x4b, 0x24}),

    /** 台币 [NT$] */
    TWD(new char[]{0x4e, 0x54, 0x24}),

    /** 欧元 [€] */
    EUR(new char[]{0x20ac}),

    /** 英镑 [£], previous: {0xffe1} */
    GBP(new char[]{0xa3}),

    /** 日元 [¥] */
    JPY(new char[]{0xa5}),

    /** 巴西雷亚尔 [R$] */
    BRL(new char[]{0x52, 0x24}),

    /** 俄罗斯卢布 [₽] */
    RUB(new char[]{0x20bd}),

    /** 澳元 [AU$] */
    AUD(new char[]{0x41, 0x55, 0x24}),

    /** 加元 [CA$] */
    CAD(new char[]{0x43, 0x41, 0x24}),

    /** 印度卢比 [₹], previous: {0x52, 0x73, 0x2e} */
    INR(new char[]{0x20b9}),

    /** 乌克兰里夫纳 [₴], previous: {0x433, 0x440, 0x43d, 0x2e} */
    UAH(new char[]{0x20b4}),

    /** 墨西哥比索 [MX$] */
    MXN(new char[]{0x4d, 0x58, 0x24}),

    /** 瑞士法郎 [CHF] */
    CHF(new char[]{0x43, 0x48, 0x46}),

    /** 新加坡元 [SG$] */
    SGD(new char[]{0x53, 0x47, 0x24}),

    /** 波兰兹罗提 [zł] */
    PLN(new char[]{0x7a, 0x142}),

    /** 瑞典克朗 [kr] */
    SEK(new char[]{0x6b, 0x72}),

    /** 智利比索 [CL$] */
    CLP(new char[]{0x43, 0x4c, 0x24}),

    /** 韩元 [₩] */
    KRW(new char[]{0x20a9}),

    /** 肯尼亚先令 [KSh] */
    KES(new char[]{0x4b, 0x53, 0x68}),

    /** 澳门元 [MOP] */
    MOP(new char[]{0x4d, 0x4f, 0x50}),

    /** 印度尼西亚卢比 [Rp] */
    IDR(new char[]{0x52, 0x70}),

    /** 沙特里亚尔 [﷼] */
    SAR(new char[]{0xfdfc}),

    /** 保加利亚列弗 [лв] */
    BGN(new char[]{0x43b, 0x432}),

    /** 罗马尼亚新列伊 [lei] */
    RON(new char[]{0x6c, 0x65, 0x69}),

    /** 捷克克朗 [Kč] */
    CZK(new char[]{0x4b, 0x10d}),

    /** 匈牙利福林 [Ft] */
    HUF(new char[]{0x46, 0x74}),

    /** 越南盾 [₫] */
    VND(new char[]{0x20ab}),

    /** 马来西亚林吉特 [RM] */
    MYR(new char[]{0x52, 0x4d}),

    /** 菲律宾比索 [₱] */
    PHP(new char[]{0x20b1}),

    /** 泰铢 [฿] */
    THB(new char[]{0xe3f}),

    /** 巴基斯坦卢比 [₨] */
    PKR(new char[]{0x20a8}),

    /** 挪威克朗 [kr] */
    NOK(new char[]{0x6b, 0x72}),

    /** 丹麦克朗 [kr] */
    DKK(new char[]{0x6b, 0x72}),

    /** 阿联酋迪拉姆 [AED] */
    AED(new char[]{0x41, 0x45, 0x44}),

    /** 阿富汗尼 [؋] */
    AFN(new char[]{0x60b}),

    /** 阿尔巴尼列克 [Lek] */
    ALL(new char[]{0x4c, 0x65, 0x6b}),

    /** 亚美尼亚德拉姆 [AMD] */
    AMD(new char[]{0x41, 0x4d, 0x44}),

    /** 荷兰盾 [ƒ] */
    ANG(new char[]{0x192}),

    /** 安哥拉宽扎 [AOA] */
    AOA(new char[]{0x41, 0x4f, 0x41}),

    /** 阿根廷比索 [$] */
    ARS(new char[]{0x24}),

    /** 阿鲁巴或荷兰盾 [ƒ] */
    AWG(new char[]{0x192}),

    /** 阿塞拜疆新马纳特 [₼], previous: {0x43c, 0x430, 0x43d} */
    AZN(new char[]{0x20bc}),

    /** 波斯尼亚可兑换马尔卡 [KM] */
    BAM(new char[]{0x4b, 0x4d}),

    /** 巴巴多斯元 [$] */
    BBD(new char[]{0x24}),

    /** 孟加拉国塔卡 [BDT] */
    BDT(new char[]{0x42, 0x44, 0x54}),

    /** 巴林第纳尔 [BHD] */
    BHD(new char[]{0x42, 0x48, 0x44}),

    /** 布隆迪法郎 [BIF] */
    BIF(new char[]{0x42, 0x49, 0x46}),

    /** 百慕大元 [$] */
    BMD(new char[]{0x24}),

    /** 文莱元 [$] */
    BND(new char[]{0x24}),

    /** 玻利维亚诺 [$b] */
    BOB(new char[]{0x24, 0x62}),

    /** 巴哈马元 [$] */
    BSD(new char[]{0x24}),

    /** 不丹努尔特鲁姆 [BTN] */
    BTN(new char[]{0x42, 0x54, 0x4e}),

    /** 博茨瓦纳普拉 [P] */
    BWP(new char[]{0x50}),

    /** 白俄罗斯卢布 [p.] */
    BYR(new char[]{0x70, 0x2e}),

    /** 伯利兹元 [BZ$] */
    BZD(new char[]{0x42, 0x5a, 0x24}),

    /** 刚果法郎 [CDF] */
    CDF(new char[]{0x43, 0x44, 0x46}),

    /** 哥伦比亚比索 [$] */
    COP(new char[]{0x24}),

    /** 哥斯达黎加科朗 [₡] */
    CRC(new char[]{0x20a1}),

    /** 古巴可兑换比索 [CUC] */
    CUC(new char[]{0x43, 0x55, 0x43}),

    /** 古巴比索 [₱] */
    CUP(new char[]{0x20b1}),

    /** 佛得角埃斯库多 [CVE] */
    CVE(new char[]{0x43, 0x56, 0x45}),

    /** 吉布提法郎 [DJF] */
    DJF(new char[]{0x44, 0x4a, 0x46}),

    /** 多米尼加比索 [RD$] */
    DOP(new char[]{0x52, 0x44, 0x24}),

    /** 阿尔及利亚第纳尔 [DZD] */
    DZD(new char[]{0x44, 0x5a, 0x44}),

    /** 埃及镑 [£] */
    EGP(new char[]{0xa3}),

    /** 厄立特里亚纳克法 [ERN] */
    ERN(new char[]{0x45, 0x52, 0x4e}),

    /** 埃塞俄比亚比尔 [ETB] */
    ETB(new char[]{0x45, 0x54, 0x42}),

    /** 斐济元 [$] */
    FJD(new char[]{0x24}),

    /** 福克兰群岛镑 [£] */
    FKP(new char[]{0xa3}),

    /** 格鲁吉亚拉里 [GEL] */
    GEL(new char[]{0x47, 0x45, 0x4c}),

    /** 加纳塞地 [¢], previous: {0x47, 0x48, 0x53} */
    GHS(new char[]{0xa2}),

    /** 直布罗陀镑 [£] */
    GIP(new char[]{0xa3}),

    /** 冈比亚达拉西 [GMD] */
    GMD(new char[]{0x47, 0x4d, 0x44}),

    /** 几内亚法郎 [GNF] */
    GNF(new char[]{0x47, 0x4e, 0x46}),

    /** 危地马拉格查尔 [Q] */
    GTQ(new char[]{0x51}),

    /** 圭亚那元 [$] */
    GYD(new char[]{0x24}),

    /** 洪都拉斯伦皮拉 [L] */
    HNL(new char[]{0x4c}),

    /** 克罗地亚库纳 [kn] */
    HRK(new char[]{0x6b, 0x6e}),

    /** 海地古德 [HTG] */
    HTG(new char[]{0x48, 0x54, 0x47}),

    /** 以色列谢克尔 [₪] */
    ILS(new char[]{0x20aa}),

    /** 伊拉克第纳尔 [IQD] */
    IQD(new char[]{0x49, 0x51, 0x44}),

    /** 伊朗里亚尔 [﷼] */
    IRR(new char[]{0xfdfc}),

    /** 冰岛克朗 [kr] */
    ISK(new char[]{0x6b, 0x72}),

    /** 牙买加元 [J$] */
    JMD(new char[]{0x4a, 0x24}),

    /** 约旦第纳尔 [JOD] */
    JOD(new char[]{0x4a, 0x4f, 0x44}),

    /** 吉尔吉斯斯坦索姆 [лв] */
    KGS(new char[]{0x43b, 0x432}),

    /** 柬埔寨瑞尔 [៛] */
    KHR(new char[]{0x17db}),

    /** 科摩罗法郎 [KMF] */
    KMF(new char[]{0x4b, 0x4d, 0x46}),

    /** 朝鲜元 [₩] */
    KPW(new char[]{0x20a9}),

    /** 科威特第纳尔 [KWD] */
    KWD(new char[]{0x4b, 0x57, 0x44}),

    /** 开曼元 [$] */
    KYD(new char[]{0x24}),

    /** 哈萨克斯坦坚戈 [лв] */
    KZT(new char[]{0x43b, 0x432}),

    /** 老挝基普 [₭] */
    LAK(new char[]{0x20ad}),

    /** 黎巴嫩镑 [£] */
    LBP(new char[]{0xa3}),

    /** 斯里兰卡卢比 [₨] */
    LKR(new char[]{0x20a8}),

    /** 利比里亚元 [$] */
    LRD(new char[]{0x24}),

    /** 巴索托洛蒂 [LSL] */
    LSL(new char[]{0x4c, 0x53, 0x4c}),

    /** 利比亚第纳尔 [LYD] */
    LYD(new char[]{0x4c, 0x59, 0x44}),

    /** 摩洛哥迪拉姆 [MAD] */
    MAD(new char[]{0x4d, 0x41, 0x44}),

    /** 摩尔多瓦列伊 [MDL] */
    MDL(new char[]{0x4d, 0x44, 0x4c}),

    /** 马尔加什阿里亚 [MGA] */
    MGA(new char[]{0x4d, 0x47, 0x41}),

    /** 马其顿第纳尔 [ден] */
    MKD(new char[]{0x434, 0x435, 0x43d}),

    /** 缅元 [MMK] */
    MMK(new char[]{0x4d, 0x4d, 0x4b}),

    /** 蒙古图格里克 [₮] */
    MNT(new char[]{0x20ae}),

    /** 毛里塔尼亚乌吉亚 [MRO] */
    MRO(new char[]{0x4d, 0x52, 0x4f}),

    /** 毛里塔尼亚卢比 [₨] */
    MUR(new char[]{0x20a8}),

    /** 马尔代夫拉菲亚 [MVR] */
    MVR(new char[]{0x4d, 0x56, 0x52}),

    /** 马拉维克瓦查 [MWK] */
    MWK(new char[]{0x4d, 0x57, 0x4b}),

    /** 莫桑比克梅蒂卡尔 [MT] */
    MZN(new char[]{0x4d, 0x54}),

    /** 纳米比亚元 [$] */
    NAD(new char[]{0x24}),

    /** 尼日利亚奈拉 [₦] */
    NGN(new char[]{0x20a6}),

    /** 尼加拉瓜科多巴 [C$] */
    NIO(new char[]{0x43, 0x24}),

    /** 尼泊尔卢比 [₨] */
    NPR(new char[]{0x20a8}),

    /** 新西兰元 [NZ$] */
    NZD(new char[]{0x4e, 0x5a, 0x24}),

    /** 阿曼里亚尔 [﷼] */
    OMR(new char[]{0xfdfc}),

    /** 巴拿马巴波亚 [B/.] */
    PAB(new char[]{0x42, 0x2f, 0x2e}),

    /** 秘鲁新索尔 [S/.] */
    PEN(new char[]{0x53, 0x2f, 0x2e}),

    /** 巴布亚新几内亚基那 [PGK] */
    PGK(new char[]{0x50, 0x47, 0x4b}),

    /** 巴拉圭瓜拉尼 [Gs] */
    PYG(new char[]{0x47, 0x73}),

    /** 卡塔尔里亚尔 [﷼] */
    QAR(new char[]{0xfdfc}),

    /** 塞尔维亚第纳尔 [Дин.] */
    RSD(new char[]{0x414, 0x438, 0x43d, 0x2e}),

    /** 卢旺达法郎 [RWF] */
    RWF(new char[]{0x52, 0x57, 0x46}),

    /** 所罗门群岛元 [$] */
    SBD(new char[]{0x24}),

    /** 塞舌尔卢比 [₨] */
    SCR(new char[]{0x20a8}),

    /** 苏丹镑 [SDG] */
    SDG(new char[]{0x53, 0x44, 0x47}),

    /** 圣赫勒拿镑 [£] */
    SHP(new char[]{0xa3}),

    /** 塞拉利昂利昂 [SLL] */
    SLL(new char[]{0x53, 0x4c, 0x4c}),

    /** 索马里先令 [S] */
    SOS(new char[]{0x53}),

    /** 苏里南元 [$] */
    SRD(new char[]{0x24}),

    /** 圣多美多布拉 [STD] */
    STD(new char[]{0x53, 0x54, 0x44}),

    /** 叙利亚镑 [£] */
    SYP(new char[]{0xa3}),

    /** 斯威士兰里兰吉尼 [SZL] */
    SZL(new char[]{0x53, 0x5a, 0x4c}),

    /** 塔吉克斯坦索莫尼 [TJS] */
    TJS(new char[]{0x54, 0x4a, 0x53}),

    /** 土库曼斯坦马纳特 [TMT] */
    TMT(new char[]{0x54, 0x4d, 0x54}),

    /** 突尼斯第纳尔 [TND] */
    TND(new char[]{0x54, 0x4e, 0x44}),

    /** 汤加潘加 [TOP] */
    TOP(new char[]{0x54, 0x4f, 0x50}),

    /** 土耳其里拉 [₺], previous: {0x54, 0x52, 0x59} */
    TRY(new char[]{0x20ba}),

    /** 特立尼达元 [TT$] */
    TTD(new char[]{0x54, 0x54, 0x24}),

    /** 坦桑尼亚先令 [TZS] */
    TZS(new char[]{0x54, 0x5a, 0x53}),

    /** 乌干达先令 [UGX] */
    UGX(new char[]{0x55, 0x47, 0x58}),

    /** 乌拉圭比索 [$U] */
    UYU(new char[]{0x24, 0x55}),

    /** 乌兹别克斯坦索姆 [лв] */
    UZS(new char[]{0x43b, 0x432}),

    /** 委内瑞拉玻利瓦尔 [Bs] */
    VEF(new char[]{0x42, 0x73}),

    /** 瓦努阿图瓦图 [VUV] */
    VUV(new char[]{0x56, 0x55, 0x56}),

    /** 萨摩亚塔拉 [WST] */
    WST(new char[]{0x57, 0x53, 0x54}),

    /** 中非金融合作法郎 [XAF] */
    XAF(new char[]{0x58, 0x41, 0x46}),

    /** 银（盎司） [XAG] */
    XAG(new char[]{0x58, 0x41, 0x47}),

    /** 金（盎司） [XAU] */
    XAU(new char[]{0x58, 0x41, 0x55}),

    /** 东加勒比元 [$] */
    XCD(new char[]{0x24}),

    /** 国际货币基金组织特别提款权 [XDR] */
    XDR(new char[]{0x58, 0x44, 0x52}),

    /** CFA 法郎 [XOF] */
    XOF(new char[]{0x58, 0x4f, 0x46}),

    /** 钯（盎司） [XPD] */
    XPD(new char[]{0x58, 0x50, 0x44}),

    /** CFP 法郎 [XPF] */
    XPF(new char[]{0x58, 0x50, 0x46}),

    /** 铂（盎司） [XPT] */
    XPT(new char[]{0x58, 0x50, 0x54}),

    /** 也门里亚尔 [﷼] */
    YER(new char[]{0xfdfc}),

    /** 南非兰特 [R] */
    ZAR(new char[]{0x52}),

    /** Belarus Ruble [Br] */
    BYN(new char[]{0x42, 0x72}),

    /** El Salvador Colon [$] */
    SVC(new char[]{0x24}),

    /** Zimbabwe Dollar [Z$] */
    ZWD(new char[]{0x5a, 0x24}),

    // ----------------------------------------------------------------------------------others
    /** 安道尔比塞塔 [ADP] */
    ADP(new char[]{0x41, 0x44, 0x50}),

    /** 奥地利先令 [ATS] */
    ATS(new char[]{0x41, 0x54, 0x53}),

    /** AYM [AYM] */
    AYM(new char[]{0x41, 0x59, 0x4d}),

    /** 比利时法郎 [BEF] */
    BEF(new char[]{0x42, 0x45, 0x46}),

    /** 保加利亚硬列弗 [BGL] */
    BGL(new char[]{0x42, 0x47, 0x4c}),

    /** 玻利维亚 Mvdol（资金） [BOV] */
    BOV(new char[]{0x42, 0x4f, 0x56}),

    /** CHE [CHE] */
    CHE(new char[]{0x43, 0x48, 0x45}),

    /** CHW [CHW] */
    CHW(new char[]{0x43, 0x48, 0x57}),

    /** 智利 Unidades de Fomento（资金） [CLF] */
    CLF(new char[]{0x43, 0x4c, 0x46}),

    /** COU [COU] */
    COU(new char[]{0x43, 0x4f, 0x55}),

    /** 塞浦路斯镑 [CYP] */
    CYP(new char[]{0x43, 0x59, 0x50}),

    /** 德国马克 [DEM] */
    DEM(new char[]{0x44, 0x45, 0x4d}),

    /** 爱沙尼亚克朗 [EEK] */
    EEK(new char[]{0x45, 0x45, 0x4b}),

    /** 西班牙比塞塔 [ESP] */
    ESP(new char[]{0x45, 0x53, 0x50}),

    /** 芬兰马克 [FIM] */
    FIM(new char[]{0x46, 0x49, 0x4d}),

    /** 法国法郎 [FRF] */
    FRF(new char[]{0x46, 0x52, 0x46}),

    /** 加纳塞第 [GHC] */
    GHC(new char[]{0x47, 0x48, 0x43}),

    /** 希腊德拉克马 [GRD] */
    GRD(new char[]{0x47, 0x52, 0x44}),

    /** 几内亚比绍比索 [GWP] */
    GWP(new char[]{0x47, 0x57, 0x50}),

    /** 爱尔兰镑 [IEP] */
    IEP(new char[]{0x49, 0x45, 0x50}),

    /** 意大利里拉 [ITL] */
    ITL(new char[]{0x49, 0x54, 0x4c}),

    /** 立陶宛立特 [LTL] */
    LTL(new char[]{0x4c, 0x54, 0x4c}),

    /** 卢森堡法郎 [LUF] */
    LUF(new char[]{0x4c, 0x55, 0x46}),

    /** 拉脱维亚拉特 [LVL] */
    LVL(new char[]{0x4c, 0x56, 0x4c}),

    /** 马达加斯加法郎 [MGF] */
    MGF(new char[]{0x4d, 0x47, 0x46}),

    /** Mauritanian Ouguiya [MRU] */
    MRU(new char[]{0x4d, 0x52, 0x55}),

    /** 马耳他里拉 [MTL] */
    MTL(new char[]{0x4d, 0x54, 0x4c}),

    /** 墨西哥 Unidad de Inversion (UDI)（资金） [MXV] */
    MXV(new char[]{0x4d, 0x58, 0x56}),

    /** 旧莫桑比克美提卡 [MZM] */
    MZM(new char[]{0x4d, 0x5a, 0x4d}),

    /** 荷兰盾 [NLG] */
    NLG(new char[]{0x4e, 0x4c, 0x47}),

    /** 葡萄牙埃斯库多 [PTE] */
    PTE(new char[]{0x50, 0x54, 0x45}),

    /** 苏丹第纳尔 [SDD] */
    SDD(new char[]{0x53, 0x44, 0x44}),

    /** 斯洛文尼亚托拉尔 [SIT] */
    SIT(new char[]{0x53, 0x49, 0x54}),

    /** 斯洛伐克克朗 [SKK] */
    SKK(new char[]{0x53, 0x4b, 0x4b}),

    /** 苏里南盾 [SRG] */
    SRG(new char[]{0x53, 0x52, 0x47}),

    /** South Sudanese Pound [SSP] */
    SSP(new char[]{0x53, 0x53, 0x50}),

    /** São Tomé and Príncipe Dobra [STN] */
    STN(new char[]{0x53, 0x54, 0x4e}),

    /** 土库曼斯坦马纳特 [TMM] */
    TMM(new char[]{0x54, 0x4d, 0x4d}),

    /** 帝汶埃斯库多 [TPE] */
    TPE(new char[]{0x54, 0x50, 0x45}),

    /** 土耳其里拉 [TRL] */
    TRL(new char[]{0x54, 0x52, 0x4c}),

    /** 美元（次日） [USN] */
    USN(new char[]{0x55, 0x53, 0x4e}),

    /** 美元（当日） [USS] */
    USS(new char[]{0x55, 0x53, 0x53}),

    /** UYI [UYI] */
    UYI(new char[]{0x55, 0x59, 0x49}),

    /** 委内瑞拉博利瓦 [VEB] */
    VEB(new char[]{0x56, 0x45, 0x42}),

    /** Venezuelan Bolívar Soberano [VED] */
    VED(new char[]{0x56, 0x45, 0x44}),

    /** Venezuelan Bolívar Soberano [VES] */
    VES(new char[]{0x56, 0x45, 0x53}),

    /** 欧洲复合单位 [XBA] */
    XBA(new char[]{0x58, 0x42, 0x41}),

    /** 欧洲货币联盟 [XBB] */
    XBB(new char[]{0x58, 0x42, 0x42}),

    /** 欧洲计算单位 (XBC) [XBC] */
    XBC(new char[]{0x58, 0x42, 0x43}),

    /** 欧洲计算单位 (XBD) [XBD] */
    XBD(new char[]{0x58, 0x42, 0x44}),

    /** 法国 UIC 法郎 [XFU] */
    XFU(new char[]{0x58, 0x46, 0x55}),

    /** Sucre [XSU] */
    XSU(new char[]{0x58, 0x53, 0x55}),

    /** 为测试保留的代码 [XTS] */
    XTS(new char[]{0x58, 0x54, 0x53}),

    /** ADB Unit of Account [XUA] */
    XUA(new char[]{0x58, 0x55, 0x41}),

    /** 南斯拉夫偌威第纳尔 [YUM] */
    YUM(new char[]{0x59, 0x55, 0x4d}),

    /** 赞比亚克瓦查 [ZMK] */
    ZMK(new char[]{0x5a, 0x4d, 0x4b}),

    /** 货币未知或无效 [XXX] */
    XXX(new char[]{0x58, 0x58, 0x58}),

    // -------------------------------Invalid currency
    /** Guernsey Pound [£] */
    //GGP(new char[]{0xa3}),

    /** Isle of Man Pound [£] */
    //IMP(new char[]{0xa3}),

    /** Jersey Pound [£] */
    //JEP(new char[]{0xa3}),

    /** Tuvalu Dollar [$] */
    //TVD(new char[]{0x24}),

    ;

    private static final Map<String, CurrencyEnum> CURRENCY_CODES = Enums.toMap(CurrencyEnum.class, CurrencyEnum::currencyCode);
    private static final Map<String, CurrencyEnum> NUMERIC_CODES  = Enums.toMap(CurrencyEnum.class, CurrencyEnum::numericCode);

    /**
     * 币种代码
     */
    private final String currencyCode;

    /**
     * 币种符号
     */
    private final String currencySymbol;

    /**
     * 世界各国和地区名称数字
     */
    private final String numericCode;

    /**
     * 币种实例对象
     */
    private final Currency currency;

    /**
     * Constructor
     *
     * @param currencySymbol 币种符号
     */
    CurrencyEnum(char[] currencySymbol) {
        this.currencyCode   = super.name();
        this.currencySymbol = new String(currencySymbol);
        this.currency       = Currency.getInstance(currencyCode);
        this.numericCode    = String.format("%03d", currency.getNumericCode());
    }

    /**
     * @return 币种代码(e.g. CNY)
     */
    public String currencyCode() {
        return currencyCode;
    }

    /**
     * @return 世界各国和地区名称数字代码（e.g. 156）
     */
    public String numericCode() {
        return numericCode;
    }

    /**
     * @return 币种符号(e.g. ¥)
     */
    public String currencySymbol() {
        return currencySymbol;
    }

    /**
     * <pre>
     *  java.util.Currency.getDisplayName(Locale.CHINA)  -> 人民币
     *  java.util.Currency.getDisplayName(Locale.US)     -> Chinese Yuan
     *  java.util.Currency.getSymbol(Locale.CHINA)       -> ￥
     *  java.util.Currency.getNumericCode()              -> 156
     * </pre>
     *
     * @return {@code java.util.Currency } object instance
     */
    public Currency currency() {
        return currency;
    }

    // --------------------------------------------------------------of methods

    /**
     * Gets CurrencyEnum by currency code
     *
     * @param currencyCode the currency code
     * @return CurrencyEnum
     */
    public static CurrencyEnum ofCurrencyCode(String currencyCode) {
        return CURRENCY_CODES.get(currencyCode);
    }

    /**
     * Gets CurrencyEnum by numeric code
     *
     * @param numericCode the numeric code
     * @return CurrencyEnum
     */
    public static CurrencyEnum ofNumericCode(String numericCode) {
        return NUMERIC_CODES.get(numericCode);
    }

    /**
     * Gets CurrencyEnum by currency
     *
     * @param currency the currency
     * @return CurrencyEnum
     */
    public static CurrencyEnum ofCurrency(Currency currency) {
        return CURRENCY_CODES.get(currency.getCurrencyCode());
    }

}
