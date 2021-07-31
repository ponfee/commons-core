***无法从中央仓库下载的jar包可通过以下命令进行本地安装***

- Install to local repository
```shell script
mvn install:install-file  -Dfile=jargs-1.0.jar         -DgroupId=net.sourceforge.jargs       -DartifactId=jargs       -Dversion=1.0    -Dpackaging=jar
mvn install:install-file  -Dfile=jchardet-1.0.jar      -DgroupId=net.sourceforge.jchardet    -DartifactId=jchardet    -Dversion=1.0    -Dpackaging=jar
mvn install:install-file  -Dfile=cpdetector-1.0.7.jar  -DgroupId=net.sourceforge.cpdetector  -DartifactId=cpdetector  -Dversion=1.0.7  -Dpackaging=jar
```

- Deploy to remote repository
```shell script
mvn deploy:deploy-file  -Dfile=jargs-1.0.jar         -DgroupId=net.sourceforge.jargs       -DartifactId=jargs       -Dversion=1.0    -Dpackaging=jar  -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/  -DrepositoryId=aliyun-nexus-releases
mvn deploy:deploy-file  -Dfile=jchardet-1.0.jar      -DgroupId=net.sourceforge.jchardet    -DartifactId=jchardet    -Dversion=1.0    -Dpackaging=jar  -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/  -DrepositoryId=aliyun-nexus-releases
mvn deploy:deploy-file  -Dfile=cpdetector-1.0.7.jar  -DgroupId=net.sourceforge.cpdetector  -DartifactId=cpdetector  -Dversion=1.0.7  -Dpackaging=jar  -Durl=http://maven.aliyun.com/nexus/content/repositories/releases/  -DrepositoryId=aliyun-nexus-releases
```
