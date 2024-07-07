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

/**
 * Whether or not an upload should be published automatically or manually after successful validation.
 */
final class PublishingType {
    private PublishingType() {
        // Instantiation is not allowed
    }

    /**
     * After an upload is successfully validated, the artifact will be published automatically.
     * This is the default.
     */
    static final String AUTOMATIC = "AUTOMATIC";

    /**
     * After an upload is successfully validated, the artifact will need to be
     * published manually.
     */
    static final String USER_MANAGED = "USER_MANAGED";
}