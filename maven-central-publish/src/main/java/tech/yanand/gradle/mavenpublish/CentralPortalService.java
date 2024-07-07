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

import groovy.json.JsonSlurper;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.yanand.gradle.mavenpublish.ExceptionFactory.checkApiHttpStatusIsNotOk;
import static tech.yanand.gradle.mavenpublish.ExceptionFactory.uploadApiHttpStatusIsNotCreated;
import static tech.yanand.gradle.mavenpublish.MultiPartBodyPublisherHelper.getFilePartPublisher;

class CentralPortalService {

    private static final Logger logger = Logging.getLogger(CentralPortalService.class);

    private HttpClient httpClient = HttpClient.newHttpClient();

    String uploadBundle(String url, String publishingType, String token, Path uploadFile) throws IOException, InterruptedException {
        String boundary = UUID.randomUUID().toString().replace("-", "");
        BodyPublisher filePartPublisher = getFilePartPublisher(boundary, uploadFile);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url + publishingType))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(filePartPublisher)
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(UTF_8));
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode != 201) {
            throw uploadApiHttpStatusIsNotCreated(statusCode, body);
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

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(UTF_8));
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode != 200) {
            throw checkApiHttpStatusIsNotOk(statusCode, body);
        } else {
            logger.lifecycle("Checking deployment status, response body: {}", body);

            @SuppressWarnings("unchecked")
            var jsonObject = (Map<String, Object>) new JsonSlurper().parseText(body);
            return (String) jsonObject.get("deploymentState");
        }
    }

    final class DeploymentStatus {
        private DeploymentStatus() {
            // Instantiation is not allowed
        }

        static final String FAILED = "FAILED";
        static final String PUBLISHING = "PUBLISHING";
        static final String PUBLISHED = "PUBLISHED";
        static final String PENDING = "PENDING";
        static final String VALIDATED = "VALIDATED";
    }
}
