**Read this in other languages: [English](README.md), [中文](README_zh.md).**

# Maven Central Portal publish plugin (new publish process)

According to Sonatype, from March 12th, 2024, all repositories will be published through the Central Portal. However,
there is currently no official Gradle plugin to do this. This project is based on Gradle built-in plugin to implement
it. The general idea is to use Gradle's built-in "maven-publish" plugin to publish the package to the local repository,
then package the resources in the local repository into a bundle, and finally upload it to the Maven Central Portal.

To use the plugin, just add the following to the `build.gradle` file:

```groovy
plugins {
    id 'maven-publish'
    id 'signing'
    id 'tech.yanand.maven-central-publish' version 'x.y.z'
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
            url = layout.buildDirectory.dir('repos/bundles')
        }
    }
}

signing {
    // About GPG signing, please refer to https://central.sonatype.org/publish/requirements/gpg/
    def signingKey = '<your signing key>'
    def signingPassword = '<your signing password>'
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign publishing.publications.mavenJava
}

mavenCentral {
    repoDir = layout.buildDirectory.dir('repos/bundles')
    // Token for Publisher API calls obtained from Sonatype official,
    // it should be Base64 encoded of "username:password".
    authToken = '<your token>'
}
```

For details on the requirements for publishing Maven components, refer to the
[Sonatype official documentation](https://central.sonatype.org/publish/requirements/).

Upload the bundle by using the `publish` and `publishToMavenCentralPortal` tasks:

```shell
$ ./gradlew publish publishToMavenCentralPortal
```