package tech.yanand.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
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

/**
 * The task used to upload a bundle to be published to the Maven central portal.
 *
 * @author Richard Zhang
 */
public abstract class UploadToCentralPortalTask extends DefaultTask {

    private static final int CHECK_STATUS_COUNT = 6;

    private static final String CHECKING_URL = "https://central.sonatype.com/publishing/deployments";

    static final String NAME = "publishToMavenCentralPortal";

    private Property<String> uploadUrl;

    private Property<String> statusUrl;

    private Property<String> authToken;

    private RegularFileProperty uploadFile;

    private CentralPortalService centralPortalService = new CentralPortalService();

    /**
     * Construct the task with the default maven central extension.
     *
     * @see DefaultMavenCentralExtension
     */
    public UploadToCentralPortalTask() {
        ObjectFactory objectFactory = getProject().getObjects();
        MavenCentralExtension extension = getProject().getExtensions().findByType(MavenCentralExtension.class);

        uploadUrl = extension.getUploadUrl();
        statusUrl = extension.getStatusUrl();
        authToken = extension.getAuthToken();
        uploadFile = objectFactory.fileProperty();
    }

    @TaskAction
    void executeTask() throws InterruptedException, IOException {
        if (!authToken.isPresent()) {
            throw new InvalidUserDataException("Error when upload to Maven Central Portal, the upload token does not provided!");
        }

        if (!uploadFile.isPresent()) {
            throw new InvalidUserDataException("Upload file must be provided!");
        }

        String deploymentId = centralPortalService.uploadBundle(uploadUrl.get(), authToken.get(), uploadFile.get().getAsFile().toPath());

        int count = 1;
        while (count <= CHECK_STATUS_COUNT) {
            TimeUnit.SECONDS.sleep(10);

            String deploymentStatus = centralPortalService.getDeploymentStatus(statusUrl.get(), authToken.get(), deploymentId);

            if (deploymentStatus == null) {
                throw new GradleException("The API did not return the 'deploymentState' field.");
            } else if (FAILED.equals(deploymentStatus)) {
                throw new GradleException(String.format("Deployment Status is failed: [%s], " +
                        "please go to [%s] check your deployment.", deploymentStatus, CHECKING_URL));
            } else if (PUBLISHING.equals(deploymentStatus) || PUBLISHED.equals(deploymentStatus)) {
                getLogger().lifecycle("Upload file success! current status: {}.", deploymentStatus);
                return;
            } else {
                ++count;
            }

            if (count == CHECK_STATUS_COUNT) {
                throw new GradleException(String.format("Deployment haven't finished, status is: [%s], " +
                        "please go to [%s] check your deployment.", deploymentStatus, CHECKING_URL));
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
}
