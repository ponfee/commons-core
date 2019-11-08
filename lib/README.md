## 本地非标准maven依赖库安装（解决无法传递依赖问题）

### mvn install:install-file  -Dfile=aspose-words-jdk16-1.0.jar  -DgroupId=aspose     -DartifactId=words-jdk16  -Dversion=1.0  -Dpackaging=jar
### mvn install:install-file  -Dfile=third-jce-1.0.jar           -DgroupId=third-jce  -DartifactId=third-jce    -Dversion=1.0  -Dpackaging=jar



### mvn install:install-file -Dfile=cpdetector-1.0.10.jar  -DgroupId=net.sourceforge.cpdetector  -DartifactId=cpdetector  -Dversion=1.0.10  -Dpackaging=jar
### mvn install:install-file -Dfile=jchardet-1.1.jar       -DgroupId=net.sourceforge.jchardet    -DartifactId=jchardet    -Dversion=1.1     -Dpackaging=jar
### mvn install:install-file -Dfile=jargs-1.0.jar          -DgroupId=net.sourceforge.jargs       -DartifactId=jargs       -Dversion=1.0     -Dpackaging=jar



### mvn deploy:deploy-file   -Dfile=cpdetector-1.0.10.jar  -DgroupId=net.sourceforge.cpdetector  -DartifactId=cpdetector  -Dversion=1.0.10  -Dpackaging=jar  -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/  -DrepositoryId=aliyun-nexus-releases
### mvn deploy:deploy-file   -Dfile=jchardet-1.1.jar       -DgroupId=net.sourceforge.jchardet    -DartifactId=jchardet    -Dversion=1.1     -Dpackaging=jar  -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/  -DrepositoryId=aliyun-nexus-releases
### mvn deploy:deploy-file   -Dfile=jargs-1.0.jar          -DgroupId=net.sourceforge.jargs       -DartifactId=jargs       -Dversion=1.0     -Dpackaging=jar  -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/  -DrepositoryId=aliyun-nexus-releases
