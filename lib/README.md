## 本地非标准maven依赖库安装（解决无法传递依赖问题）

### mvn install:install-file -Dfile=aspose-words-jdk16-1.0.jar -DgroupId=aspose -DartifactId=words-jdk16 -Dversion=1.0 -Dpackaging=jar

### mvn install:install-file -Dfile=cpdetector-1.0.10.jar -DgroupId=cpdetector -DartifactId=cpdetector -Dversion=1.0.10 -Dpackaging=jar

### mvn install:install-file -Dfile=third-jce-1.0.jar -DgroupId=third-jce -DartifactId=third-jce -Dversion=1.0 -Dpackaging=jar

### mvn install:install-file -Dfile=chardet-1.0.jar -DgroupId=org.mozilla.intl -DartifactId=chardet -Dversion=1.0 -Dpackaging=jar

### mvn deploy:deploy-file   -Dfile=chardet-1.0.jar -DgroupId=org.mozilla.intl -DartifactId=chardet -Dversion=1.0 -Dpackaging=jar -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/ -DrepositoryId=aliyun-nexus-releases
