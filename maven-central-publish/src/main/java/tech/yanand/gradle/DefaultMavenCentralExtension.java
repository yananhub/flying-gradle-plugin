package tech.yanand.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

class DefaultMavenCentralExtension implements MavenCentralExtension {

    private static final String UPLOAD_URL = "https://central.sonatype.com/api/v1/publisher/upload?publishingType=";

    private static final String STATUS_URL = "https://central.sonatype.com/api/v1/publisher/status?id=";

    private static final Integer MAX_WAIT = 60;

    private Property<String> uploadUrl;

    private Property<String> publishingType;

    private Property<String> statusUrl;

    private Property<String> authToken;

    private DirectoryProperty repoDir;

    private Property<Integer> maxWait;

    public DefaultMavenCentralExtension(ObjectFactory objectFactory) {
        uploadUrl = objectFactory.property(String.class)
                .convention(UPLOAD_URL);
        statusUrl = objectFactory.property(String.class)
                .convention(STATUS_URL);
        publishingType = objectFactory.property(String.class)
                .convention(PublishingType.AUTOMATIC);
        authToken = objectFactory.property(String.class);
        repoDir = objectFactory.directoryProperty();
        maxWait = objectFactory.property(Integer.class)
                .convention(MAX_WAIT);
    }

    @Override
    public Property<String> getUploadUrl() {
        return uploadUrl;
    }

    @Override
    public Property<String> getPublishingType() { return publishingType; }

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

    @Override
    public Property<Integer> getMaxWait() {
        return maxWait;
    }
}
