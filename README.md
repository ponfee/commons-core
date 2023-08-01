[![Blog](https://img.shields.io/badge/blog-@Ponfee-informational.svg?logo=Pelican)](http://www.ponfee.cn)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![JDK](https://img.shields.io/badge/jdk-8+-green.svg)](https://www.oracle.com/java/technologies/downloads/#java8)
[![Build status](https://github.com/ponfee/commons-core/workflows/build-with-maven/badge.svg)](https://github.com/ponfee/commons-core/actions)
[![Maven Central](https://img.shields.io/badge/maven--central-1.4-orange.svg?style=plastic&logo=apachemaven)](https://central.sonatype.com/artifact/cn.ponfee/commons-core/1.4)

# Commons Core

A commons java tool lib

## ⬇️ [Download From Maven Central](https://central.sonatype.com/artifact/cn.ponfee/commons-core/1.4)

```xml
<dependency>
  <groupId>cn.ponfee</groupId>
  <artifactId>commons-core</artifactId>
  <version>1.4</version>
</dependency>
```

## 🔄 Build From Source

```bash
./mvnw clean package -DskipTests -Dcheckstyle.skip=true -U
```

## 🛠️ Functions
| **function** |                                           **description**                                                |
| ------------ | -------------------------------------------------------------------------------------------------------- |
| base         | 基础类：Tuple数据类型、原始与包装类型等                                                                        |
| collect      | 集合工具类                                                                                                 |
| concurrent   | 并发相关的工具类：异步批处理、延时消费、线程池创建与监控等                                                         |
| constrain    | 方法参数、实体字段等数据校验                                                                                  |
| data         | 多数据源组件，动态增加数据源                                                                                  |
| date         | 时间工具类(支持各种时间格式的解析，时间周期处理)                                                                 |
| exception    | 异常工具类                                                                                                 |
| export       | 数据导出为Excel(支持复杂表头及切分多个文件)、HTML(支持复杂表头)、CSV(支持切分多个文件)、Console(类似SQL命令行查询结果)  |
| extract      | 数据文件导入：支持XLS/XLSX/CSV格式的文件，支持大文件                                                            |
| http         | HTTP工具类(轻量级，不依赖第三方库)                                                                            |
| io           | IO操作工具类(如文件UTF编码BOM头处理、文件编码探测、文件编码转换及内容替换、数字格式化为KB/MB/GB/TB/PB、Gzip等)         |
| jce          | 加解密工具(对称加解密、非对称加解密、签名/验签、数字信封、ECC算法、哈希算法、国密算法、根证创建与CA证书签发、密码处理等)    |
| model        | 数据模型相关公用类(带类型的Map操作、定义返回结果的结构体、分页实体等)                                               |
| reflect      | 反射工具类(泛型解析、实体与Map互转、实体字段拷贝、实体字段获取、方法调用、Unsafe工具等)                               |
| schema       | 表格数据结构定义，任意JSON格式数据转二维表等                                                                    |
| serial       | 序列化工具类(JDK、JSON、FST、Hessian、Kryo、Protostuff)                                                      |
| spring       | Spring相关工具类                                                                                           |
| tree         | 强大的树型数据结构组件，构建复杂表头的基础(多路树构造及解析、类似`tree -N`命令的多路树打印、二叉树打印等)                |
| util         | 常用工具类(Zip、时间轮、Snowflake id生成算法、Money/币种、一致性Hash算法、Base58编码、高效的字节处理等)              |
