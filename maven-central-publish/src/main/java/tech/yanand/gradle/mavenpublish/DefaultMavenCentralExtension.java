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
