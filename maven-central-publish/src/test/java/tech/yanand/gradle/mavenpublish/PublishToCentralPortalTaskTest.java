package tech.yanand.gradle.mavenpublish;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static tech.yanand.gradle.mavenpublish.CentralPortalService.DeploymentStatus.FAILED;
import static tech.yanand.gradle.mavenpublish.CentralPortalService.DeploymentStatus.PENDING;
import static tech.yanand.gradle.mavenpublish.CentralPortalService.DeploymentStatus.PUBLISHING;
import static tech.yanand.gradle.mavenpublish.CentralPortalService.DeploymentStatus.VALIDATED;
import static tech.yanand.gradle.mavenpublish.ExceptionFactory.API_NOT_RETURN_DEPLOYMENT_STATE_FIELD;
import static tech.yanand.gradle.mavenpublish.ExceptionFactory.AUTH_TOKEN_NOT_PROVIDED;
import static tech.yanand.gradle.mavenpublish.ExceptionFactory.CHECKING_URL;
import static tech.yanand.gradle.mavenpublish.ExceptionFactory.DEPLOYMENT_NOT_FINISHED;
import static tech.yanand.gradle.mavenpublish.ExceptionFactory.DEPLOYMENT_STATUS_IS_FIELD;
import static tech.yanand.gradle.mavenpublish.ExceptionFactory.PUBLISHING_TYPE_INVALID;
import static tech.yanand.gradle.mavenpublish.ExceptionFactory.UPLOAD_FILE_MUST_PROVIDED;

@ExtendWith(MockitoExtension.class)
class PublishToCentralPortalTaskTest {

    private static final String UPLOAD_URL = "https://test.com/upload";

    private static final String STATUS_URL = "https://test.com/status";

    private static final String AUTH_TOKEN = "test_token";

    private static final String DEPLOYMENT_ID = "test_deployment_id";

    private static final Project project = ProjectBuilder.builder().build();

    static {
        project.getExtensions().create(MavenCentralExtension.class,
                MavenCentralExtension.NAME, DefaultMavenCentralExtension.class, project.getObjects());
    }

    @Mock
    private Property<String> uploadUrl;

    @Mock
    private Property<String> publishingType;

    @Mock
    private Property<String> statusUrl;

    @Mock
    private Property<String> authToken;

    @Mock
    private RegularFileProperty uploadFile;

    @Mock
    private CentralPortalService centralPortalService;

    @Mock
    private RegularFile regularFile;

    @Mock
    private Property<Integer> maxWait;

    @InjectMocks
    private PublishToCentralPortalTask underTest = project.getTasks()
            .maybeCreate("testTask", PublishToCentralPortalTask.class);

    @Test
    void executeTask_authTokenNotPresent() {
        when(authToken.isPresent()).thenReturn(false);

        GradleException actual = assertThrows(GradleException.class, underTest::executeTask);

        assertEquals(AUTH_TOKEN_NOT_PROVIDED, actual.getMessage());
    }

    @Test
    void executeTask_uploadFileNotPresent() {
        when(authToken.isPresent()).thenReturn(true);
        when(uploadFile.isPresent()).thenReturn(false);

        GradleException actual = assertThrows(GradleException.class, underTest::executeTask);

        assertEquals(UPLOAD_FILE_MUST_PROVIDED, actual.getMessage());
    }

    @Test
    void executeTask_publishingTypeIsInvalid() {
        when(authToken.isPresent()).thenReturn(true);
        when(uploadFile.isPresent()).thenReturn(true);
        when(publishingType.get()).thenReturn("INVALID_VALUE");

        GradleException actual = assertThrows(GradleException.class, underTest::executeTask);

        assertEquals(PUBLISHING_TYPE_INVALID, actual.getMessage());
    }

    @Test
    void executeTask_deploymentStatusIsNull() throws IOException, InterruptedException {
        stubbingTheInput();

        when(centralPortalService.getDeploymentStatus(STATUS_URL, AUTH_TOKEN, DEPLOYMENT_ID)).thenReturn(null);

        GradleException actual = assertThrows(GradleException.class, underTest::executeTask);

        assertEquals(API_NOT_RETURN_DEPLOYMENT_STATE_FIELD, actual.getMessage());
    }

    @Test
    void executeTask_deploymentStatusIsFailed() throws IOException, InterruptedException {
        stubbingTheInput();

        when(centralPortalService.getDeploymentStatus(STATUS_URL, AUTH_TOKEN, DEPLOYMENT_ID)).thenReturn(FAILED);

        GradleException actual = assertThrows(GradleException.class, underTest::executeTask);

        assertEquals(format(DEPLOYMENT_STATUS_IS_FIELD, FAILED, CHECKING_URL), actual.getMessage());
    }

    @Test
    void executeTask_deploymentStatusIsPublishing() throws IOException, InterruptedException {
        stubbingTheInput();

        when(centralPortalService.getDeploymentStatus(STATUS_URL, AUTH_TOKEN, DEPLOYMENT_ID)).thenReturn(PUBLISHING);

        assertDoesNotThrow(underTest::executeTask);
    }

    @Test
    void executeTask_deploymentNotFinished() throws IOException, InterruptedException {
        stubbingTheInput();

        when(centralPortalService.getDeploymentStatus(STATUS_URL, AUTH_TOKEN, DEPLOYMENT_ID)).thenReturn(PENDING);

        GradleException actual = assertThrows(GradleException.class, underTest::executeTask);

        assertEquals(format(DEPLOYMENT_NOT_FINISHED, PENDING, CHECKING_URL), actual.getMessage());
    }

    @Test
    void executeTask_deploymentValidatingInUserAutomaticPublishing() throws IOException, InterruptedException {
        stubbingTheInput();

        when(centralPortalService.getDeploymentStatus(STATUS_URL, AUTH_TOKEN, DEPLOYMENT_ID)).thenReturn(VALIDATED);

        GradleException actual = assertThrows(GradleException.class, underTest::executeTask);

        assertEquals(format(DEPLOYMENT_NOT_FINISHED, VALIDATED, CHECKING_URL), actual.getMessage());
    }

    @Test
    void executeTask_deploymentValidatingInUserManagedPublishing() throws IOException, InterruptedException {
        stubbingTheInput(PublishingType.USER_MANAGED);

        when(centralPortalService.getDeploymentStatus(STATUS_URL, AUTH_TOKEN, DEPLOYMENT_ID)).thenReturn(VALIDATED);

        assertDoesNotThrow(underTest::executeTask);
    }

    private void stubbingTheInput() throws IOException, InterruptedException {
        stubbingTheInput(PublishingType.AUTOMATIC);
    }

    private void stubbingTheInput(String currentPublishingType) throws IOException, InterruptedException {
        when(authToken.isPresent()).thenReturn(true);
        when(uploadFile.isPresent()).thenReturn(true);
        when(uploadUrl.get()).thenReturn(UPLOAD_URL);
        when(publishingType.get()).thenReturn(currentPublishingType);
        when(statusUrl.get()).thenReturn(STATUS_URL);
        when(authToken.get()).thenReturn(AUTH_TOKEN);
        when(uploadFile.get()).thenReturn(regularFile);
        when(regularFile.getAsFile()).thenReturn(new File("bundle.zip"));
        when(maxWait.get()).thenReturn(10);

        when(centralPortalService.uploadBundle(eq(UPLOAD_URL), eq(currentPublishingType), eq(AUTH_TOKEN), any(Path.class))).thenReturn(DEPLOYMENT_ID);
    }
}