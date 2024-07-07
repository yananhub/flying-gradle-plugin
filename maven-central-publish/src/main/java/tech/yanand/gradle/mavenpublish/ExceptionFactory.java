/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.yanand.gradle.mavenpublish;

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

    static final String DEPLOYMENT_NOT_FINISHED = "Deployment hasn't finished, status is: [%s], please go to [%s] check your deployment.";

    static final String PUBLISHING_TYPE_INVALID = "The publishingType is invalid. Accepted values are `AUTOMATIC` and `USER_MANAGED`.";

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

    static GradleException publishingTypeInvalid() {
        return new GradleException(PUBLISHING_TYPE_INVALID);
    }
}
