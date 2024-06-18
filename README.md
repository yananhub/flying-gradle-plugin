# Maven Central Portal publish plugin (new publish process)

**Read this in other languages: [English](README.md), [中文](README_zh.md).**

According to Sonatype, all repositories will be published through the Central Portal. However, there is currently no
official Gradle plugin to do this. This project is based on Gradle built-in plugin to implement it. The general idea
is to use Gradle's built-in "maven-publish" plugin to publish the package to the local repository, then package the
resources in the local repository into bundles, and finally upload them to the Maven Central Portal.

To use the plugin, just add the following to the `build.gradle` file:

```groovy
plugins {
    id 'maven-publish'
    id 'signing'
    id 'tech.yanand.gradle.maven-central-publish' version 'x.y.z'
}

java {
    // Sonatype officially requires Java source and Java doc
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java

            pom {
                // Please define according to Sonatype official requirements
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

    authToken = '<your token>' // Token for Publisher AP calls obtained from Sonatype official
}
```

For details on the requirements for publishing Maven components, refer to the
[Sonatype official documentation](https://central.sonatype.org/publish/requirements/).

Upload the bundle by using the `publish` and `publishToMavenCentralPortal` tasks:

```shell
$ ./gradlew publish publishToMavenCentralPortal
```