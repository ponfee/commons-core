/**
 * <pre>
 * Java Cryptography Extension提供用于加密、密钥生成和协商
 * 以及 Message Authentication Code（MAC）算法的实现
 * 
 * http://www.freebuf.com/articles/others-articles/136742.html
 * https://www.jianshu.com/p/b10a892879a0
 * 
 * 1、密码：
 *   你知道什么：口令（密码）、口令摘要、质询/响应
 *   你有什么：认证令牌（质询/响应令牌、时间令牌），PIN双因素认证、SSL与认证令牌、智能卡
 *   你是什么：生物特征认证，FAR(False Accept Ratio)，FRR(False Reject Ratio)
 * 
 * 2、对称加密：
 *   优点：效率高
 *   缺点：密钥成几何数增长、需要事先协商密钥
 *   类型：分组密码（DES、3DES、AES），序列密码（RC4）、盐加密（PBE）
 *   分组模式：ECB、CBC、OFB、CFB
 *   填充：NoPadding, PKCS5Padding, PKCS7Padding, PADDING_ISO10126
 *   AES要支持256位密钥：http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
 *                  解压后替换jre/lib/security/目录下的jar文件即可
 * 
 * 3、非对称加密：
 *   优点：密钥分发安全，公开公钥即可
 *   缺点：效率低
 *   算法：
 *      DH：基于离散对数的实现，主要用于密钥交换
 *      RSA：基于大整数因式分解的实现，Ron [R]ivest, Adi [S]hamir, Leonard [A]dleman（三人）
 *          https://www.kancloud.cn/kancloud/rsa_algorithm/48488
 *      DSA：基于整数有限域离散对数难题（特点是两个素数公开），Digital Signature Algorithm，顾名思义只用于数字签名
 *      ECC：基于椭圆曲线算法，指在取代RSA
 *   填充：RSA_PKCS1_PADDING（blocklen=keysize/8–11）、RSA_PKCS1_OAEP_PADDING(keysize-41)、RSA_NO_PADDING
 *   签名/验签：PKCS1及填充、PKCS7格式（附原文|不附原文）
 *   PKCS: Public-Key Cryptography Standards
 *   PKI: Public-Key Infrastructure
 * 
 * 4、对称与非对称结合：数字信封envelop，结构体，（带签名|不带签名）
 * 
 * 5、数字证书：ASN1、X509、p7b、p7r、p10、p12、PEM、DER等概念
 * 
 * 6、BASE64编码：3个字节切分为4个字节后每字节最高位补00  0 ~ 63, “=”，并与编码表对照
 *         前生：解决邮件只能发ASCII码问题
 *         应用：二进制字节流数据文本化（某些场景的网络传输及文本表示）
 * 
 * 7、哈希算法：指纹、摘要，用于防篡改等
 *    MD5：前身MD2、MD3和MD4，安全性低，算法原理（填充、记录长度、数据处理）
 *    SHA-1：已被严重质疑
 *    SHA-2：SHA-224、SHA-256、SHA-384、SHA-512，算法跟SHA-1基本上仍然相似
 *    SHA-3：之前名为Keccak算法，是一个加密杂凑算法
 *    RIPEMD-160：哈希加密算法，用于替代128位哈希函数 MD4、MD5 和 RIPEMD
 * 
 * 8、密码安全：BCrypt、SCrypt、PBKDF2, Argon2
 * 
 * 9、时间戳、签章
 * 
 * 10、区块链：
 *     https://anders.com/blockchain，https://www.zhihu.com/question/22075219
 *     SHA256：
 *          block_header = version + previous_block_hash + merkle_root + time + target_bits + nonce
 *          for i in range(0, 2^32):
 *              if sha256(sha256(block_header)) < target_bits:
 *                  break
 *              else:
 *                  continue

 *          version：block的版本（静态常数）
 *          previous_block_hash：上一个block的hash值（前一区块已经是打包好的）
 *          merkle_root：需要写入的交易记录的hash树的值（根据本次交易包含的交易列表得到）
 *          time：更新时间（utc时间：取打包时的时间，也不需要很精确，前后几十秒也都可以）
 *          target_bits：当前难度
 *          nonce：从0试到最大值2^32
 *
 *          target_bits：TARGET_MAX/difficulty，创世区块时的difficulty=1
 *          TARGET_MAX=0x00000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
 *
 *
 *          https://www.liaoxuefeng.com/article/1124144362997184
 *          ┌─────────────────────────────────────────────┐
 *          │Tx: abcd...1234                              │
 *          ├─────────────────────┬───────────────────────┤
 *          │         TxIn        │         TxOut         │
 *          ├─────────────┬───────┼──────┬────────────────┤
 *          │prev hash    │index  │btc   │pkScript        │
 *          ├─────────────┼───────┼──────┼────────────────┤
 *          │2016...a3c5  │3      │0.15  │OP_DUP a1b2...  │<─┐
 *          ├─────────────┼───────┼──────┼────────────────┤  │
 *          │2015...b6d8  │1      │0.08  │OP_DUP c3d4...  │  │
 *          └─────────────┴───────┴──────┴────────────────┘  │
 *          ┌────────────────────────────────────────────────┘
 *          │  ┌─────────────────────────────────────────────┐
 *          │  │Tx: a1b2...e8f9                              │
 *          │  ├─────────────────────┬───────────────────────┤
 *          │  │         TxIn        │         TxOut         │
 *          │  ├─────────────┬───────┼──────┬────────────────┤
 *          │  │prev hash    │index  │btc   │pkScript        │
 *          │  ├─────────────┼───────┼──────┼────────────────┤
 *          └──│abcd...1234  │0      │0.15  │OP_DUP 3456...  │
 *             ├─────────────┼───────┼──────┼────────────────┤
 *             │cdef...5678  │2      │0.02  │OP_DUP 9876...  │
 *             └─────────────┴───────┴──────┴────────────────┘
 *
 *
 *     ECC：公钥160位的指纹作为钱包地址，一笔交易就是一个地址的比特币转移到另一个地址
 *     Base58：end of 4 bytes long checksum
 * 
 * 11、国密系列：
 *   SM1：为对称加密，其加密强度与AES相当。该算法不公开，调用该算法时，需要通过加密芯片的接口进行调用
 *   SM2：基于ECC的非对称加密算法，该算法已公开。ECC 256位（SM2采用的就是ECC 256位的一种）
 *       安全强度比RSA 2048位高，但运算速度快于RSA
 *       方程：y² = x³ + ax + b
 *       1、用户A选定一条椭圆曲线Ep(a,b)，并取椭圆曲线上一点，作为基点G。
 *       2、用户A选择一个私有密钥k，并生成公开密钥K=kG。
 *       3、用户A将Ep(a,b)和点K，G传给用户B。
 *       4、用户B接到信息后 ，将待传输的明文编码到Ep(a,b)上一点M（编码方法很多，这里不作讨论），并产生一个随机整数r（r<n）。
 *       5、用户B计算点C1=M+rK；C2=rG。
 *       6、用户B将C1、C2传给用户A。
 *       7、用户A接到信息后，计算C1-kC2，结果就是点M。因为C1-kC2=M+rK-k(rG)=M+rK-r(kG)=M，再对点M进行解码就可以得到明文。
 *   SM3：摘要算法，该算法已公开，校验结果为256位
 *   SM4：无线局域网标准的分组数据对称加密算法，该算法已公开，密钥长度和分组长度均为128位（16 byte）
 *   签名算法：SM3WithSM2
 * 
 * 12、Windows证书管理：
 *    当前用户的证书管理：certmgr.msc
 * 
 * 13、TOP：基于时间的一次性密码（Time-based One-time Password），如动态口令
 *    TC = floor((unixtime(now) − unixtime(T0)) / TS) // unixtime(now)：当前unix时间戳，unixtime(T0)：约定的起始时间点的时间戳，TS：哈希有效期的时间长度
 *    TOTP = HASH(SecretKey, TC)
 * </pre>
 *
 * @author Ponfee
 */
package code.ponfee.commons.jce;
