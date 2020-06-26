## 本地非标准maven依赖库安装（解决无法传递依赖问题）

## --------------------------------------------------------------------------------install
### mvn install:install-file  -Dfile=jargs-1.0.jar         -DgroupId=net.sourceforge.jargs       -DartifactId=jargs       -Dversion=1.0    -Dpackaging=jar
### mvn install:install-file  -Dfile=jchardet-1.0.jar      -DgroupId=net.sourceforge.jchardet    -DartifactId=jchardet    -Dversion=1.0    -Dpackaging=jar
### mvn install:install-file  -Dfile=cpdetector-1.0.7.jar  -DgroupId=net.sourceforge.cpdetector  -DartifactId=cpdetector  -Dversion=1.0.7  -Dpackaging=jar



## --------------------------------------------------------------------------------deploy
### mvn deploy:deploy-file  -Dfile=jargs-1.0.jar         -DgroupId=net.sourceforge.jargs       -DartifactId=jargs       -Dversion=1.0    -Dpackaging=jar  -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/  -DrepositoryId=aliyun-nexus-releases
### mvn deploy:deploy-file  -Dfile=jchardet-1.0.jar      -DgroupId=net.sourceforge.jchardet    -DartifactId=jchardet    -Dversion=1.0    -Dpackaging=jar  -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/  -DrepositoryId=aliyun-nexus-releases
### mvn deploy:deploy-file  -Dfile=cpdetector-1.0.7.jar  -DgroupId=net.sourceforge.cpdetector  -DartifactId=cpdetector  -Dversion=1.0.7  -Dpackaging=jar  -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/  -DrepositoryId=aliyun-nexus-releases
