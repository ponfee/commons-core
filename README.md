[![Blog](https://img.shields.io/badge/blog-@Ponfee-informational.svg?logo=Pelican)](http://www.ponfee.cn)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![JDK](https://img.shields.io/badge/jdk-8+-green.svg)](https://www.oracle.com/java/technologies/downloads/#java8)
[![Build status](https://github.com/ponfee/commons-core/workflows/build-with-maven/badge.svg)](https://github.com/ponfee/commons-core/actions)
[![Maven Central](https://img.shields.io/badge/maven--central-1.3-orange.svg?style=plastic&logo=apachemaven)](https://central.sonatype.dev/artifact/cn.ponfee/commons-core/1.3)

# Commons Core

A commons java tool lib

## [Download From Maven Central](https://central.sonatype.dev/artifact/cn.ponfee/commons-core/1.3)

> [**注意**](https://developer.aliyun.com/mvn/search): **最近aliyun那边的镜像仓受Maven中央仓库网络限制，部分依赖可能会从中央仓库同步文件失败，如果依赖查找不到(即无法下载)请在`settings.xml`文件中删除aliyun mirror的配置(不建议使用aliyun maven mirror)**

```xml
<dependency>
  <groupId>cn.ponfee</groupId>
  <artifactId>commons-core</artifactId>
  <version>1.3</version>
</dependency>
```

## Build From Source

```bash
./mvnw clean package -DskipTests -Dcheckstyle.skip=true -U
```
