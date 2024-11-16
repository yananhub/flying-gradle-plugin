**Read this in other languages: [English](README.md), [中文](README_zh.md).**

# Maven Central Portal publish plugin (new publish process)

According to Sonatype, the Central Portal will replace the legacy OSSRH service for publishing.
However, there is currently no official Gradle plugin to do this.
This project is based on Gradle built-in plugin to implement it.
The general idea is to use Gradle's built-in "maven-publish" plugin to publish the package to the local repository,
then package the resources in the local repository into a bundle, and finally upload it to the Maven Central Portal.

To use the plugin, just add the following to the `build.gradle` file:

```groovy
plugins {
    id 'maven-publish'
    id 'signing'
    // The Central Portal publish plugin
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
        // Starting from version 1.3.0, it does not need to configure the repository
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
    // Starting from version 1.3.0, it does not need to configure this item
    repoDir = layout.buildDirectory.dir('repos/bundles')
    // Token for Publisher API calls obtained from Sonatype official,
    // it should be Base64 encoded of "username:password".
    authToken = '<your token>'
    // Whether the upload should be automatically published or not. Use 'USER_MANAGED' if you wish to do this manually.
    // This property is optional and defaults to 'AUTOMATIC'.
    publishingType = 'AUTOMATIC'
    // Max wait time for status API to get 'PUBLISHING' or 'PUBLISHED' status when the publishing type is 'AUTOMATIC',
    // or additionally 'VALIDATED' when the publishing type is 'USER_MANAGED'.
    // This property is optional and defaults to 60 seconds.
    maxWait = 60
}
```

For details on the requirements for publishing Maven components, refer to the
[Sonatype official documentation](https://central.sonatype.org/publish/requirements/).

Upload the bundle by using the `publish` and `publishToMavenCentralPortal` tasks:

```shell
$ ./gradlew publish publishToMavenCentralPortal
```

Since version 1.3.0, the execution `publishToMavenCentralPortal` task before no longer need to execute `publish` task:

```shell
$ ./gradlew publishToMavenCentralPortal
```