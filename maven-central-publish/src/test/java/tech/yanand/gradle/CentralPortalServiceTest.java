package tech.yanand.gradle;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.yanand.gradle.CentralPortalService.DeploymentStatus.PUBLISHING;
import static tech.yanand.gradle.ExceptionFactory.CHECK_API_HTTP_STATUS_IS_NOT_OK;
import static tech.yanand.gradle.ExceptionFactory.UPLOAD_API_HTTP_STATUS_IS_NOT_CREATED;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CentralPortalServiceTest {

    private static final String TEST_TOKEN = "test_token";

    private static final String UPLOAD_URL = "https://test.com/upload";

    private static final String STATUS_URL = "https://test.com/status";

    private static final String DEPLOYMENT_ID = "test_ID";

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> response;

    @TempDir
    private Path tempDir;

    @InjectMocks
    private CentralPortalService underTest;

    @BeforeEach
    void setUp() {
        reset(response);
    }

    @Test
    void uploadBundle_returnBody() throws IOException, InterruptedException {
        int statusCode = 201;
        String body = "test body";
        Path uploadFile = Files.createTempFile(tempDir, "test_bundle", ".zip");

        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);

        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenReturn(response);

        String actual = underTest.uploadBundle(UPLOAD_URL, PublishingType.AUTOMATIC, TEST_TOKEN, uploadFile);

        assertEquals(body, actual);
    }

    @Test
    void uploadBundle_throwException() throws IOException, InterruptedException {
        int statusCode = 400;
        String body = "test body";
        Path uploadFile = Files.createTempFile(tempDir, "test_bundle", ".zip");

        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);

        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenReturn(response);

        GradleException actual = assertThrows(GradleException.class,
                () -> underTest.uploadBundle(UPLOAD_URL, PublishingType.AUTOMATIC, TEST_TOKEN, uploadFile));

        assertEquals(format(UPLOAD_API_HTTP_STATUS_IS_NOT_CREATED, statusCode, body), actual.getMessage());
    }

    @Test
    void getDeploymentStatus_returnStatus() throws IOException, InterruptedException {
        int statusCode = 200;
        String body = "{\"deploymentState\": \"" + PUBLISHING + "\"}";

        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);

        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenReturn(response);

        String actual = underTest.getDeploymentStatus(STATUS_URL, TEST_TOKEN, DEPLOYMENT_ID);

        assertEquals(PUBLISHING, actual);
    }

    @Test
    void getDeploymentStatus_throwException() throws IOException, InterruptedException {
        int statusCode = 400;
        String body = "{\"deploymentState\": \"" + PUBLISHING + "\"}";

        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);

        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenReturn(response);

        GradleException actual = assertThrows(GradleException.class,
                () -> underTest.getDeploymentStatus(STATUS_URL, TEST_TOKEN, DEPLOYMENT_ID));

        assertEquals(format(CHECK_API_HTTP_STATUS_IS_NOT_OK, statusCode, body), actual.getMessage());
    }

    @Test
    void uploadBundle_automaticPublicationURI() throws IOException, InterruptedException {
        int statusCode = 201;
        String body = "test body";
        Path uploadFile = Files.createTempFile(tempDir, "test_bundle", ".zip");
        String publishingType = PublishingType.AUTOMATIC;

        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        when(httpClient.send(any(), any(BodyHandler.class))).thenReturn(response);

        underTest.uploadBundle(UPLOAD_URL, publishingType, TEST_TOKEN, uploadFile);

        final ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(captor.capture(), any());

        assertEquals(
            captor.getValue().uri().toString(),
            UPLOAD_URL + publishingType
        );
    }

    @Test
    void uploadBundle_userManagedPublicationURI() throws IOException, InterruptedException {
        int statusCode = 201;
        String body = "test body";
        Path uploadFile = Files.createTempFile(tempDir, "test_bundle", ".zip");
        String publishingType = PublishingType.USER_MANAGED;

        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        when(httpClient.send(any(), any(BodyHandler.class))).thenReturn(response);

        underTest.uploadBundle(UPLOAD_URL, publishingType, TEST_TOKEN, uploadFile);

        final ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(captor.capture(), any());

        assertEquals(
            captor.getValue().uri().toString(),
            UPLOAD_URL + publishingType
        );
    }
}