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

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.net.http.HttpRequest.BodyPublisher;

import static java.net.http.HttpRequest.BodyPublishers.concat;
import static java.net.http.HttpRequest.BodyPublishers.ofFile;
import static java.net.http.HttpRequest.BodyPublishers.ofString;

final class MultiPartBodyPublisherHelper {

    static final String CRLF = "\r\n";

    private MultiPartBodyPublisherHelper() {
        // Instantiation is not allowed.
    }

    static BodyPublisher getFilePartPublisher(String boundary, Path file) throws FileNotFoundException {
        String boundaryStart = CRLF + "--" + boundary + CRLF;
        String boundaryEnd = CRLF + "--" + boundary + "--";
        String partMeta = getFilePartMeta(boundaryStart, file.getFileName());
        return concat(ofString(partMeta), ofFile(file), ofString(boundaryEnd));
    }

    private static String getFilePartMeta(String boundaryStart, Path fileName) {
        return new StringBuilder()
                .append(boundaryStart)
                .append("Content-Disposition: form-data; name=\"bundle\"; filename=\"").append(fileName).append("\"").append(CRLF)
                .append("Content-Type: application/octet-stream").append(CRLF)
                .append(CRLF)
                .toString();
    }
}
