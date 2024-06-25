package tech.yanand.gradle;

import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;

/**
 * The class to generate exceptions.
 *
 * @author Richard Zhang
 */
final class ExceptionFactory {

    static final String UPLOAD_API_HTTP_STATUS_IS_NOT_CREATED = "Upload failed, status code: [%d], response body: %s";

    static final String CHECK_API_HTTP_STATUS_IS_NOT_OK = "Check status failed, status code: [%d], response body: %s";

    static final String AUTH_TOKEN_NOT_PROVIDED = "Error when upload to Maven Central Portal, the auth token does not provided!";

    static final String UPLOAD_FILE_MUST_PROVIDED = "Upload file must be provided!";

    static final String API_NOT_RETURN_DEPLOYMENT_STATE_FIELD = "The API did not return the 'deploymentState' field.";

    static final String DEPLOYMENT_STATUS_IS_FIELD = "Deployment Status is failed: [%s], please go to [%s] check your deployment.";

    static final String DEPLOYMENT_NOT_FINISHED = "Deployment haven't finished, status is: [%s], please go to [%s] check your deployment.";

    static final String CHECKING_URL = "https://central.sonatype.com/publishing/deployments";

    private ExceptionFactory() {
        // Instantiation is not allowed
    }

    static GradleException uploadApiHttpStatusIsNotCreated(int statusCode, String body) {
        return new GradleException(String.format(UPLOAD_API_HTTP_STATUS_IS_NOT_CREATED, statusCode, body));
    }

    static GradleException checkApiHttpStatusIsNotOk(int statusCode, String body) {
        return new GradleException(String.format(CHECK_API_HTTP_STATUS_IS_NOT_OK, statusCode, body));
    }

    static InvalidUserDataException authTokenNotProvided() {
        return new InvalidUserDataException(AUTH_TOKEN_NOT_PROVIDED);
    }

    static InvalidUserDataException uploadFileMustProvided() {
        return new InvalidUserDataException(UPLOAD_FILE_MUST_PROVIDED);
    }

    static GradleException apiNotReturnDeploymentStateField() {
        return new GradleException(API_NOT_RETURN_DEPLOYMENT_STATE_FIELD);
    }

    static GradleException deploymentStatusIsField(String deploymentStatus) {
        return new GradleException(String.format(DEPLOYMENT_STATUS_IS_FIELD, deploymentStatus, CHECKING_URL));
    }

    static GradleException deploymentNotFinished(String deploymentStatus) {
        return new GradleException(String.format(DEPLOYMENT_NOT_FINISHED, deploymentStatus, CHECKING_URL));
    }
}
