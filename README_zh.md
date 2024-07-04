**其他语言版本: [English](README.md), [中文](README_zh.md).**

# Maven Central Portal Gradle 发布插件（新发布流程）

根据 Sonatype 官方说明，Central Portal 将取代传统的 OSSRH 服务发布仓库。
然而现在还没有官方 Gradle 插件来完成这个事情。本项目就是一个基于Gradle内置的插件来实现它。
实现大致思路是，先使用 Gradle 内置的 "maven-publish" 插件将包发布到 本地仓库，
然后将本地仓库中的资源打包成 bundle，最后上传到 Maven Central Portal。

要使用这个插件，只需要在 `build.gradle` 文件中加入以下内容：

```groovy
plugins {
    id 'maven-publish'
    id 'signing'
    // Central Portal 发布插件
    id 'tech.yanand.maven-central-publish' version 'x.y.z'
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
            url = layout.buildDirectory.dir('repos/bundles')
        }
    }
}

signing {
    // 关于 GPG 签名, 请参考 https://central.sonatype.org/publish/requirements/gpg/
    def signingKey = '<your signing key>'
    def signingPassword = '<your signing password>'
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign publishing.publications.mavenJava
}

mavenCentral {
    repoDir = layout.buildDirectory.dir('repos/bundles')
    // 从 Sonatype 官方获取的 Publisher API 调用的 token，应为 Base64 编码后的 username:password
    authToken = '<your token>'
    // 上传是否应该自动发布。
    // 如果您希望手动执行此操作，请使用 USER_MANAGED。
    publishingType.set(PublishingType.AUTOMATIC)
}
```

关于发布 Maven 组件的要求详情，请参考 [Sonatype 官方文档](https://central.sonatype.org/publish/requirements/)。

通过使用`publish`和`publishToMavenCentralPortal`任务上传bundle:

```shell
$ ./gradlew publish publishToMavenCentralPortal
```