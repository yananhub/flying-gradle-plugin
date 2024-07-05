package tech.yanand.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static tech.yanand.gradle.CentralPortalService.DeploymentStatus.FAILED;
import static tech.yanand.gradle.CentralPortalService.DeploymentStatus.PUBLISHED;
import static tech.yanand.gradle.CentralPortalService.DeploymentStatus.PUBLISHING;
import static tech.yanand.gradle.CentralPortalService.DeploymentStatus.VALIDATED;
import static tech.yanand.gradle.ExceptionFactory.apiNotReturnDeploymentStateField;
import static tech.yanand.gradle.ExceptionFactory.authTokenNotProvided;
import static tech.yanand.gradle.ExceptionFactory.deploymentNotFinished;
import static tech.yanand.gradle.ExceptionFactory.deploymentStatusIsField;
import static tech.yanand.gradle.ExceptionFactory.publishingTypeInvalid;
import static tech.yanand.gradle.ExceptionFactory.uploadFileMustProvided;

/**
 * The task used to upload a bundle to be published to the Maven central portal.
 *
 * @author Richard Zhang
 */
public abstract class PublishToCentralPortalTask extends DefaultTask {

    static final String NAME = "publishToMavenCentralPortal";

    private static final int WAIT_DURATION = 10;

    private Property<String> uploadUrl;

    private Property<String> publishingType;

    private Property<String> statusUrl;

    private Property<String> authToken;

    private RegularFileProperty uploadFile;

    private Property<Integer> maxWait;

    private CentralPortalService centralPortalService = new CentralPortalService();

    /**
     * Construct the task with the default maven central extension.
     *
     * @see DefaultMavenCentralExtension
     */
    public PublishToCentralPortalTask() {
        ObjectFactory objectFactory = getProject().getObjects();
        ExtensionContainer extensions = getProject().getExtensions();
        MavenCentralExtension extension = extensions.findByType(MavenCentralExtension.class);
        extension = extension != null ? extension
                : extensions.create(MavenCentralExtension.class, MavenCentralExtension.NAME, DefaultMavenCentralExtension.class, getProject().getObjects());

        uploadUrl = extension.getUploadUrl();
        publishingType = extension.getPublishingType();
        statusUrl = extension.getStatusUrl();
        authToken = extension.getAuthToken();
        uploadFile = objectFactory.fileProperty();
        maxWait = extension.getMaxWait();
    }

    @TaskAction
    void executeTask() throws InterruptedException, IOException {
        if (!authToken.isPresent()) {
            throw authTokenNotProvided();
        }

        if (!uploadFile.isPresent()) {
            throw uploadFileMustProvided();
        }

        if (!isValidPublishingType(publishingType.get())) {
            throw publishingTypeInvalid();
        }

        String deploymentId = centralPortalService.uploadBundle(uploadUrl.get(), publishingType.get(), authToken.get(), uploadFile.get().getAsFile().toPath());

        int count = 0;
        int checkCount = getCheckCount();
        while (count < checkCount) {
            TimeUnit.SECONDS.sleep(WAIT_DURATION);

            String deploymentStatus = centralPortalService.getDeploymentStatus(statusUrl.get(), authToken.get(), deploymentId);

            if (deploymentStatus == null) {
                throw apiNotReturnDeploymentStateField();
            } else if (FAILED.equals(deploymentStatus)) {
                throw deploymentStatusIsField(deploymentStatus);
            } else if (isSuccessful(deploymentStatus)) {
                getLogger().lifecycle("Upload file success! current status: {}.", deploymentStatus);
                return;
            } else {
                ++count;
            }

            if (count == checkCount) {
                throw deploymentNotFinished(deploymentStatus);
            }
        }
    }

    /**
     * Upload URL for uploading a deployment bundle.
     *
     * @return URL string
     */
    @Input
    public Property<String> getUploadUrl() {
        return uploadUrl;
    }

    /**
     * The URL for retrieving status of a deployment.
     *
     * @return URL string
     */
    @Input
    public Property<String> getStatusUrl() {
        return statusUrl;
    }

    /**
     * The authorization toke for calling central portal APIs
     *
     * @return Token string
     */
    @Input
    @Optional
    public Property<String> getAuthToken() {
        return authToken;
    }

    /**
     * The file for uploading to Maven central portal
     *
     * @return Regular file
     */
    @InputFile
    public RegularFileProperty getUploadFile() {
        return uploadFile;
    }

    /**
     * Max wait time for status API to get 'PUBLISHING' or 'PUBLISHED' status when the
     * publishing type is 'AUTOMATIC', or additionally 'VALIDATED' when the publishing type is
     * 'USER_MANAGED'.
     *
     * @return Duration in seconds
     */
    @Input
    public Property<Integer> getMaxWait() {
        return maxWait;
    }

    /**
     * Whether to publish automatically or manually after a successful upload.
     *
     * @return Publishing type
     */
    @Input
    public Property<String> getPublishingType() { return publishingType; }

    private int getCheckCount() {
        int checkCount = getMaxWait().get() / WAIT_DURATION;
        return checkCount <= 0 ? 1 : checkCount;
    }

    private boolean isSuccessful(String deploymentStatus) {
        return (publishingType.get().equals(PublishingType.USER_MANAGED) && VALIDATED.equals(deploymentStatus))
            || PUBLISHING.equals(deploymentStatus)
            || PUBLISHED.equals(deploymentStatus);
    }

    private boolean isValidPublishingType(String publishingType) {
        return publishingType.equals(PublishingType.AUTOMATIC)
            || publishingType.equals(PublishingType.USER_MANAGED);
    }
}
