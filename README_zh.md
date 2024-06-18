# 请求和线程范围的 Spring 缓存扩展

**其他语言版本: [English](README.md), [中文](README_zh.md).**

根据 Sonatype 官方说明，所有的仓库都要通过 Central Portal 发布。然而现在还没有官方 Gradle 插件来完成这个事情。本项目就是
一个基于Gradle内置的插件来实现它。实现大致思路是，先使用 Gradle 内置的 "maven-publish" 插件将包发布到本地仓库，然后将本地
仓库中的资源打包成 bundle，最后上传到 Maven Central Portal。

要使用这个插件，只需要在 `build.gradle` 文件中加入以下内容：

```groovy
plugins {
    id 'maven-publish'
    id 'signing'
    id 'tech.yanand.plugin.maven-central-publish' version '0.1.0-beta'
}

java {
    // Sonatype官方要求 Java 源代码和 Java 文档
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java

            pom {
                // 请按 Sonatype 官方要求定义
            }
        }
    }

    repositories {
        maven {
            name = "Local"
            def repoDir = version.endsWith('SNAPSHOT') ? 'repos/snapshots' : 'repos/releases'
            url = layout.buildDirectory.dir(repoDir)
        }
    }
}

signing {
    def signingKey = '<your signing key>'
    def signingPassword = '<your signing password>'
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign publishing.publications.mavenJava
}

mavenCentral {
    def dir = version.endsWith('SNAPSHOT') ? 'repos/snapshots' : 'repos/releases'
    repoDir = layout.buildDirectory.dir(dir)

    uploadToken = '<your token>' // 从 Sonatype 官方获取的 Publisher AP 调用的 token
}
```

关于发布 Maven 组件的要求详情，请参考 [Sonatype 官方文档](https://central.sonatype.org/publish/requirements/)。

通过使用`publish`和`publishToMavenCentralPortal`任务上传bundle:

```shell
$ ./gradlew publish publishToMavenCentralPortal
```