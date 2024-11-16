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
import org.gradle.api.provider.Property;

/**
 * The configuration of how to upload a bundles to maven central portal.
 *
 * @author Richard Zhang
 */
public interface MavenCentralExtension {

    /**
     * The extension name.
     */
    String NAME = "mavenCentral";

    /**
     * Upload URL for uploading a deployment bundle.
     *
     * @return URL string
     */
    Property<String> getUploadUrl();

    /**
     * Whether to publish automatically or manually after a successful upload.
     *
     * @return Publishing type
     */
    Property<String> getPublishingType();

    /**
     * The URL for retrieving status of a deployment.
     *
     * @return URL string
     */
    Property<String> getStatusUrl();

    /**
     * The authorization toke for calling central portal APIs
     *
     * @return Token string
     */
    Property<String> getAuthToken();

    /**
     * The repository directory for zipping the bundle.
     * It is usually a local directory published by the Maven publish plugin.
     *
     * @deprecated Since 1.3.0, it is no longer needed.
     * @return Repository directory
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    DirectoryProperty getRepoDir();

    /**
     * Max wait time for status API to get 'PUBLISHING' or 'PUBLISHED' status when the
     * publishing type is 'AUTOMATIC', or additionally 'VALIDATED' when the publishing type is
     * 'USER_MANAGED'.
     *
     * @return Duration in seconds
     */
    Property<Integer> getMaxWait();
}