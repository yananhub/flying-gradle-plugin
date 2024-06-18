package tech.yanand.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

class DefaultMavenCentralExtension implements MavenCentralExtension {

    private Property<String> uploadUrl;

    private Property<String> statusUrl;

    private Property<String> authToken;

    private DirectoryProperty repoDir;

    public DefaultMavenCentralExtension(ObjectFactory objectFactory) {
        uploadUrl = objectFactory.property(String.class)
                .convention("https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC");
        statusUrl = objectFactory.property(String.class)
                .convention("https://central.sonatype.com/api/v1/publisher/status?id=");
        authToken = objectFactory.property(String.class);
        repoDir = objectFactory.directoryProperty();
    }

    @Override
    public Property<String> getUploadUrl() {
        return uploadUrl;
    }

    @Override
    public Property<String> getStatusUrl() {
        return statusUrl;
    }

    @Override
    public Property<String> getAuthToken() {
        return authToken;
    }

    @Override
    public DirectoryProperty getRepoDir() {
        return repoDir;
    }
}
