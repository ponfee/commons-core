***如果这些jar无法从中央仓库下载，则可通过以下命令进行本地安装***

- Install to local repository
```shell script
mvn install:install-file  -Dfile=jchardet-1.0.jar      -DgroupId=net.sourceforge.jchardet    -DartifactId=jchardet    -Dversion=1.0    -Dpackaging=jar
mvn install:install-file  -Dfile=cpdetector-1.0.7.jar  -DgroupId=net.sourceforge.cpdetector  -DartifactId=cpdetector  -Dversion=1.0.7  -Dpackaging=jar
```

- Deploy to remote repository
```shell script
mvn deploy:deploy-file  -Dfile=jchardet-1.0.jar      -DgroupId=net.sourceforge.jchardet    -DartifactId=jchardet    -Dversion=1.0    -Dpackaging=jar  -Durl=http://{{domain}}/repository/releases/  -DrepositoryId=repos-releases
mvn deploy:deploy-file  -Dfile=cpdetector-1.0.7.jar  -DgroupId=net.sourceforge.cpdetector  -DartifactId=cpdetector  -Dversion=1.0.7  -Dpackaging=jar  -Durl=http://{{domain}}/repository/releases/  -DrepositoryId=repos-releases
```
