package tech.yanand.gradle;

import groovy.json.JsonSlurper;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static tech.yanand.gradle.MultiPartBodyPublisherHelper.getFilePartPublisher;

class CentralPortalService {

    private static final Logger logger = Logging.getLogger(CentralPortalService.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();

    String uploadBundle(String url, String token, Path file) throws IOException, InterruptedException {
        String boundary = UUID.randomUUID().toString().replace("-", "");
        BodyPublisher filePartPublisher = getFilePartPublisher(boundary, file);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(filePartPublisher)
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode != 201) {
            throw new GradleException(String.format("Upload failed, status code: [%d], response body: %s", statusCode, body));
        } else {
            logger.lifecycle("Upload success, response body: {}", body);
            return body;
        }
    }

    String getDeploymentStatus(String url, String token, String deploymentId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url + deploymentId))
                .header("Authorization", "Bearer " + token)
                .POST(noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode != 200) {
            throw new GradleException(String.format("Check status failed, status code: [%d], response body: %s", statusCode, body));
        } else {
            logger.lifecycle("Checking deployment status, response body: {}", body);

            @SuppressWarnings("unchecked")
            var jsonObject = (Map<String, Object>) new JsonSlurper().parseText(body);
            return (String) jsonObject.get("deploymentState");
        }
    }

    class DeploymentStatus {
        private DeploymentStatus() {
            // Instantiation is not allowed
        }

        static final String FAILED = "FAILED";
        static final String PUBLISHING = "PUBLISHING";
        static final String PUBLISHED = "PUBLISHED";
    }
}
